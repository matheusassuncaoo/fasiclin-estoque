/**
 * CacheManager - Sistema de Cache Local para Resiliência Offline
 * 
 * Gerencia cache de dados críticos no LocalStorage para garantir
 * operação contínua mesmo quando API ou banco estão indisponíveis.
 * 
 * Funcionalidades:
 * - Cache automático de ordens, produtos e fornecedores
 * - Expiração configurável de dados
 * - Sincronização automática quando API retorna
 * - Detecção de conflitos de versão
 * 
 * @author Sistema Fasiclin - Resiliência
 * @version 1.0
 */

class CacheManager {
  constructor() {
    this.CACHE_PREFIX = 'fasiclin_cache_';
    this.CACHE_VERSION = '1.0';
    
    // Tempo de expiração por tipo (em milissegundos)
    this.CACHE_EXPIRY = {
      ordens: 30 * 60 * 1000,      // 30 minutos
      produtos: 60 * 60 * 1000,    // 1 hora
      fornecedores: 60 * 60 * 1000, // 1 hora
      itens: 30 * 60 * 1000         // 30 minutos
    };

    this.initCache();
  }

  /**
   * Inicializa o sistema de cache
   */
  initCache() {
    try {
      // Verificar se LocalStorage está disponível
      if (!this.isLocalStorageAvailable()) {
        console.warn('[CacheManager] LocalStorage não disponível - cache desabilitado');
        this.enabled = false;
        return;
      }

      this.enabled = true;

      // Limpar cache expirado na inicialização
      this.cleanExpiredCache();

    } catch (error) {
      console.error('[CacheManager] Erro ao inicializar cache:', error);
      this.enabled = false;
    }
  }

  /**
   * Verifica se LocalStorage está disponível
   */
  isLocalStorageAvailable() {
    try {
      const test = '__localStorage_test__';
      localStorage.setItem(test, test);
      localStorage.removeItem(test);
      return true;
    } catch (e) {
      return false;
    }
  }

  /**
   * Salva dados no cache
   * @param {string} key - Chave do cache
   * @param {any} data - Dados a serem armazenados
   * @param {string} type - Tipo de dado (ordens, produtos, etc)
   */
  set(key, data, type = 'default') {
    if (!this.enabled) return false;

    try {
      const cacheKey = this.getCacheKey(key);
      const cacheData = {
        version: this.CACHE_VERSION,
        type: type,
        data: data,
        timestamp: Date.now(),
        expiry: this.CACHE_EXPIRY[type] || 30 * 60 * 1000
      };

      localStorage.setItem(cacheKey, JSON.stringify(cacheData));
      return true;
    } catch (error) {
      console.error('[CacheManager] Erro ao salvar cache:', error);
      
      // Se falhar por quota excedida, limpar cache antigo
      if (error.name === 'QuotaExceededError') {
        this.clearOldestCache();
        // Tentar novamente
        try {
          localStorage.setItem(cacheKey, JSON.stringify(cacheData));
          return true;
        } catch (retryError) {
          return false;
        }
      }
      
      return false;
    }
  }

  /**
   * Recupera dados do cache
   * @param {string} key - Chave do cache
   * @returns {any|null} Dados armazenados ou null se não existir/expirado
   */
  get(key) {
    if (!this.enabled) return null;

    try {
      const cacheKey = this.getCacheKey(key);
      const cached = localStorage.getItem(cacheKey);
      
      if (!cached) return null;

      const cacheData = JSON.parse(cached);
      
      // Verificar expiração
      if (this.isExpired(cacheData)) {
        localStorage.removeItem(cacheKey);
        return null;
      }

      return cacheData.data;
    } catch (error) {
      console.error('[CacheManager] Erro ao recuperar cache:', error);
      return null;
    }
  }

  /**
   * Verifica se dados em cache estão expirados
   */
  isExpired(cacheData) {
    const now = Date.now();
    const age = now - cacheData.timestamp;
    return age > cacheData.expiry;
  }

  /**
   * Remove item específico do cache
   */
  remove(key) {
    if (!this.enabled) return;
    
    try {
      const cacheKey = this.getCacheKey(key);
      localStorage.removeItem(cacheKey);
    } catch (error) {
      console.error('[CacheManager] Erro ao remover cache:', error);
    }
  }

  /**
   * Limpa todo o cache
   */
  clearAll() {
    if (!this.enabled) return;

    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith(this.CACHE_PREFIX)) {
          localStorage.removeItem(key);
        }
      });
    } catch (error) {
      console.error('[CacheManager] Erro ao limpar cache:', error);
    }
  }

  /**
   * Limpa apenas cache expirado
   */
  cleanExpiredCache() {
    if (!this.enabled) return;

    try {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith(this.CACHE_PREFIX)) {
          try {
            const cached = localStorage.getItem(key);
            const cacheData = JSON.parse(cached);
            
            if (this.isExpired(cacheData)) {
              localStorage.removeItem(key);
            }
          } catch (error) {
            // Cache corrompido, remover
            localStorage.removeItem(key);
          }
        }
      });
    } catch (error) {
      console.error('[CacheManager] Erro ao limpar cache expirado:', error);
    }
  }

  /**
   * Remove o cache mais antigo quando quota é excedida
   */
  clearOldestCache() {
    if (!this.enabled) return;

    try {
      const keys = Object.keys(localStorage);
      const cacheKeys = keys.filter(k => k.startsWith(this.CACHE_PREFIX));
      
      if (cacheKeys.length === 0) return;

      // Encontrar o cache mais antigo
      let oldestKey = null;
      let oldestTime = Date.now();

      cacheKeys.forEach(key => {
        try {
          const cached = JSON.parse(localStorage.getItem(key));
          if (cached.timestamp < oldestTime) {
            oldestTime = cached.timestamp;
            oldestKey = key;
          }
        } catch (error) {
          // Ignorar erros de parse
        }
      });

      if (oldestKey) {
        localStorage.removeItem(oldestKey);
      }
    } catch (error) {
      console.error('[CacheManager] Erro ao limpar cache antigo:', error);
    }
  }

  /**
   * Gera chave de cache completa
   */
  getCacheKey(key) {
    return `${this.CACHE_PREFIX}${key}`;
  }

  /**
   * Obtém estatísticas do cache
   */
  getStats() {
    if (!this.enabled) {
      return { enabled: false };
    }

    try {
      const keys = Object.keys(localStorage);
      const cacheKeys = keys.filter(k => k.startsWith(this.CACHE_PREFIX));
      
      let totalSize = 0;
      const stats = {
        enabled: true,
        totalItems: cacheKeys.length,
        byType: {}
      };

      cacheKeys.forEach(key => {
        try {
          const value = localStorage.getItem(key);
          totalSize += value.length;
          
          const cacheData = JSON.parse(value);
          const type = cacheData.type || 'unknown';
          
          if (!stats.byType[type]) {
            stats.byType[type] = { count: 0, size: 0 };
          }
          
          stats.byType[type].count++;
          stats.byType[type].size += value.length;
        } catch (error) {
          // Ignorar erros
        }
      });

      stats.totalSize = totalSize;
      stats.totalSizeKB = (totalSize / 1024).toFixed(2);

      return stats;
    } catch (error) {
      console.error('[CacheManager] Erro ao obter estatísticas:', error);
      return { enabled: true, error: error.message };
    }
  }

  /**
   * Atualiza timestamp de um cache sem modificar dados
   * Útil para renovar TTL
   */
  touch(key) {
    if (!this.enabled) return false;

    try {
      const cacheKey = this.getCacheKey(key);
      const cached = localStorage.getItem(cacheKey);
      
      if (!cached) return false;

      const cacheData = JSON.parse(cached);
      cacheData.timestamp = Date.now();
      
      localStorage.setItem(cacheKey, JSON.stringify(cacheData));
      return true;
    } catch (error) {
      console.error('[CacheManager] Erro ao atualizar timestamp:', error);
      return false;
    }
  }
}

// Criar instância global
if (typeof window !== 'undefined') {
  window.cacheManager = new CacheManager();
}
