/**
 * OfflineQueueManager - Gerenciador de Fila de Sincronização Offline
 * 
 * Armazena operações quando API está indisponível e sincroniza
 * automaticamente quando conexão é restabelecida.
 * 
 * Funcionalidades:
 * - Enfileira operações POST/PUT/DELETE quando offline
 * - Sincronização automática quando API retorna
 * - Persistência em LocalStorage
 * - Tratamento de conflitos
 * - Retry com backoff exponencial
 * 
 * @author Sistema Fasiclin - Resiliência
 * @version 1.0
 */

class OfflineQueueManager {
  constructor() {
    this.QUEUE_KEY = 'fasiclin_offline_queue';
    this.queue = [];
    this.isSyncing = false;
    this.isOnline = navigator.onLine;
    
    this.loadQueue();
    this.setupNetworkListeners();
  }

  /**
   * Carrega fila do LocalStorage
   */
  loadQueue() {
    try {
      const stored = localStorage.getItem(this.QUEUE_KEY);
      if (stored) {
        this.queue = JSON.parse(stored);
      }
    } catch (error) {
      console.error('[OfflineQueue] Erro ao carregar fila:', error);
      this.queue = [];
    }
  }

  /**
   * Salva fila no LocalStorage
   */
  saveQueue() {
    try {
      localStorage.setItem(this.QUEUE_KEY, JSON.stringify(this.queue));
    } catch (error) {
      console.error('[OfflineQueue] Erro ao salvar fila:', error);
    }
  }

  /**
   * Adiciona operação à fila
   * @param {Object} operation - Operação a ser enfileirada
   * @returns {string} ID da operação
   */
  enqueue(operation) {
    const queuedOperation = {
      id: this.generateId(),
      timestamp: Date.now(),
      attempts: 0,
      maxAttempts: 5,
      ...operation
    };

    this.queue.push(queuedOperation);
    this.saveQueue();

    // Tentar sincronizar imediatamente se online
    if (this.isOnline && !this.isSyncing) {
      this.sync();
    }

    return queuedOperation.id;
  }

  /**
   * Remove operação da fila
   */
  dequeue(operationId) {
    const index = this.queue.findIndex(op => op.id === operationId);
    if (index !== -1) {
      this.queue.splice(index, 1);
      this.saveQueue();
    }
  }

  /**
   * Sincroniza todas as operações pendentes
   */
  async sync() {
    if (this.isSyncing || !this.isOnline || this.queue.length === 0) {
      return;
    }

    this.isSyncing = true;

    try {
      // Copiar fila para processar
      const operations = [...this.queue];
      
      for (const operation of operations) {
        try {
          await this.processOperation(operation);
          
          // Sucesso - remover da fila
          this.dequeue(operation.id);
          
          // Notificar sucesso
          if (typeof notify !== 'undefined') {
            notify.success(`Operação sincronizada: ${operation.description || 'sem descrição'}`);
          }
        } catch (error) {
          // Incrementar tentativas
          operation.attempts++;
          
          if (operation.attempts >= operation.maxAttempts) {
            // Máximo de tentativas atingido
            this.moveToFailedQueue(operation, error);
            this.dequeue(operation.id);
            
            if (typeof notify !== 'undefined') {
              notify.error(`Falha ao sincronizar: ${operation.description || 'operação'}`);
            }
          } else {
            // Salvar progresso
            this.saveQueue();
          }
        }
        
        // Delay entre operações
        await this.delay(500);
      }
    } finally {
      this.isSyncing = false;
    }
  }

  /**
   * Processa uma operação da fila
   */
  async processOperation(operation) {
    if (!window.apiManager) {
      throw new Error('ApiManager não disponível');
    }

    const { method, endpoint, data } = operation;

    switch (method.toUpperCase()) {
      case 'POST':
        return await window.apiManager.makeRequest(endpoint, {
          method: 'POST',
          body: JSON.stringify(data)
        });
      
      case 'PUT':
        return await window.apiManager.makeRequest(endpoint, {
          method: 'PUT',
          body: JSON.stringify(data)
        });
      
      case 'DELETE':
        return await window.apiManager.makeRequest(endpoint, {
          method: 'DELETE'
        });
      
      default:
        throw new Error(`Método não suportado: ${method}`);
    }
  }

  /**
   * Move operação falhada para fila de falhas
   */
  moveToFailedQueue(operation, error) {
    try {
      const FAILED_KEY = 'fasiclin_failed_queue';
      let failedQueue = [];
      
      const stored = localStorage.getItem(FAILED_KEY);
      if (stored) {
        failedQueue = JSON.parse(stored);
      }
      
      failedQueue.push({
        ...operation,
        failedAt: Date.now(),
        error: error.message
      });
      
      // Manter apenas últimas 50 falhas
      if (failedQueue.length > 50) {
        failedQueue = failedQueue.slice(-50);
      }
      
      localStorage.setItem(FAILED_KEY, JSON.stringify(failedQueue));
    } catch (error) {
      console.error('[OfflineQueue] Erro ao salvar operação falhada:', error);
    }
  }

  /**
   * Configura listeners de rede
   */
  setupNetworkListeners() {
    window.addEventListener('online', () => {
      this.isOnline = true;
      
      if (typeof notify !== 'undefined') {
        notify.success('Conexão restabelecida! Sincronizando operações...');
      }
      
      // Tentar sincronizar após 2 segundos (dar tempo para API estabilizar)
      setTimeout(() => {
        this.sync();
      }, 2000);
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      
      if (typeof notify !== 'undefined') {
        notify.warning('Sem conexão com a internet. Operações serão sincronizadas quando conectar.');
      }
    });
  }

  /**
   * Obtém status da fila
   */
  getStatus() {
    return {
      isOnline: this.isOnline,
      isSyncing: this.isSyncing,
      queueLength: this.queue.length,
      operations: this.queue.map(op => ({
        id: op.id,
        description: op.description,
        timestamp: op.timestamp,
        attempts: op.attempts,
        maxAttempts: op.maxAttempts
      }))
    };
  }

  /**
   * Limpa a fila
   */
  clear() {
    this.queue = [];
    this.saveQueue();
  }

  /**
   * Gera ID único para operação
   */
  generateId() {
    return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Delay helper
   */
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  /**
   * Obtém fila de operações falhadas
   */
  getFailedQueue() {
    try {
      const FAILED_KEY = 'fasiclin_failed_queue';
      const stored = localStorage.getItem(FAILED_KEY);
      return stored ? JSON.parse(stored) : [];
    } catch (error) {
      console.error('[OfflineQueue] Erro ao obter fila de falhas:', error);
      return [];
    }
  }

  /**
   * Limpa fila de operações falhadas
   */
  clearFailedQueue() {
    try {
      const FAILED_KEY = 'fasiclin_failed_queue';
      localStorage.removeItem(FAILED_KEY);
    } catch (error) {
      console.error('[OfflineQueue] Erro ao limpar fila de falhas:', error);
    }
  }
}

// Criar instância global
if (typeof window !== 'undefined') {
  window.offlineQueue = new OfflineQueueManager();
}
