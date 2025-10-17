/**
 * ApiManager - Gerenciador de APIs para Sistema Fasiclin
 * Classe responsável por todas as comunicações HTTP com o backend
 * Implementa padrões REST e tratamento de erros consistente
 */
class ApiManager {
  constructor() {
    // Configuração base da API
    this.baseURL = "http://localhost:8080/api"; // Ajustar conforme configuração do backend
    this.defaultHeaders = {
      "Content-Type": "application/json",
      Accept: "application/json",
    };

    // Configurações de timeout e retry
    this.timeout = 30000; // 30 segundos
    this.maxRetries = 3;
    this.retryDelay = 1000; // 1 segundo
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
   * Método genérico para fazer requisições HTTP
   * @param {string} endpoint - Endpoint da API
   * @param {Object} options - Opções da requisição
   * @returns {Promise} - Resposta da API
   */
  async makeRequest(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      method: options.method || "GET",
      headers: {
        ...this.defaultHeaders,
        ...options.headers,
      },
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

    // Implementação de retry
    for (let attempt = 1; attempt <= this.maxRetries; attempt++) {
      try {
        console.log(
          `[ApiManager] ${config.method} ${url} (Tentativa ${attempt})`
        );

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), this.timeout);

        const response = await fetch(url, {
          ...config,
          signal: controller.signal,
        });

        clearTimeout(timeoutId);

        // Verificar se a resposta é válida
        if (!response.ok) {
          // Tentar obter mensagem detalhada do backend
          let serverMessage = "";
          try {
            const ct = response.headers.get("content-type");
            if (ct && ct.includes("application/json")) {
              const body = await response.json();
              // procurar campos comuns de erro
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

        // Tentar parsear JSON, se não conseguir retornar texto
        const contentType = response.headers.get("content-type");
        let data;

        if (contentType && contentType.includes("application/json")) {
          data = await response.json();
        } else {
          data = await response.text();
        }

        console.log(`[ApiManager] Sucesso: ${config.method} ${url}`, data);
        

        
        return data;
      } catch (error) {
        lastError = error;
        console.error(`[ApiManager] Erro na tentativa ${attempt}:`, error);

        // Se não é a última tentativa e é um erro de rede, aguardar antes de tentar novamente
        if (attempt < this.maxRetries && this.isRetryableError(error)) {
          console.log(
            `[ApiManager] Aguardando ${this.retryDelay}ms antes da próxima tentativa...`
          );
          await this.delay(this.retryDelay * attempt); // Backoff exponencial
        } else {
          break;
        }
      }
    }

    // Se chegou aqui, todas as tentativas falharam
    console.error(
      `[ApiManager] Todas as tentativas falharam para ${config.method} ${url}`
    );
    throw this.createApiError(lastError, endpoint, config.method);
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

  /**
   * Utilitário para delay
   * @param {number} ms - Milissegundos para aguardar
   * @returns {Promise} - Promise que resolve após o delay
   */
  delay(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
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
    const payload = this.sanitizeOrdemCompraPayload(ordemCompra, true);
    console.log("🚀 [createOrdemCompra] Payload que será enviado:", payload);
    this.validateOrdemCompra(payload);
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
    console.log("📦 [sanitizeOrdemCompraPayload] Dados recebidos:", data);
    console.log("📦 [sanitizeOrdemCompraPayload] isCreate:", isCreate);
    
    const onlyDate = (v) => {
      console.log("🔍 [onlyDate] Processando:", v, "tipo:", typeof v);
      if (!v) {
        console.log("⚠️ [onlyDate] Valor vazio, retornando null");
        return null;
      }
      if (typeof v === "string" && /^\d{4}-\d{2}-\d{2}$/.test(v)) {
        console.log("✅ [onlyDate] Já está em formato YYYY-MM-DD:", v);
        return v;
      }
      const d = new Date(v);
      const resultado = isNaN(d.getTime()) ? null : d.toISOString().split("T")[0];
      console.log("🔄 [onlyDate] Convertido para:", resultado);
      return resultado;
    };

    const statusMap = { ANDA: "PROC" }; // mapear valores legacy
    const status = (data.statusOrdemCompra || "").toUpperCase();
    const normalizedStatus = statusMap[status] || status;

    const payload = {
      statusOrdemCompra: normalizedStatus,
      dataPrev: onlyDate(data.dataPrev),
      dataOrdem: onlyDate(data.dataOrdem),
    };
    
    console.log("📋 [sanitizeOrdemCompraPayload] Payload ANTES de filtrar:", payload);
    
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
        console.log(`⚠️ Removendo campo ${k} (valor: ${payload[k]})`);
        delete payload[k];
      }
    });

    console.log("✅ [sanitizeOrdemCompraPayload] Payload sanitizado FINAL:", payload);
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
      // Tentar endpoint de ordens primeiro, depois health
      await this.makeRequest("/ordens-compra", { method: "GET" });
      return true;
    } catch (error) {
      try {
        // Fallback para endpoint de health
        await this.makeRequest("/health", { method: "GET" });
        return true;
      } catch (healthError) {
        console.error("[ApiManager] Teste de conexão falhou:", error);
        return false;
      }
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
    console.log("[ApiManager] Adicionando itens à ordem:", { ordemId, itens });

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

      console.log("[ApiManager] Itens adicionados com sucesso:", response);
      return response;
    } catch (error) {
      console.error("[ApiManager] Erro ao adicionar itens:", error);
      throw error;
    }
  }

  /**
   * Busca itens de uma ordem de compra
   * @param {number} ordemId - ID da ordem de compra
   * @returns {Promise<Array>} - Lista de itens
   */
  async getItensOrdem(ordemId) {
    console.log("[ApiManager] Buscando itens da ordem:", ordemId);

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

      console.log("[ApiManager] Itens encontrados:", response);

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
      console.warn("[ApiManager] Resposta não é um array válido:", response);
      return [];
    } catch (error) {
      console.error("[ApiManager] Erro ao buscar itens:", error);
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
    console.log("[ApiManager] Atualizando item:", {
      ordemId,
      itemId,
      dadosItem,
    });

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

      console.log("[ApiManager] Item atualizado:", response);
      return response;
    } catch (error) {
      console.error("[ApiManager] Erro ao atualizar item:", error);
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
    console.log("[ApiManager] Removendo item:", { ordemId, itemId });

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

      console.log("[ApiManager] Item removido:", response);
      return response;
    } catch (error) {
      console.error("[ApiManager] Erro ao remover item:", error);
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
    console.log("[ApiManager] Criando ordem completa com itens:", {
      dadosOrdem,
      itens,
    });

    try {
      // 1. Primeiro criar a ordem de compra
      const ordemCriada = await this.createOrdemCompra(dadosOrdem);
      console.log("[ApiManager] Ordem criada:", ordemCriada);

      // 2. Se há itens, adicioná-los à ordem criada
      if (itens && itens.length > 0) {
        console.log("[ApiManager] Adicionando itens à ordem:", itens);
        const itensAdicionados = await this.adicionarItensOrdem(
          ordemCriada.id,
          itens
        );
        console.log("[ApiManager] Itens adicionados:", itensAdicionados);

        // 3. Buscar a ordem atualizada com valores recalculados
        const ordemAtualizada = await this.getOrdemCompra(ordemCriada.id);
        return ordemAtualizada;
      }

      return ordemCriada;
    } catch (error) {
      console.error("[ApiManager] Erro ao criar ordem com itens:", error);
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
      console.warn(
        "[ApiManager] Resposta de produtos não é um array:",
        response
      );
      return [];
    } catch (error) {
      console.error("[ApiManager] Erro ao buscar produtos:", error);
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
      console.error(
        "[ApiManager] Erro ao buscar produtos para reposição:",
        error
      );
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

console.log("[ApiManager] Inicializado com sucesso");
