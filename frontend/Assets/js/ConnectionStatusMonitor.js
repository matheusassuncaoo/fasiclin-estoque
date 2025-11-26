/**
 * ConnectionStatusMonitor - Monitor Visual de Status da Conexão
 * 
 * Exibe indicador visual do status da API e rede
 * Mostra fila de sincronização pendente
 * Permite retry manual
 * 
 * @author Sistema Fasiclin - Resiliência
 * @version 1.0
 */

class ConnectionStatusMonitor {
  constructor() {
    this.statusElement = null;
    this.detailsElement = null;
    this.isExpanded = false;
    
    this.init();
  }

  /**
   * Inicializa o monitor
   */
  init() {
    this.createStatusIndicator();
    this.startMonitoring();
  }

  /**
   * Cria indicador visual na interface
   */
  createStatusIndicator() {
    // Criar elemento de status
    const statusBar = document.createElement('div');
    statusBar.id = 'connection-status-bar';
    statusBar.className = 'connection-status-bar';
    statusBar.innerHTML = `
      <div class="status-indicator" id="status-indicator">
        <div class="status-dot"></div>
        <span class="status-text">Verificando conexão...</span>
      </div>
      <div class="status-details" id="status-details" style="display: none;">
        <div class="detail-row">
          <span class="detail-label">Internet:</span>
          <span class="detail-value" id="detail-internet">-</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">API:</span>
          <span class="detail-value" id="detail-api">-</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Fila:</span>
          <span class="detail-value" id="detail-queue">-</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Cache:</span>
          <span class="detail-value" id="detail-cache">-</span>
        </div>
        <div class="detail-actions">
          <button id="btn-retry-sync" class="btn-status-action">
            <i data-feather="refresh-cw"></i> Sincronizar
          </button>
          <button id="btn-clear-cache" class="btn-status-action">
            <i data-feather="trash-2"></i> Limpar Cache
          </button>
        </div>
      </div>
    `;

    // Adicionar estilos
    this.injectStyles();

    // Adicionar ao body
    document.body.appendChild(statusBar);

    // Guardar referências
    this.statusElement = document.getElementById('status-indicator');
    this.detailsElement = document.getElementById('status-details');

    // Setup eventos
    this.setupEvents();
  }

  /**
   * Injeta estilos CSS
   */
  injectStyles() {
    const style = document.createElement('style');
    style.textContent = `
      .connection-status-bar {
        position: fixed;
        bottom: 20px;
        right: 20px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 9999;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        max-width: 350px;
      }

      .status-indicator {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 16px;
        cursor: pointer;
        user-select: none;
      }

      .status-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: #94a3b8;
        animation: pulse 2s ease-in-out infinite;
      }

      .status-dot.online {
        background: #10b981;
      }

      .status-dot.offline {
        background: #ef4444;
      }

      .status-dot.degraded {
        background: #f59e0b;
      }

      @keyframes pulse {
        0%, 100% { opacity: 1; }
        50% { opacity: 0.5; }
      }

      .status-text {
        font-size: 14px;
        font-weight: 500;
        color: #1e293b;
      }

      .status-details {
        border-top: 1px solid #e2e8f0;
        padding: 12px 16px;
        animation: slideDown 0.2s ease-out;
      }

      @keyframes slideDown {
        from {
          opacity: 0;
          transform: translateY(-10px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      .detail-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 6px 0;
        font-size: 13px;
      }

      .detail-label {
        color: #64748b;
        font-weight: 500;
      }

      .detail-value {
        color: #1e293b;
        font-weight: 600;
      }

      .detail-value.success {
        color: #10b981;
      }

      .detail-value.error {
        color: #ef4444;
      }

      .detail-value.warning {
        color: #f59e0b;
      }

      .detail-actions {
        display: flex;
        gap: 8px;
        margin-top: 12px;
      }

      .btn-status-action {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        padding: 8px 12px;
        border: 1px solid #e2e8f0;
        background: #f8fafc;
        border-radius: 6px;
        font-size: 12px;
        font-weight: 500;
        color: #475569;
        cursor: pointer;
        transition: all 0.2s;
      }

      .btn-status-action:hover {
        background: #e2e8f0;
        border-color: #cbd5e1;
      }

      .btn-status-action svg {
        width: 14px;
        height: 14px;
      }
    `;
    document.head.appendChild(style);
  }

  /**
   * Setup eventos
   */
  setupEvents() {
    // Toggle detalhes ao clicar
    this.statusElement.addEventListener('click', () => {
      this.toggleDetails();
    });

    // Botão sincronizar
    document.getElementById('btn-retry-sync')?.addEventListener('click', () => {
      this.retrySync();
    });

    // Botão limpar cache
    document.getElementById('btn-clear-cache')?.addEventListener('click', () => {
      this.clearCache();
    });
  }

  /**
   * Inicia monitoramento
   */
  startMonitoring() {
    // Atualizar a cada 5 segundos
    setInterval(() => {
      this.updateStatus();
    }, 5000);

    // Atualizar imediatamente
    this.updateStatus();
  }

  /**
   * Atualiza status
   */
  async updateStatus() {
    const isOnline = navigator.onLine;
    const apiManager = window.apiManager;
    const offlineQueue = window.offlineQueue;
    const cacheManager = window.cacheManager;

    let statusClass = 'offline';
    let statusText = 'Offline';

    if (isOnline) {
      if (apiManager?.apiAvailable) {
        statusClass = 'online';
        statusText = 'Online';
        
        if (offlineQueue && offlineQueue.queue.length > 0) {
          statusClass = 'degraded';
          statusText = `Sincronizando (${offlineQueue.queue.length})`;
        }
      } else {
        statusClass = 'degraded';
        statusText = 'API Indisponível';
      }
    }

    // Atualizar indicador
    const dot = this.statusElement.querySelector('.status-dot');
    const text = this.statusElement.querySelector('.status-text');
    
    dot.className = `status-dot ${statusClass}`;
    text.textContent = statusText;

    // Atualizar detalhes se expandido
    if (this.isExpanded) {
      this.updateDetails();
    }
  }

  /**
   * Atualiza detalhes
   */
  updateDetails() {
    const isOnline = navigator.onLine;
    const apiManager = window.apiManager;
    const offlineQueue = window.offlineQueue;
    const cacheManager = window.cacheManager;

    // Internet
    const internetEl = document.getElementById('detail-internet');
    if (internetEl) {
      internetEl.textContent = isOnline ? 'Conectado' : 'Desconectado';
      internetEl.className = `detail-value ${isOnline ? 'success' : 'error'}`;
    }

    // API
    const apiEl = document.getElementById('detail-api');
    if (apiEl && apiManager) {
      const apiStatus = apiManager.apiAvailable ? 'Disponível' : 'Indisponível';
      apiEl.textContent = apiStatus;
      apiEl.className = `detail-value ${apiManager.apiAvailable ? 'success' : 'error'}`;
    }

    // Fila
    const queueEl = document.getElementById('detail-queue');
    if (queueEl && offlineQueue) {
      const queueLength = offlineQueue.queue.length;
      queueEl.textContent = queueLength === 0 ? 'Vazia' : `${queueLength} pendente(s)`;
      queueEl.className = `detail-value ${queueLength === 0 ? 'success' : 'warning'}`;
    }

    // Cache
    const cacheEl = document.getElementById('detail-cache');
    if (cacheEl && cacheManager) {
      const stats = cacheManager.getStats();
      if (stats.enabled) {
        cacheEl.textContent = `${stats.totalItems} itens (${stats.totalSizeKB} KB)`;
        cacheEl.className = 'detail-value';
      } else {
        cacheEl.textContent = 'Desabilitado';
        cacheEl.className = 'detail-value error';
      }
    }

    // Atualizar ícones Feather
    if (typeof feather !== 'undefined') {
      feather.replace();
    }
  }

  /**
   * Toggle detalhes
   */
  toggleDetails() {
    this.isExpanded = !this.isExpanded;
    
    if (this.isExpanded) {
      this.detailsElement.style.display = 'block';
      this.updateDetails();
    } else {
      this.detailsElement.style.display = 'none';
    }
  }

  /**
   * Retry sincronização
   */
  async retrySync() {
    if (window.offlineQueue) {
      if (typeof notify !== 'undefined') {
        notify.info('Iniciando sincronização...');
      }
      
      await window.offlineQueue.sync();
      this.updateDetails();
    }
  }

  /**
   * Limpar cache
   */
  clearCache() {
    if (confirm('Tem certeza que deseja limpar todo o cache? Isso pode afetar a operação offline.')) {
      if (window.cacheManager) {
        window.cacheManager.clearAll();
        
        if (typeof notify !== 'undefined') {
          notify.success('Cache limpo com sucesso');
        }
        
        this.updateDetails();
      }
    }
  }
}

// Inicializar quando DOM estiver pronto
if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      window.connectionMonitor = new ConnectionStatusMonitor();
    });
  } else {
    window.connectionMonitor = new ConnectionStatusMonitor();
  }
}
