/**
 * ApiManager - Gerenciador de APIs para Sistema Fasiclin
 * Classe responsável por todas as comunicações HTTP com o backend
 * Implementa padrões REST e tratamento de erros consistente
 */
class ApiManager {
  constructor() {
    // Configuração base da API - detecta ambiente automaticamente
    this.baseURL = this.detectBaseURL();
    this.defaultHeaders = {
      "Content-Type": "application/json",
      Accept: "application/json",
    };

    // Configurações de timeout e retry
    this.timeout = 30000; // 30 segundos
    this.maxRetries = 3;
    this.retryDelay = 1000; // 1 segundo inicial (backoff exponencial)
    
    // Status de conectividade
    this.isOnline = navigator.onLine;
    this.apiAvailable = true;
    this.lastHealthCheck = null;
    
    // Rate limiting
    this.requestQueue = new Map();
    this.maxRequestsPerSecond = 10;
    
    // Setup listeners
    this.setupConnectivityListeners();
  }

  /**
   * Detecta a URL base da API automaticamente
   * Em produção (Render), usa URL relativa; em dev, usa localhost
   */
  detectBaseURL() {
    const hostname = window.location.hostname;
    
    // Se estiver no Render ou em produção
    if (hostname.includes('onrender.com') || hostname.includes('render.com')) {
      return '/api';
    }
    
    // Se for localhost ou 127.0.0.1, usar porta padrão do backend
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      return 'http://localhost:8080/api';
    }
    
    // Fallback: tentar URL relativa
    return '/api';
  }

  /**
   * Configura listeners de conectividade
   */
  setupConnectivityListeners() {
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.checkApiHealth();
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      this.apiAvailable = false;
    });
    
    // Health check periódico (a cada 2 minutos)
    setInterval(() => {
      if (this.isOnline) {
        this.checkApiHealth();
      }
    }, 120000);
  }

  /**
   * Verifica saúde da API
   */
  async checkApiHealth() {
    try {
      const response = await fetch(`${this.baseURL}/health`, {
        method: 'GET',
        signal: AbortSignal.timeout(5000)
      });
      
      this.apiAvailable = response.ok;
      this.lastHealthCheck = Date.now();
      
      return this.apiAvailable;
    } catch (error) {
      this.apiAvailable = false;
      this.lastHealthCheck = Date.now();
      return false;
    }
  }

  // Monta header de Basic Auth a partir de login e senha
  buildBasicAuthHeader(login, senha) {
    if (!login || !senha) return {};
    try {
      const token = btoa(`${login}:${senha}`);
      return { Authorization: `Basic ${token}` };
    } catch (_) {
      return {};
    }
  }

  /**
   * Método genérico para fazer requisições HTTP com cache e fallback
   * @param {string} endpoint - Endpoint da API
   * @param {Object} options - Opções da requisição
   * @returns {Promise} - Resposta da API ou cache
   */
  async makeRequest(endpoint, options = {}) {
    const method = options.method || "GET";
    const cacheKey = `api_${method}_${endpoint}`;
    
    // Para GET: tentar cache primeiro se offline
    if (method === 'GET' && !this.isOnline && window.cacheManager) {
      const cached = window.cacheManager.get(cacheKey);
      if (cached) {
        return cached;
      }
    }
    
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      method: method,
      headers: {
        ...this.defaultHeaders,
        ...options.headers,
      },
      cache: 'no-store',
      ...options,
    };

    // Adicionar body apenas para métodos que suportam
    if (config.method !== "GET" && options.body) {
      config.body =
        typeof options.body === "string"
          ? options.body
          : JSON.stringify(options.body);
    }

    let lastError;

    // Implementação de retry com backoff exponencial
    for (let attempt = 1; attempt <= this.maxRetries; attempt++) {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), this.timeout);

        const response = await fetch(url, {
          ...config,
          signal: controller.signal,
        });

        clearTimeout(timeoutId);

        // Marcar API como disponível
        this.apiAvailable = true;

        // Verificar se a resposta é válida
        if (!response.ok) {
          let serverMessage = "";
          try {
            const ct = response.headers.get("content-type");
            if (ct && ct.includes("application/json")) {
              const body = await response.json();
              serverMessage = body?.message || body?.error || JSON.stringify(body);
            } else {
              serverMessage = await response.text();
            }
          } catch (_) {
            // ignora falha no parse
          }
          const msgSuffix = serverMessage ? ` - ${serverMessage}` : "";
          throw new Error(`HTTP ${response.status}: ${response.statusText}${msgSuffix}`);
        }

        // Parsear resposta
        const contentType = response.headers.get("content-type");
        let data;

        if (contentType && contentType.includes("application/json")) {
          data = await response.json();
        } else {
          data = await response.text();
        }

        // Para GET: salvar no cache
        if (method === 'GET' && window.cacheManager) {
          const cacheType = this.getCacheType(endpoint);
          window.cacheManager.set(cacheKey, data, cacheType);
        }

        return data;
      } catch (error) {
        lastError = error;

        // Marcar API como indisponível se erro de rede
        if (this.isRetryableError(error)) {
          this.apiAvailable = false;
        }

        // Se não é a última tentativa e é um erro de rede, aguardar antes de tentar novamente
        if (attempt < this.maxRetries && this.isRetryableError(error)) {
          // Backoff exponencial: 1s, 2s, 4s
          await this.delay(this.retryDelay * Math.pow(2, attempt - 1));
        } else {
          break;
        }
      }
    }

    // Todas tentativas falharam - tentar fallbacks
    
    // Para GET: retornar cache mesmo expirado se disponível
    if (method === 'GET' && window.cacheManager) {
      const cached = window.cacheManager.get(cacheKey);
      if (cached) {
        if (typeof notify !== 'undefined') {
          notify.warning('API indisponível. Usando dados em cache.');
        }
        return cached;
      }
    }
    
    // Para operações de escrita: enfileirar para sincronização
    if ((method === 'POST' || method === 'PUT' || method === 'DELETE') && window.offlineQueue) {
      const queueId = window.offlineQueue.enqueue({
        method: method,
        endpoint: endpoint,
        data: options.body ? JSON.parse(config.body) : null,
        description: this.getOperationDescription(method, endpoint)
      });
      
      if (typeof notify !== 'undefined') {
        notify.warning('Operação enfileirada para sincronização quando API retornar');
      }
      
      // Retornar objeto indicando que foi enfileirado
      return {
        queued: true,
        queueId: queueId,
        message: 'Operação será sincronizada quando conexão for restabelecida'
      };
    }

    // Se chegou aqui, todas as tentativas falharam e sem fallback
    throw this.createApiError(lastError, endpoint, config.method);
  }

  /**
   * Determina tipo de cache baseado no endpoint
   */
  getCacheType(endpoint) {
    if (endpoint.includes('/ordens-compra')) return 'ordens';
    if (endpoint.includes('/produtos')) return 'produtos';
    if (endpoint.includes('/fornecedores')) return 'fornecedores';
    if (endpoint.includes('/itens')) return 'itens';
    return 'default';
  }

  /**
   * Gera descrição legível da operação
   */
  getOperationDescription(method, endpoint) {
    const parts = endpoint.split('/').filter(p => p);
    const resource = parts[0] || 'recurso';
    
    const descriptions = {
      'POST': `Criar ${resource}`,
      'PUT': `Atualizar ${resource}`,
      'DELETE': `Excluir ${resource}`
    };
    
    return descriptions[method] || `${method} em ${resource}`;
  }

  /**
   * Delay helper
   */
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  /**
   * Determina se um erro é passível de retry
   * @param {Error} error - Erro a ser verificado
   * @returns {boolean} - Se deve tentar novamente
   */
  isRetryableError(error) {
    // Erros de rede ou timeout são passíveis de retry
    return (
      error.name === "AbortError" ||
      error.name === "TypeError" ||
      error.message.includes("Failed to fetch") ||
      error.message.includes("Network Error")
    );
  }

  /**
   * Cria um erro padronizado da API
   * @param {Error} originalError - Erro original
   * @param {string} endpoint - Endpoint que falhou
   * @param {string} method - Método HTTP
   * @returns {Error} - Erro padronizado
   */
  createApiError(originalError, endpoint, method) {
    const error = new Error(
      `Falha na requisição ${method} ${endpoint}: ${originalError.message}`
    );
    error.originalError = originalError;
    error.endpoint = endpoint;
    error.method = method;
    error.isApiError = true;
    return error;
  }

  // ============================================
  // MÉTODOS DE SANITIZAÇÃO E VALIDAÇÃO
  // ============================================

  /**
   * Valida e sanitiza um ID numérico
   * @param {any} id - ID a ser validado
   * @param {string} fieldName - Nome do campo para mensagem de erro
   * @returns {number} - ID validado
   * @throws {Error} - Se ID for inválido
   */
  validateId(id, fieldName = 'ID') {
    if (id === null || id === undefined) {
      throw new Error(`${fieldName} é obrigatório`);
    }
    
    const numId = parseInt(id, 10);
    
    if (isNaN(numId)) {
      throw new Error(`${fieldName} deve ser um número válido`);
    }
    
    if (numId <= 0) {
      throw new Error(`${fieldName} deve ser maior que zero`);
    }
    
    if (numId > 2147483647) {
      throw new Error(`${fieldName} excede o valor máximo permitido`);
    }
    
    return numId;
  }

  /**
   * Sanitiza uma string removendo caracteres perigosos
   * @param {string} value - Valor a ser sanitizado
   * @returns {string} - Valor sanitizado
   */
  sanitizeString(value) {
    if (typeof value !== 'string') return value;
    
    return value
      .replace(/<[^>]*>/g, '') // Remove tags HTML
      .replace(/javascript:/gi, '') // Remove javascript:
      .replace(/on\w+=/gi, '') // Remove event handlers
      .replace(/data:/gi, '') // Remove data: URIs
      .trim();
  }

  /**
   * Sanitiza um objeto completo
   * @param {Object} data - Objeto a ser sanitizado
   * @returns {Object} - Objeto sanitizado
   */
  sanitizeInput(data) {
    if (typeof data !== 'object' || data === null) return data;
    
    const sanitized = {};
    
    for (const [key, value] of Object.entries(data)) {
      // Sanitizar a chave também
      const safeKey = this.sanitizeString(key);
      
      if (typeof value === 'string') {
        sanitized[safeKey] = this.sanitizeString(value);
      } else if (typeof value === 'number') {
        sanitized[safeKey] = isNaN(value) || !isFinite(value) ? null : value;
      } else if (value instanceof Date) {
        sanitized[safeKey] = value.toISOString().split('T')[0];
      } else if (Array.isArray(value)) {
        sanitized[safeKey] = value.map(item => this.sanitizeInput(item));
      } else if (typeof value === 'object' && value !== null) {
        sanitized[safeKey] = this.sanitizeInput(value);
      } else {
        sanitized[safeKey] = value;
      }
    }
    
    return sanitized;
  }

  /**
   * Sanitiza um valor decimal
   * @param {any} value - Valor a ser sanitizado
   * @returns {string|null} - Valor decimal formatado ou null
   */
  sanitizeDecimal(value) {
    if (value === null || value === undefined || value === '') return null;
    const num = parseFloat(value);
    return (!isNaN(num) && isFinite(num) && num >= 0) ? num.toFixed(2) : null;
  }

  /**
   * Sanitiza uma data
   * @param {any} value - Valor a ser sanitizado
   * @returns {string|null} - Data no formato YYYY-MM-DD ou null
   */
  sanitizeDateInput(value) {
    if (!value) return null;
    
    // Se já está no formato correto
    if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
      return value;
    }
    
    const date = new Date(value);
    return !isNaN(date.getTime()) ? date.toISOString().split('T')[0] : null;
  }

  /**
   * Sanitiza um valor enum
   * @param {string} value - Valor a ser validado
   * @param {Array<string>} allowedValues - Valores permitidos
   * @returns {string|null} - Valor validado ou null
   */
  sanitizeEnum(value, allowedValues) {
    if (!value) return null;
    const normalized = String(value).toUpperCase().trim();
    return allowedValues.includes(normalized) ? normalized : null;
  }

  /**
   * Verifica rate limiting antes de fazer requisição
   * @param {string} endpoint - Endpoint da requisição
   * @throws {Error} - Se exceder limite de requisições
   */
  checkRateLimit(endpoint) {
    const now = Date.now();
    const key = endpoint.split('?')[0]; // Ignora query params
    const requests = this.requestQueue.get(key) || [];
    
    // Limpar requests antigos (mais de 1 segundo)
    const recentRequests = requests.filter(t => now - t < 1000);
    
    if (recentRequests.length >= this.maxRequestsPerSecond) {
      throw new Error('Muitas requisições. Por favor, aguarde um momento.');
    }
    
    recentRequests.push(now);
    this.requestQueue.set(key, recentRequests);
  }

  // ============================================
  // MÉTODOS ESPECÍFICOS PARA ORDEM DE COMPRA
  // ============================================

  /**
   * Busca todas as ordens de compra
   * @param {Object} params - Parâmetros de busca (página, tamanho, filtros)
   * @returns {Promise<Array>} - Lista de ordens de compra
   */
  async getOrdensCompra(params = {}) {
    // Adiciona timestamp para evitar cache do navegador
    params._t = Date.now();
    const queryString = new URLSearchParams(params).toString();
    const endpoint = `/ordens-compra${queryString ? `?${queryString}` : ""}`;
    return await this.makeRequest(endpoint, { method: "GET" });
  }

  /**
   * Busca uma ordem de compra específica por ID
   * @param {number} id - ID da ordem de compra
   * @returns {Promise<Object>} - Dados da ordem de compra
   */
  async getOrdemCompra(id) {
    if (!id) {
      throw new Error("ID da ordem de compra é obrigatório");
    }
    return await this.makeRequest(`/ordens-compra/${id}`, { method: "GET" });
  }

  /**
   * Cria uma nova ordem de compra
   * @param {Object} ordemCompra - Dados da ordem de compra
   * @returns {Promise<Object>} - Ordem de compra criada
   */
  async createOrdemCompra(ordemCompra) {
    this.validateOrdemCompra(ordemCompra, true);
    const payload = this.sanitizeOrdemCompraPayload(ordemCompra, true);
    return await this.makeRequest("/ordens-compra", {
      method: "POST",
      body: payload,
    });
  }

  /**
   * Atualiza uma ordem de compra existente
   * @param {number} id - ID da ordem de compra
   * @param {Object} ordemCompra - Dados atualizados
   * @returns {Promise<Object>} - Ordem de compra atualizada
   */
  async updateOrdemCompra(id, ordemCompra) {
    if (!id) {
      throw new Error("ID da ordem de compra é obrigatório para atualização");
    }
    
    const payload = this.sanitizeOrdemCompraPayload(ordemCompra, false);
    
    // Garantir que o ID esteja presente no corpo para o backend validar
    if (payload.id === undefined || payload.id === null) {
      payload.id = id;
    }
    
    this.validateOrdemCompra(payload, false);
    
    const resultado = await this.makeRequest(`/ordens-compra/${id}`, {
      method: "PUT",
      body: payload,
    });
    
    return resultado;
  }

  /**
   * Remove uma ordem de compra
   * @param {number} id - ID da ordem de compra
   * @returns {Promise} - Confirmação da remoção
   */
  async deleteOrdemCompra(id) {
    if (!id) {
      throw new Error("ID da ordem de compra é obrigatório para remoção");
    }
    return await this.makeRequest(`/ordens-compra/${id}`, { method: "DELETE" });
  }

  /**
   * Remove uma ordem de compra com autenticação básica (login/senha)
   * @param {number} id - ID da ordem de compra
   * @param {{login:string, senha:string, motivo?:string}} credentials - Credenciais
   */
  async deleteOrdemCompraWithAuth(id, credentials = {}) {
    if (!id) throw new Error("ID da ordem de compra é obrigatório para remoção");

    const body = {
      login: credentials.login,
      senha: credentials.senha,
      motivo: credentials.motivo,
    };

    return await this.makeRequest(`/ordens-compra/${id}/authenticated`, {
      method: "DELETE",
      body,
    });
  }

  /**
   * Remove múltiplas ordens de compra
   * @param {Array<number>} ids - Array com IDs das ordens
   * @returns {Promise} - Resultado das remoções
   */
  async deleteMultipleOrdensCompra(ids) {
    if (!Array.isArray(ids) || ids.length === 0) {
      throw new Error("Array de IDs é obrigatório e não pode estar vazio");
    }

    // Executar remoções em paralelo com controle de concorrência
    const batchSize = 5; // Limitar a 5 requisições simultâneas
    const results = [];

    for (let i = 0; i < ids.length; i += batchSize) {
      const batch = ids.slice(i, i + batchSize);
      const batchPromises = batch.map((id) =>
        this.deleteOrdemCompra(id).catch((error) => ({ id, error }))
      );

      const batchResults = await Promise.all(batchPromises);
      results.push(...batchResults);
    }

    return results;
  }

  /**
   * Remove múltiplas ordens de compra com autenticação
   * @param {Array<number>} ids
   * @param {{login:string, senha:string, motivo?:string}} credentials
   */
  async deleteMultipleOrdensCompraWithAuth(ids, credentials = {}) {
    if (!Array.isArray(ids) || ids.length === 0) {
      throw new Error("Array de IDs é obrigatório e não pode estar vazio");
    }
    const batchSize = 5;
    const results = [];
    for (let i = 0; i < ids.length; i += batchSize) {
      const batch = ids.slice(i, i + batchSize);
      const batchPromises = batch.map((id) =>
        this.deleteOrdemCompraWithAuth(id, credentials).catch((error) => ({ id, error }))
      );
      const batchResults = await Promise.all(batchPromises);
      results.push(...batchResults);
    }
    return results;
  }

  /**
   * Valida os dados de uma ordem de compra
   * @param {Object} ordemCompra - Dados a serem validados
   * @param {boolean} isCreate - Se é uma criação (true) ou atualização (false)
   */
  validateOrdemCompra(ordemCompra, isCreate = true) {
    if (!ordemCompra || typeof ordemCompra !== "object") {
      throw new Error("Dados da ordem de compra são obrigatórios");
    }

    // Enum válido no backend: PEND, PROC, CONC, CANC
    const validStatuses = ["PEND", "PROC", "CONC", "CANC"];

    // Validar status (sempre obrigatório)
    if (!ordemCompra.statusOrdemCompra) {
      throw new Error("Status é obrigatório");
    }

    // Validar status
    if (!validStatuses.includes(ordemCompra.statusOrdemCompra)) {
      throw new Error(
        `Status deve ser um dos valores: ${validStatuses.join(", ")}`
      );
    }

    // Validar datas apenas na criação (em updates, aceitar null)
    if (isCreate) {
      if (!ordemCompra.dataPrev) {
        throw new Error("Data prevista é obrigatória");
      }
      if (!ordemCompra.dataOrdem) {
        throw new Error("Data da ordem é obrigatória");
      }
      this.validateDate(ordemCompra.dataPrev, "Data prevista");
      this.validateDate(ordemCompra.dataOrdem, "Data da ordem");
    } else {
      // Em updates, validar apenas se fornecidas
      if (ordemCompra.dataPrev) {
        this.validateDate(ordemCompra.dataPrev, "Data prevista");
      }
      if (ordemCompra.dataOrdem) {
        this.validateDate(ordemCompra.dataOrdem, "Data da ordem");
      }
    }

    // Data prevista não pode ser anterior à data da ordem (apenas se ambas existirem)
    if (ordemCompra.dataPrev && ordemCompra.dataOrdem) {
      const dPrev = new Date(ordemCompra.dataPrev);
      const dOrdem = new Date(ordemCompra.dataOrdem);
      if (!isNaN(dPrev.getTime()) && !isNaN(dOrdem.getTime()) && dPrev < dOrdem) {
        throw new Error("Data prevista não pode ser anterior à data da ordem");
      }
    }

    // Data da ordem não pode ser futura (regra do backend) - apenas se existir
    if (ordemCompra.dataOrdem) {
      const hoje = new Date();
      // normalizar somente a parte da data
      const hojeYMD = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
      const dOrdem = new Date(ordemCompra.dataOrdem);
      const dOrdemYMD = new Date(dOrdem.getFullYear(), dOrdem.getMonth(), dOrdem.getDate());
      if (!isNaN(dOrdemYMD.getTime()) && dOrdemYMD > hojeYMD) {
        throw new Error("Data da ordem não pode ser futura");
      }
    }

    if (ordemCompra.dataEntre) {
      this.validateDate(ordemCompra.dataEntre, "Data de entrega");
    }
  }

  /**
   * Normaliza e filtra o payload para o backend
   * - Remove campos desconhecidos
   * - Converte tipos
   * - Garante status válido
   * - Normaliza datas para YYYY-MM-DD
   */
  sanitizeOrdemCompraPayload(data, isCreate = true) {
    const onlyDate = (v) => {
      if (!v) {
        return null;
      }
      if (typeof v === "string" && /^\d{4}-\d{2}-\d{2}$/.test(v)) {
        return v;
      }
      const d = new Date(v);
      return isNaN(d.getTime()) ? null : d.toISOString().split("T")[0];
    };

    const statusMap = { ANDA: "PROC" }; // mapear valores legacy
    const status = (data.statusOrdemCompra || "").toUpperCase();
    const normalizedStatus = statusMap[status] || status;

    const payload = {
      statusOrdemCompra: normalizedStatus,
      dataPrev: onlyDate(data.dataPrev),
      dataOrdem: onlyDate(data.dataOrdem),
    };
    
    // Adicionar ID apenas se for edição
    if (!isCreate && data.id) {
      payload.id = data.id;
    }
    
    // Adicionar observações se houver
    if (data.observacoes) {
      payload.observacoes = data.observacoes;
    }

    // Remover apenas undefined E null (campos vazios não devem ser enviados)
    Object.keys(payload).forEach((k) => {
      if (payload[k] === undefined || payload[k] === null) {
        delete payload[k];
      }
    });

    return payload;
  }

  /**
   * Valida uma data
   * @param {string} dateString - String da data
   * @param {string} fieldName - Nome do campo para mensagem de erro
   */
  validateDate(dateString, fieldName) {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
      throw new Error(`${fieldName} deve ser uma data válida`);
    }
  }

  // ============================================
  // MÉTODOS UTILITÁRIOS
  // ============================================

  /**
   * Testa a conectividade com o backend
   * @returns {Promise<boolean>} - Status da conexão
   */
  async testConnection() {
    try {
      const response = await fetch(`${this.baseURL}/ordens-compra`, {
        method: "GET",
        headers: this.defaultHeaders,
      });

      if (!response.ok) {
        return false;
      }

      return true;
    } catch (error) {
      return false;
    }
  }

  /**
   * Obtém informações sobre a API
   * @returns {Promise<Object>} - Informações da API
   */
  async getApiInfo() {
    return await this.makeRequest("/info", { method: "GET" });
  }

  /**
   * Configura headers customizados
   * @param {Object} headers - Headers a serem adicionados
   */
  setCustomHeaders(headers) {
    this.defaultHeaders = { ...this.defaultHeaders, ...headers };
  }

  /**
   * Remove um header customizado
   * @param {string} headerName - Nome do header a ser removido
   */
  removeCustomHeader(headerName) {
    delete this.defaultHeaders[headerName];
  }

  /**
   * Altera a URL base da API
   * @param {string} newBaseURL - Nova URL base
   */
  setBaseURL(newBaseURL) {
    this.baseURL = newBaseURL.endsWith("/")
      ? newBaseURL.slice(0, -1)
      : newBaseURL;
  }

  /**
   * Adiciona itens a uma ordem de compra existente
   * @param {number} ordemId - ID da ordem de compra
   * @param {Array} itens - Array com os itens para adicionar
   * @returns {Promise<Object>} - Resposta da API
   */
  async adicionarItensOrdem(ordemId, itens) {
    if (!ordemId || !itens || !Array.isArray(itens) || itens.length === 0) {
      throw new Error("ID da ordem e itens são obrigatórios");
    }

    // Validar estrutura dos itens (apenas produto e quantidade)
    for (const item of itens) {
      if (!item.produtoId || !item.quantidade) {
        throw new Error(
          "Todos os itens devem ter produtoId e quantidade"
        );
      }
    }

    try {
      const response = await this.makeRequest(
        `/ordens-compra/${ordemId}/itens`,
        {
          method: "POST",
          body: itens,
        }
      );

      return response;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Busca itens de uma ordem de compra
   * @param {number} ordemId - ID da ordem de compra
   * @returns {Promise<Array>} - Lista de itens
   */
  async getItensOrdem(ordemId) {
    if (!ordemId) {
      throw new Error("ID da ordem é obrigatório");
    }

    try {
      const response = await this.makeRequest(
        `/ordens-compra/${ordemId}/itens`,
        {
          method: "GET",
        }
      );

      // Garantir que sempre retornamos um array
      if (!response) {
        return [];
      }

      if (Array.isArray(response)) {
        return response;
      }

      // Se a resposta tem propriedade 'data' que é um array
      if (response.data && Array.isArray(response.data)) {
        return response.data;
      }

      // Se a resposta tem propriedade 'content' que é um array (paginação Spring Boot)
      if (response.content && Array.isArray(response.content)) {
        return response.content;
      }

      // Se a resposta segue o formato { success: true, itens: [...] }
      if (response.itens && Array.isArray(response.itens)) {
        return response.itens;
      }

      // Se chegou aqui, a resposta não é um array, retornar vazio
      return [];
    } catch (error) {
      return []; // Retornar array vazio em caso de erro ao invés de throw
    }
  }

  /**
   * Busca itens de uma ordem de compra (alias para compatibilidade)
   * @param {number} ordemId - ID da ordem de compra
   * @returns {Promise<Array>} - Lista de itens
   */
  async getItensOrdemCompra(ordemId) {
    return await this.getItensOrdem(ordemId);
  }

  /**
   * Atualiza um item específico de uma ordem
   * @param {number} ordemId - ID da ordem de compra
   * @param {number} itemId - ID do item
   * @param {Object} dadosItem - Dados atualizados do item
   * @returns {Promise<Object>} - Item atualizado
   */
  async atualizarItemOrdem(ordemId, itemId, dadosItem) {
    if (!ordemId || !itemId || !dadosItem) {
      throw new Error("ID da ordem, ID do item e dados são obrigatórios");
    }

    try {
      const response = await this.makeRequest(
        `/ordens-compra/${ordemId}/itens/${itemId}`,
        {
          method: "PUT",
          body: dadosItem,
        }
      );

      return response;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Remove um item de uma ordem de compra
   * @param {number} ordemId - ID da ordem de compra
   * @param {number} itemId - ID do item
   * @returns {Promise<Object>} - Resposta da API
   */
  async removerItemOrdem(ordemId, itemId) {
    if (!ordemId || !itemId) {
      throw new Error("ID da ordem e ID do item são obrigatórios");
    }

    try {
      const response = await this.makeRequest(
        `/ordens-compra/${ordemId}/itens/${itemId}`,
        {
          method: "DELETE",
        }
      );

      return response;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Cria uma nova ordem de compra com itens em sequência
   * @param {Object} dadosOrdem - Dados da ordem de compra (sem itens)
   * @param {Array} itens - Array com os itens para adicionar
   * @returns {Promise<Object>} - Ordem criada com itens
   */
  async criarOrdemComItens(dadosOrdem, itens = []) {
    try {
      // 1. Primeiro criar a ordem de compra
      const ordemCriada = await this.createOrdemCompra(dadosOrdem);

      // 2. Se há itens, adicioná-los à ordem criada
      if (itens && itens.length > 0) {
        const itensAdicionados = await this.adicionarItensOrdem(
          ordemCriada.id,
          itens
        );

        // 3. Buscar a ordem atualizada com valores recalculados
        const ordemAtualizada = await this.getOrdemCompra(ordemCriada.id);
        return ordemAtualizada;
      }

      return ordemCriada;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Busca a lista de produtos
   * @returns {Promise<Array>} Lista de produtos
   */
  async getProdutos() {
    try {
      const response = await this.makeRequest("/produtos", { method: "GET" });
      // Normalizar para array
      if (!response) return [];
      if (Array.isArray(response)) return response;
      if (response.data && Array.isArray(response.data)) return response.data;
      if (response.content && Array.isArray(response.content))
        return response.content;
      return [];
    } catch (error) {
      return [];
    }
  }

  /**
   * Busca produtos que precisam de reposição
   * Caso o endpoint específico não exista, retorna todos os produtos e deixa para o cliente decidir.
   * @returns {Promise<Array>}
   */
  async getProdutosParaReposicao() {
    try {
      // Tenta um endpoint específico, se existir
      try {
        const resp = await this.makeRequest("/produtos/reposicao", {
          method: "GET",
        });
        if (Array.isArray(resp)) return resp;
        if (resp?.data && Array.isArray(resp.data)) return resp.data;
        if (resp?.content && Array.isArray(resp.content)) return resp.content;
      } catch (e) {
        // Silencia e cai no fallback
      }
      // Fallback: retorna todos e o cliente filtra
      return await this.getProdutos();
    } catch (error) {
      return [];
    }
  }

  // ============================================
  // MÉTODOS PARA ORDENS AUTOMÁTICAS
  // ============================================

  /**
   * Obtém estatísticas do sistema de ordens automáticas
   * @returns {Promise<Object>} - Estatísticas do sistema
   */
  async getOrdemAutomaticaStats() {
    try {
      return await this.makeRequest("/ordens-automaticas/estatisticas", {
        method: "GET",
      });
    } catch (error) {
      console.error("[ApiManager] Erro ao buscar estatísticas automáticas:", error);
      return {
        servicoAtivo: false,
        produtosCriticos: 0,
        error: error.message
      };
    }
  }

  /**
   * Força verificação manual de produtos para reposição
   * @returns {Promise<Object>} - Resultado da verificação
   */
  async forcarVerificacaoReposicao() {
    try {
      return await this.makeRequest("/ordens-automaticas/verificar", {
        method: "POST",
      });
    } catch (error) {
      console.error("[ApiManager] Erro ao forçar verificação:", error);
      throw error;
    }
  }

  /**
   * Obtém status do serviço de ordens automáticas
   * @returns {Promise<Object>} - Status do serviço
   */
  async getOrdemAutomaticaStatus() {
    try {
      return await this.makeRequest("/ordens-automaticas/status", {
        method: "GET",
      });
    } catch (error) {
      console.error("[ApiManager] Erro ao buscar status:", error);
      return {
        status: "offline",
        error: error.message
      };
    }
  }

  /**
   * Busca produtos com estoque crítico
   * @returns {Promise<Array>} - Lista de produtos críticos
   */
  async getProdutosEstoqueCritico() {
    try {
      const resp = await this.makeRequest("/produtos/estoque-critico", {
        method: "GET",
      });
      if (Array.isArray(resp)) return resp;
      if (resp?.data && Array.isArray(resp.data)) return resp.data;
      if (resp?.content && Array.isArray(resp.content)) return resp.content;
      return [];
    } catch (error) {
      console.error("[ApiManager] Erro ao buscar produtos críticos:", error);
      return [];
    }
  }

  /**
   * Busca produtos com estoque baixo
   * @returns {Promise<Array>} - Lista de produtos com estoque baixo
   */
  async getProdutosEstoqueBaixo() {
    try {
      const resp = await this.makeRequest("/produtos/estoque-baixo", {
        method: "GET",
      });
      if (Array.isArray(resp)) return resp;
      if (resp?.data && Array.isArray(resp.data)) return resp.data;
      if (resp?.content && Array.isArray(resp.content)) return resp.content;
      return [];
    } catch (error) {
      console.error("[ApiManager] Erro ao buscar produtos com estoque baixo:", error);
      return [];
    }
  }
}

// Criar instância global do ApiManager
const apiManager = new ApiManager();

// Disponibilizar globalmente
window.apiManager = apiManager;

// Configurar headers de autenticação se necessário
// apiManager.setCustomHeaders({ 'Authorization': 'Bearer YOUR_TOKEN' });

// Exportar para uso em outros módulos
if (typeof module !== "undefined" && module.exports) {
  module.exports = ApiManager;
}
