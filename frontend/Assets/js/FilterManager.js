/**
 * FilterManager - Gerenciador de Filtros para Ordens de Compra
 * Implementa filtros avançados e ordenação baseados nos endpoints da API
 * Inclui sanitização de dados e validação de segurança
 */
class FilterManager {
  constructor(ordemCompraManager) {
    this.ordemCompraManager = ordemCompraManager;
    this.currentFilters = {
      status: "",
      valorMin: "",
      valorMax: "",
      dataInicio: "",
      dataFim: "",
    };
    this.currentSort = {
      field: "id",
      direction: "asc",
    };
    this.searchTerm = "";
    this.searchTimeout = null;
    
    // Valores válidos para enums
    this.validStatuses = ["PEND", "PROC", "CONC", "CANC"];
    this.validSortFields = ["id", "valor", "dataOrdem", "dataPrev", "statusOrdemCompra"];
    this.validSortDirections = ["asc", "desc"];

    this.init();
  }

  /**
   * Inicializa o FilterManager
   */
  init() {
    // Aguardar ApiManager estar disponível
    if (!window.apiManager) {
      setTimeout(() => this.init(), 100);
      return;
    }

    this.setupEventListeners();
    this.setupDateDefaults();
  }

  /**
   * Configura valores padrão para filtros de data
   */
  setupDateDefaults() {
    const hoje = new Date();
    const inicioMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
    const fimMes = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0);

    // Não aplicar filtros por padrão, apenas manter os campos vazios
    // para que o usuário possa escolher o período que desejar
  }

  /**
   * Configura os event listeners dos filtros
   */
  setupEventListeners() {
    // Botão aplicar filtros
    const btnAplicarFiltros = document.getElementById("btnAplicarFiltros");
    if (btnAplicarFiltros) {
      btnAplicarFiltros.addEventListener("click", () => {
        this.applyFilters();
      });
    }

    // Botão limpar filtros
    const btnLimparFiltros = document.getElementById("btnLimparFiltros");
    if (btnLimparFiltros) {
      btnLimparFiltros.addEventListener("click", () => {
        this.clearFilters();
      });
    }

    // Enter nos campos de input
    const filterInputs = document.querySelectorAll(
      ".filter-input, .filter-select"
    );
    filterInputs.forEach((input) => {
      input.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
          this.applyFilters();
        }
      });
    });

    // Ordenação da tabela
    const sortableHeaders = document.querySelectorAll(".sortable");
    sortableHeaders.forEach((header) => {
      header.addEventListener("click", () => {
        const field = header.getAttribute("data-sort");
        this.handleSort(field);
      });
    });

    // Busca
    const searchInput = document.getElementById("searchInput");
    if (searchInput) {
      searchInput.addEventListener("input", (e) => {
        this.handleSearch(e.target.value);
      });

      searchInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
          this.handleSearch(e.target.value, true);
        }
      });
    }

    // Botão limpar busca
    const clearSearch = document.getElementById("clearSearch");
    if (clearSearch) {
      clearSearch.addEventListener("click", () => {
        this.clearSearch();
      });
    }
  }

  /**
   * Aplica os filtros selecionados
   */
  async applyFilters() {
    try {
      // Verificar se ApiManager está disponível
      if (!window.apiManager) {
        return;
      }

      // Coleta e sanitiza os valores dos filtros
      const rawFilters = {
        status: document.getElementById("filterStatus")?.value || "",
        valorMin: document.getElementById("filterValorMin")?.value || "",
        valorMax: document.getElementById("filterValorMax")?.value || "",
        dataInicio: document.getElementById("filterDataInicio")?.value || "",
        dataFim: document.getElementById("filterDataFim")?.value || "",
      };

      // Sanitizar todos os valores
      this.currentFilters = this.sanitizeFilters(rawFilters);

      // Valida os filtros
      if (!this.validateFilters()) {
        return;
      }

      // Aplica os filtros via API
      await this.loadFilteredData();
    } catch (error) {
      console.error("[FilterManager] Erro ao aplicar filtros:", error);
      if (typeof notify !== 'undefined') {
        notify.error("Erro ao aplicar filtros. Tente novamente.");
      }
    }
  }

  /**
   * Sanitiza os valores dos filtros para segurança
   * @param {Object} filters - Filtros a serem sanitizados
   * @returns {Object} - Filtros sanitizados
   */
  sanitizeFilters(filters) {
    return {
      status: this.sanitizeEnum(filters.status, this.validStatuses),
      valorMin: this.sanitizeDecimal(filters.valorMin),
      valorMax: this.sanitizeDecimal(filters.valorMax),
      dataInicio: this.sanitizeDate(filters.dataInicio),
      dataFim: this.sanitizeDate(filters.dataFim),
    };
  }

  /**
   * Sanitiza um valor enum
   * @param {string} value - Valor a ser validado
   * @param {Array<string>} allowedValues - Valores permitidos
   * @returns {string} - Valor validado ou string vazia
   */
  sanitizeEnum(value, allowedValues) {
    if (!value || typeof value !== 'string') return "";
    const normalized = value.toUpperCase().trim();
    return allowedValues.includes(normalized) ? normalized : "";
  }

  /**
   * Sanitiza um valor decimal
   * @param {string} value - Valor a ser sanitizado
   * @returns {string} - Valor decimal válido ou string vazia
   */
  sanitizeDecimal(value) {
    if (!value || value === "") return "";
    
    // Remove caracteres não numéricos exceto ponto e vírgula
    const cleaned = String(value).replace(/[^\d.,]/g, '').replace(',', '.');
    const num = parseFloat(cleaned);
    
    if (isNaN(num) || !isFinite(num) || num < 0) return "";
    if (num > 999999999) return "999999999"; // Limite máximo razoável
    
    return num.toFixed(2);
  }

  /**
   * Sanitiza uma data
   * @param {string} value - Valor a ser sanitizado
   * @returns {string} - Data no formato YYYY-MM-DD ou string vazia
   */
  sanitizeDate(value) {
    if (!value || value === "") return "";
    
    // Verifica se já está no formato correto
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
      const date = new Date(value);
      if (!isNaN(date.getTime())) {
        // Validar que a data não é muito antiga ou futura demais
        const minDate = new Date('2000-01-01');
        const maxDate = new Date('2100-12-31');
        if (date >= minDate && date <= maxDate) {
          return value;
        }
      }
    }
    
    return "";
  }

  /**
   * Sanitiza o termo de busca
   * @param {string} term - Termo de busca
   * @returns {string} - Termo sanitizado
   */
  sanitizeSearchTerm(term) {
    if (!term || typeof term !== 'string') return "";
    
    return term
      .replace(/<[^>]*>/g, '') // Remove tags HTML
      .replace(/[<>'";&]/g, '') // Remove caracteres perigosos
      .trim()
      .slice(0, 100); // Limita tamanho máximo
  }

  /**
   * Valida os filtros antes de aplicar
   */
  validateFilters() {
    const { valorMin, valorMax, dataInicio, dataFim } = this.currentFilters;

    // Validação de valores
    if (valorMin && valorMax && parseFloat(valorMin) > parseFloat(valorMax)) {
      return false;
    }

    // Validação de datas
    if (dataInicio && dataFim && new Date(dataInicio) > new Date(dataFim)) {
      return false;
    }

    return true;
  }

  /**
   * Carrega dados filtrados da API
   */
  async loadFilteredData() {
    const { status, valorMin, valorMax, dataInicio, dataFim } =
      this.currentFilters;
    let endpoint = "/ordens-compra";
    let ordensCompra = [];

    try {
      // Verificar se o ApiManager está disponível
      if (!window.apiManager) {
        return;
      }

      // Aplica filtros específicos baseados nos endpoints disponíveis
      if (status) {
        endpoint = `/ordens-compra/status/${status}`;
        try {
          ordensCompra = await window.apiManager.makeRequest(endpoint, {
            method: "GET",
          });
        } catch (_) {
          // Fallback: carrega todos e filtra no cliente
          const todas = await window.apiManager.getOrdensCompra();
          ordensCompra = Array.isArray(todas)
            ? todas.filter(
                (o) => String(o.statusOrdemCompra) === String(status)
              )
            : [];
        }
      } else if (valorMin || valorMax) {
        const min = valorMin || 0;
        const max = valorMax || 999999.99;
        endpoint = `/ordens-compra/valor?valorMin=${min}&valorMax=${max}`;
        try {
          ordensCompra = await window.apiManager.makeRequest(endpoint, {
            method: "GET",
          });
        } catch (_) {
          const todas = await window.apiManager.getOrdensCompra();
          ordensCompra = Array.isArray(todas)
            ? todas.filter(
                (o) =>
                  parseFloat(o.valor || 0) >= parseFloat(min) &&
                  parseFloat(o.valor || 0) <= parseFloat(max)
              )
            : [];
        }
      } else if (dataInicio && dataFim) {
        endpoint = `/ordens-compra/periodo?dataInicio=${dataInicio}&dataFim=${dataFim}`;
        try {
          ordensCompra = await window.apiManager.makeRequest(endpoint, {
            method: "GET",
          });
        } catch (_) {
          const todas = await window.apiManager.getOrdensCompra();
          ordensCompra = Array.isArray(todas)
            ? todas.filter((o) => {
                const data = new Date(o.dataOrdem);
                return (
                  data >= new Date(dataInicio) && data <= new Date(dataFim)
                );
              })
            : [];
        }
      } else {
        // Sem filtros específicos, carrega todos
        ordensCompra = await window.apiManager.getOrdensCompra();
      }

      // Aplica filtros adicionais no frontend se necessário
      ordensCompra = this.applyClientSideFilters(ordensCompra);

      // Aplica ordenação
      ordensCompra = this.applySorting(ordensCompra);

      // Atualiza a tabela
      this.ordemCompraManager.ordensCompra = ordensCompra;
      this.ordemCompraManager.componentManager.renderTable(ordensCompra);
      this.ordemCompraManager.componentManager.updatePaginationInfo(
        ordensCompra.length
      );
    } catch (error) {
      throw error;
    }
  }

  /**
   * Aplica filtros adicionais no lado do cliente
   */
  applyClientSideFilters(ordensCompra) {
    const { valorMin, valorMax, dataInicio, dataFim } = this.currentFilters;

    return ordensCompra.filter((ordem) => {
      // Filtro de valor (se não foi aplicado no backend)
      if (valorMin && parseFloat(ordem.valor) < parseFloat(valorMin)) {
        return false;
      }
      if (valorMax && parseFloat(ordem.valor) > parseFloat(valorMax)) {
        return false;
      }

      // Filtro de data (se não foi aplicado no backend)
      if (dataInicio) {
        const dataOrdem = new Date(ordem.dataOrdem);
        const dataFiltroInicio = new Date(dataInicio);
        if (dataOrdem < dataFiltroInicio) {
          return false;
        }
      }
      if (dataFim) {
        const dataOrdem = new Date(ordem.dataOrdem);
        const dataFiltroFim = new Date(dataFim);
        if (dataOrdem > dataFiltroFim) {
          return false;
        }
      }

      return true;
    });
  }

  /**
   * Aplica ordenação aos dados
   */
  applySorting(ordensCompra) {
    const { field, direction } = this.currentSort;

    return ordensCompra.sort((a, b) => {
      let valueA = a[field];
      let valueB = b[field];

      // Tratamento especial para diferentes tipos de dados
      if (field.includes("data") || field.includes("Data")) {
        valueA = new Date(valueA);
        valueB = new Date(valueB);
      } else if (field === "valor") {
        valueA = parseFloat(valueA);
        valueB = parseFloat(valueB);
      } else if (typeof valueA === "string") {
        valueA = valueA.toLowerCase();
        valueB = valueB.toLowerCase();
      }

      if (direction === "asc") {
        return valueA < valueB ? -1 : valueA > valueB ? 1 : 0;
      } else {
        return valueA > valueB ? -1 : valueA < valueB ? 1 : 0;
      }
    });
  }

  /**
   * Manipula a ordenação da tabela
   */
  handleSort(field) {
    // Validar se o campo é permitido
    if (!this.validSortFields.includes(field)) {
      console.warn(`[FilterManager] Campo de ordenação inválido: ${field}`);
      return;
    }

    // Se é o mesmo campo, inverte a direção
    if (this.currentSort.field === field) {
      this.currentSort.direction =
        this.currentSort.direction === "asc" ? "desc" : "asc";
    } else {
      this.currentSort.field = field;
      this.currentSort.direction = "asc";
    }

    // Garantir que a direção é válida
    if (!this.validSortDirections.includes(this.currentSort.direction)) {
      this.currentSort.direction = "asc";
    }

    // Atualiza os ícones de ordenação
    this.updateSortIcons();

    // Aplica os filtros novamente com a nova ordenação
    this.applyFilters();
  }

  /**
   * Atualiza os ícones de ordenação na tabela
   */
  updateSortIcons() {
    // Remove classes de ordenação de todos os headers
    const sortableHeaders = document.querySelectorAll(".sortable");
    sortableHeaders.forEach((header) => {
      header.classList.remove("sorted", "desc");
      const icon = header.querySelector(".sort-icon");
      if (icon) {
        icon.style.transform = "";
        icon.style.opacity = "0.5";
      }
    });

    // Adiciona classe ao header ativo
    const activeHeader = document.querySelector(
      `[data-sort="${this.currentSort.field}"]`
    );
    if (activeHeader) {
      activeHeader.classList.add("sorted");
      if (this.currentSort.direction === "desc") {
        activeHeader.classList.add("desc");
      }

      const icon = activeHeader.querySelector(".sort-icon");
      if (icon) {
        icon.style.opacity = "1";
        icon.style.transform =
          this.currentSort.direction === "desc" ? "rotate(180deg)" : "";
      }
    }
  }

  /**
   * Limpa todos os filtros
   */
  async clearFilters() {
    try {
      // Limpa os campos do formulário
      document.getElementById("filterStatus").value = "";
      document.getElementById("filterValorMin").value = "";
      document.getElementById("filterValorMax").value = "";
      document.getElementById("filterDataInicio").value = "";
      document.getElementById("filterDataFim").value = "";

      // Reseta os filtros internos
      this.currentFilters = {
        status: "",
        valorMin: "",
        valorMax: "",
        dataInicio: "",
        dataFim: "",
      };

      // Reseta a ordenação para o padrão
      this.currentSort = {
        field: "id",
        direction: "asc",
      };

      // Recarrega todos os dados
      await this.ordemCompraManager.loadOrdens();

      // Atualiza os ícones de ordenação
      this.updateSortIcons();

      notify.success("Filtros removidos com sucesso!");
    } catch (error) {
      notify.error("Erro ao limpar filtros. Tente novamente.");
    }
  }

  /**
   * Obtém o estado atual dos filtros
   */
  getFiltersState() {
    return {
      filters: { ...this.currentFilters },
      sort: { ...this.currentSort },
    };
  }

  /**
   * Lida com a busca
   * @param {string} searchValue - Termo de busca
   * @param {boolean} immediate - Se deve executar imediatamente
   */
  handleSearch(searchValue, immediate = false) {
    // Limpar timeout anterior
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }

    // Sanitizar e atualizar termo de busca
    this.searchTerm = this.sanitizeSearchTerm(searchValue);

    // Mostrar/ocultar botão limpar
    const clearButton = document.getElementById("clearSearch");
    if (clearButton) {
      clearButton.style.display = this.searchTerm ? "flex" : "none";
    }

    // Aplicar busca com debounce ou imediatamente
    if (immediate || this.searchTerm.length === 0) {
      this.executeSearch();
    } else {
      this.searchTimeout = setTimeout(() => {
        this.executeSearch();
      }, 300); // 300ms de debounce
    }
  }

  /**
   * Executa a busca
   */
  executeSearch() {
    if (!this.ordemCompraManager) {
      return;
    }

    // Filtrar ordens baseado no termo de busca
    const allOrdens = this.ordemCompraManager.ordensCompra;
    let filteredOrdens = allOrdens;

    if (this.searchTerm) {
      const searchLower = this.searchTerm.toLowerCase();

      filteredOrdens = allOrdens.filter((ordem) => {
        // Buscar por ID
        if (ordem.id && ordem.id.toString().includes(searchLower)) {
          return true;
        }

        // Buscar por Status
        if (
          ordem.statusOrdemCompra &&
          ordem.statusOrdemCompra.toLowerCase().includes(searchLower)
        ) {
          return true;
        }

        // Buscar por Valor
        if (ordem.valor) {
          const valorFormatado = parseFloat(ordem.valor).toLocaleString(
            "pt-BR",
            {
              minimumFractionDigits: 2,
            }
          );
          if (
            valorFormatado.includes(searchLower) ||
            ordem.valor.toString().includes(searchLower)
          ) {
            return true;
          }
        }

        // Buscar por Data Prevista
        if (ordem.dataPrev) {
          const dataPrevFormatada = new Date(ordem.dataPrev).toLocaleDateString(
            "pt-BR"
          );
          if (dataPrevFormatada.includes(searchLower)) {
            return true;
          }
        }

        // Buscar por Data da Ordem
        if (ordem.dataOrdem) {
          const dataOrdemFormatada = new Date(
            ordem.dataOrdem
          ).toLocaleDateString("pt-BR");
          if (dataOrdemFormatada.includes(searchLower)) {
            return true;
          }
        }

        // Buscar por Data de Entrega
        if (ordem.dataEntre) {
          const dataEntregaFormatada = new Date(
            ordem.dataEntre
          ).toLocaleDateString("pt-BR");
          if (dataEntregaFormatada.includes(searchLower)) {
            return true;
          }
        }

        return false;
      });
    }

    // Atualizar a tabela com os resultados filtrados (resetar para a primeira página)
    if (this.ordemCompraManager.componentManager) {
      this.ordemCompraManager.componentManager.currentPage = 1;
      this.ordemCompraManager.componentManager.renderTable(filteredOrdens);
      this.ordemCompraManager.componentManager.updatePaginationInfo(
        filteredOrdens.length
      );
    }

    // Atualizar contador de resultados
    this.updateSearchResults(filteredOrdens.length, allOrdens.length);
  }

  /**
   * Limpa a busca
   */
  clearSearch() {
    const searchInput = document.getElementById("searchInput");
    const clearButton = document.getElementById("clearSearch");

    if (searchInput) {
      searchInput.value = "";
    }

    if (clearButton) {
      clearButton.style.display = "none";
    }

    this.searchTerm = "";

    // Restaurar todas as ordens
    if (this.ordemCompraManager?.componentManager) {
      this.ordemCompraManager.componentManager.currentPage = 1;
      this.ordemCompraManager.componentManager.renderTable(
        this.ordemCompraManager.ordensCompra
      );
      this.ordemCompraManager.componentManager.updatePaginationInfo(
        this.ordemCompraManager.ordensCompra.length
      );
    }

    // Focar no input
    if (searchInput) {
      searchInput.focus();
    }
  }

  /**
   * Atualiza o contador de resultados da busca
   * @param {number} filteredCount - Número de resultados filtrados
   * @param {number} totalCount - Número total de ordens
   */
  updateSearchResults(filteredCount, totalCount) {
    // Criar ou atualizar indicador de resultados
    let resultsIndicator = document.getElementById("searchResults");

    if (!resultsIndicator) {
      resultsIndicator = document.createElement("div");
      resultsIndicator.id = "searchResults";
      resultsIndicator.className = "search-results";

      const searchContainer = document.querySelector(".search-container");
      if (searchContainer) {
        searchContainer.appendChild(resultsIndicator);
      }
    }

    if (this.searchTerm && filteredCount !== totalCount) {
      resultsIndicator.textContent = `${filteredCount} de ${totalCount} ordens`;
      resultsIndicator.style.display = "block";
    } else {
      resultsIndicator.style.display = "none";
    }
  }

  /**
   * Aplica filtros rápidos (para uso em outros componentes)
   */
  async applyQuickFilter(filterType, value) {
    switch (filterType) {
      case "status":
        document.getElementById("filterStatus").value = value;
        break;
      case "today":
        const today = new Date().toISOString().split("T")[0];
        document.getElementById("filterDataInicio").value = today;
        document.getElementById("filterDataFim").value = today;
        break;
      case "thisWeek":
        const startOfWeek = new Date();
        startOfWeek.setDate(startOfWeek.getDate() - startOfWeek.getDay());
        const endOfWeek = new Date(startOfWeek);
        endOfWeek.setDate(endOfWeek.getDate() + 6);

        document.getElementById("filterDataInicio").value = startOfWeek
          .toISOString()
          .split("T")[0];
        document.getElementById("filterDataFim").value = endOfWeek
          .toISOString()
          .split("T")[0];
        break;
    }

    await this.applyFilters();
  }
}

// Exporta a classe para uso global
window.FilterManager = FilterManager;
