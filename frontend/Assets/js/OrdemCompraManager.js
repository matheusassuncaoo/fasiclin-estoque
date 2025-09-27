/**
 * OrdemCompraManager - Gerenciador Principal
 * Coordena todas as operações CRUD e integra os outros managers
 * Implementa o padrão MVC para a página de Ordem de Compra
 */
class OrdemCompraManager {
  constructor() {
    this.ordensCompra = [];
    this.currentOrdem = null;
    this.isLoading = false;
    this.cache = new Map();

    // Inicializar managers
    this.componentManager = null;
    this.filterManager = null;

    this.init();
  }

  /**
   * Aguarda o carregamento do OrdemCompraComponentsManager
   */
  async waitForComponentsManager() {
    let attempts = 0;
    const maxAttempts = 50; // 5 segundos máximo

    while (
      typeof OrdemCompraComponentsManager === "undefined" &&
      attempts < maxAttempts
    ) {
      await new Promise((resolve) => setTimeout(resolve, 100));
      attempts++;
    }

    if (typeof OrdemCompraComponentsManager === "undefined") {
      throw new Error(
        "OrdemCompraComponentsManager não foi carregado após 5 segundos"
      );
    }

    console.log(
      "[OrdemCompraManager] OrdemCompraComponentsManager carregado com sucesso"
    );
  }

  /**
   * Inicializa o manager
   */
  async init() {
    console.log("[OrdemCompraManager] Inicializando...");

    try {
      // Aguardar o carregamento do OrdemCompraComponentsManager
      if (typeof OrdemCompraComponentsManager === "undefined") {
        console.log(
          "[OrdemCompraManager] Aguardando carregamento do OrdemCompraComponentsManager..."
        );
        await this.waitForComponentsManager();
      }

      // Usar o componentsManager global se existir, senão criar um novo
      if (typeof window !== "undefined" && window.componentsManager) {
        this.componentManager = window.componentsManager;
        console.log("[OrdemCompraManager] Usando componentsManager global");
      } else {
        this.componentManager = new OrdemCompraComponentsManager();
        console.log(
          "[OrdemCompraManager] Criando nova instância de componentManager"
        );
      }

      this.setupEventListeners();
      await this.loadInitialData();

      // Inicializar filter manager após carregar dados
      if (typeof FilterManager !== "undefined") {
        this.filterManager = new FilterManager(this);
      } else {
        console.warn("[OrdemCompraManager] FilterManager não disponível");
      }

      console.log("[OrdemCompraManager] Inicializado com sucesso");
    } catch (error) {
      console.error("[OrdemCompraManager] Erro na inicialização:", error);
      notify.error(
        "Erro ao inicializar a página. Recarregue e tente novamente."
      );
    }
  }

  /**
   * Configura os event listeners
   */
  setupEventListeners() {
    // Eventos do componente de UI
    document.addEventListener("ordemcompra:form:submit", (e) => {
      this.handleFormSubmit(e.detail);
    });

    document.addEventListener("ordemcompra:ordem:view", (e) => {
      this.handleViewOrdem(e.detail.id);
    });

    document.addEventListener("ordemcompra:ordem:edit", (e) => {
      this.handleEditOrdem(e.detail.id);
    });

    document.addEventListener("ordemcompra:ordem:delete", (e) => {
      this.handleDeleteOrdem(e.detail.id);
    });

    document.addEventListener("ordemcompra:ordem:deactivate", (e) => {
      this.handleDeactivateOrdem(e.detail.id, e.detail.credentials);
    });

    document.addEventListener("ordemcompra:ordem:bulkDelete", (e) => {
      this.handleBulkDelete(e.detail.ids);
    });

    document.addEventListener("ordemcompra:table:sort", (e) => {
      this.handleSort(e.detail);
    });

    document.addEventListener("ordemcompra:pagination:change", () => {
      // Re-render no cliente com dados já carregados
      if (this.componentManager) {
        this.componentManager.renderTable(this.ordensCompra);
      }
    });

    // Event listeners para modal de informações
    const btnInfoSistema = document.getElementById("btnInfoSistema");
    const modalInfo = document.getElementById("modalInfo");
    const btnCloseInfo = document.getElementById("btnCloseInfo");

    if (btnInfoSistema && modalInfo) {
      btnInfoSistema.addEventListener("click", () => {
        this.updateInfoModalStats();
        modalInfo.style.display = "flex";
      });
    }

    if (btnCloseInfo && modalInfo) {
      btnCloseInfo.addEventListener("click", () => {
        modalInfo.style.display = "none";
      });
    }

    if (modalInfo) {
      modalInfo.addEventListener("click", (e) => {
        if (e.target === modalInfo) {
          modalInfo.style.display = "none";
        }
      });
    }

    // Event listener para exportação Excel
    const btnExportarExcel = document.getElementById("btnExportarExcel");
    if (btnExportarExcel) {
      btnExportarExcel.addEventListener("click", () => {
        this.exportToExcel();
      });
    }
  }

  // ============================================
  // OPERAÇÕES CRUD
  // ============================================

  /**
   * Carrega dados iniciais
   */
  async loadInitialData() {
    this.setLoading(true);

    try {
      await this.loadOrdens();
    } catch (error) {
      console.error(
        "[OrdemCompraManager] Erro ao carregar dados iniciais:",
        error
      );
      // Erro já foi tratado no loadOrdens
    } finally {
      this.setLoading(false);
    }
  }

  /**
   * Carrega ordens de compra do backend
   */
  async loadOrdens() {
    this.setLoading(true);

    try {
      const params = this.componentManager
        ? this.componentManager.getPaginationParams()
        : {};

      // Buscar do backend
      const response = await apiManager.getOrdensCompra(params);
      this.ordensCompra = Array.isArray(response)
        ? response
        : response.content || [];

      // Atualizar cache
      this.updateCache();

      // Renderizar dados
      this.renderOrdens();
    } catch (error) {
      console.error("[OrdemCompraManager] Erro ao carregar ordens:", error);

        // Mensagem mais simples para o usuário final
        notify.error('Não foi possível carregar as ordens agora. Tente novamente em instantes.');

      // Mostrar estado vazio com instruções
      this.showConnectionError();
      throw error;
    } finally {
      this.setLoading(false);
    }
  }

  /**
   * Cria uma nova ordem de compra
   * @param {Object} ordemData - Dados da ordem
   */
  async createOrdem(ordemData) {
    this.setLoading(true);
    if (this.componentManager) this.componentManager.showFormLoading(true);

    try {
      console.log(
        "[OrdemCompraManager] Iniciando criação de ordem:",
        ordemData
      );

      // Criar no backend
      const novaOrdem = await apiManager.createOrdemCompra(ordemData);
      console.log("[OrdemCompraManager] Ordem criada com sucesso:", novaOrdem);

      notify.success("Ordem de compra criada com sucesso!");

      // Atualizar interface
      await this.loadOrdens();
      if (this.componentManager) this.componentManager.closeModal();
    } catch (error) {
      console.error("[OrdemCompraManager] Erro ao criar ordem:", error);

      let errorMessage = "Erro desconhecido";

      if (
        error.message.includes("Failed to fetch") ||
        error.message.includes("NetworkError")
      ) {
        errorMessage =
          "Erro de conexão: Verifique se o backend está rodando em http://localhost:8080";
      } else if (error.message.includes("CORS")) {
        errorMessage =
          "Erro de CORS: Configure o backend para permitir requisições do frontend";
      } else if (error.message.includes("HTTP 400")) {
        errorMessage =
          "Dados inválidos: Verifique se todos os campos estão preenchidos corretamente";
      } else if (error.message.includes("HTTP 422")) {
        errorMessage =
          "Erro de validação: Os dados não atendem aos critérios de negócio";
      } else if (error.message.includes("HTTP 500")) {
        errorMessage = "Erro interno do servidor: Verifique os logs do backend";
      } else {
        errorMessage = error.message;
      }

      notify.error(`Erro ao criar ordem de compra: ${errorMessage}`);
    } finally {
      this.setLoading(false);
      if (this.componentManager) this.componentManager.showFormLoading(false);
    }
  }

  /**
   * Atualiza uma ordem de compra existente
   * @param {number} id - ID da ordem
   * @param {Object} ordemData - Dados atualizados
   */
  async updateOrdem(id, ordemData) {
    this.setLoading(true);
    if (this.componentManager) this.componentManager.showFormLoading(true);

    try {
      // Atualizar no backend
      const ordemAtualizada = await apiManager.updateOrdemCompra(id, ordemData);
      notify.success("Ordem de compra atualizada com sucesso!");

      // Atualizar interface
      await this.loadOrdens();
      if (this.componentManager) this.componentManager.closeModal();
    } catch (error) {
      console.error("[OrdemCompraManager] Erro ao atualizar ordem:", error);

      if (
        error.message.includes("CORS") ||
        error.message.includes("NetworkError")
      ) {
        notify.error(
          "Erro de CORS: Configure o backend para permitir requisições do frontend"
        );
      } else {
        notify.error(`Erro ao atualizar ordem de compra: ${error.message}`);
      }
    } finally {
      this.setLoading(false);
      if (this.componentManager) this.componentManager.showFormLoading(false);
    }
  }

  /**
   * Exclui uma ordem de compra
   * @param {number} id - ID da ordem
   */
  async deleteOrdem(id) {
    this.setLoading(true);

    try {
      // Excluir no backend
      await apiManager.deleteOrdemCompra(id);
      notify.success("Ordem de compra excluída com sucesso!");

      // Atualizar interface
      if (this.componentManager) this.componentManager.selectedItems.delete(id);
      await this.loadOrdens();
    } catch (error) {
      console.error("[OrdemCompraManager] Erro ao excluir ordem:", error);

      if (
        error.message.includes("CORS") ||
        error.message.includes("NetworkError")
      ) {
        notify.error(
          "Erro de CORS: Configure o backend para permitir requisições do frontend"
        );
      } else {
        notify.error(`Erro ao excluir ordem de compra: ${error.message}`);
      }
    } finally {
      this.setLoading(false);
    }
  }

  /**
   * Exclui múltiplas ordens de compra
   * @param {Array<number>} ids - Array com IDs das ordens
   */
  async bulkDelete(ids) {
    if (!ids || ids.length === 0) return;

    this.setLoading(true);
    const loadingNotification = notify.loading(
      `Excluindo ${ids.length} ordem(ns) de compra...`
    );

    try {
      // Excluir no backend
      const results = await apiManager.deleteMultipleOrdensCompra(ids);

      let successCount = 0;
      let errorCount = 0;

      results.forEach((result) => {
        if (result.error) {
          errorCount++;
        } else {
          successCount++;
        }
      });

      // Mostrar resultado
      notify.hide(loadingNotification);

      if (successCount > 0) {
        notify.success(`${successCount} ordem(ns) excluída(s) com sucesso!`);
      }

      if (errorCount > 0) {
        notify.warning(`${errorCount} ordem(ns) não puderam ser excluídas`);
      }

      // Limpar seleções e atualizar interface
      if (this.componentManager) this.componentManager.clearSelections();
      await this.loadOrdens();
    } catch (error) {
      notify.hide(loadingNotification);
      console.error("[OrdemCompraManager] Erro na exclusão em massa:", error);

      if (
        error.message.includes("CORS") ||
        error.message.includes("NetworkError")
      ) {
        notify.error(
          "Erro de CORS: Configure o backend para permitir requisições do frontend"
        );
      } else {
        notify.error("Erro na exclusão em massa das ordens");
      }
    } finally {
      this.setLoading(false);
    }
  }

  // ============================================
  // MANIPULADORES DE EVENTOS
  // ============================================

  /**
   * Manipula submissão do formulário
   * @param {Object} detail - Dados do evento
   */
  async handleFormSubmit(detail) {
    const { data, isEdit } = detail;

    if (isEdit) {
      await this.updateOrdem(data.id, data);
    } else {
      await this.createOrdem(data);
    }
  }

  /**
   * Manipula visualização de ordem
   * @param {number} id - ID da ordem
   */
  async handleViewOrdem(id) {
    try {
      const ordem = await this.getOrdem(id);
      if (ordem) {
        this.showOrdemDetails(ordem);
      } else {
        notify.error("Ordem de compra não encontrada");
      }
    } catch (error) {
      console.error("[OrdemCompraManager] Erro ao visualizar ordem:", error);
      notify.error("Erro ao carregar detalhes da ordem");
    }
  }

  /**
   * Manipula edição de ordem
   * @param {number} id - ID da ordem
   */
  async handleEditOrdem(id) {
    try {
      const ordem = await this.getOrdem(id);
      if (ordem) {
        if (this.componentManager) {
          this.componentManager.openModal("edit", ordem);
        } else {
          console.error("[OrdemCompraManager] ComponentManager não disponível");
        }
      } else {
        notify.error("Ordem de compra não encontrada");
      }
    } catch (error) {
      console.error("[OrdemCompraManager] Erro ao editar ordem:", error);
      notify.error("Erro ao carregar dados da ordem");
    }
  }

  /**
   * Manipula exclusão de ordem
   * @param {number} id - ID da ordem
   */
  async handleDeleteOrdem(id) {
    await this.deleteOrdem(id);
  }

  /**
   * Manipula desativação de ordem com autenticação
   * @param {number} id - ID da ordem
   * @param {Object} credentials - Credenciais de autenticação
   */
  async handleDeactivateOrdem(id, credentials) {
    this.setLoading(true);

    try {
      console.log("[OrdemCompraManager] Iniciando remoção da ordem:", id);

      // Chamar API de remoção com autenticação
      const result = await apiManager.deleteOrdemCompraWithAuth(
        id,
        credentials
      );

      console.log("[OrdemCompraManager] Ordem removida com sucesso:", result);

      notify.success("Ordem de compra removida com sucesso!");

      // Fechar modal de credenciais
      if (this.componentManager) {
        this.componentManager.closeCredentialsModal();
      }

      // Atualizar lista
      await this.loadOrdens();
    } catch (error) {
      console.error("[OrdemCompraManager] Erro ao remover ordem:", error);

      let errorMessage = "Erro desconhecido";

      if (
        error.message.includes("HTTP 401") ||
        error.message.includes("Credenciais inválidas")
      ) {
        errorMessage = "Credenciais inválidas: Verifique seu login e senha";
      } else if (error.message.includes("HTTP 404")) {
        errorMessage = "Ordem de compra não encontrada";
      } else if (error.message.includes("HTTP 422")) {
        errorMessage =
          "Esta ordem não pode ser removida (pode estar concluída)";
      } else if (
        error.message.includes("Failed to fetch") ||
        error.message.includes("NetworkError")
      ) {
        errorMessage = "Erro de conexão: Verifique se o backend está rodando";
      } else {
        errorMessage = error.message;
      }

      notify.error(`Erro ao remover ordem: ${errorMessage}`);

      // Esconder loading no modal
      if (this.componentManager) {
        this.componentManager.showCredentialsLoading(false);
      }
    } finally {
      this.setLoading(false);
    }
  }

  /**
   * Manipula exclusão em massa
   * @param {Array<number>} ids - IDs das ordens
   */
  async handleBulkDelete(ids) {
    await this.bulkDelete(ids);
  }

  /**
   * Manipula ordenação
   * @param {Object} sortConfig - Configuração de ordenação
   */
  handleSort(sortConfig) {
    this.sortOrdens(sortConfig);
    this.renderOrdens();
  }

  // ============================================
  // MÉTODOS AUXILIARES
  // ============================================

  /**
   * Obtém uma ordem específica
   * @param {number} id - ID da ordem
   * @returns {Promise<Object>} - Dados da ordem
   */
  async getOrdem(id) {
    try {
      // Buscar no backend
      const ordem = await apiManager.getOrdemCompra(id);
      return ordem;
    } catch (error) {
      console.error(
        "[OrdemCompraManager] Erro ao buscar ordem no backend:",
        error
      );

      if (
        error.message.includes("CORS") ||
        error.message.includes("NetworkError")
      ) {
        notify.error(
          "Erro de CORS: Configure o backend para permitir requisições do frontend"
        );
      } else {
        notify.error("Erro ao carregar dados da ordem");
      }

      return null;
    }
  }

  /**
   * Renderiza as ordens na interface
   */
  renderOrdens() {
    if (this.componentManager) {
      this.componentManager.renderTable(this.ordensCompra);
      this.componentManager.updatePagination(this.ordensCompra.length);
    }
  }

  /**
   * Ordena as ordens de compra
   * @param {Object} sortConfig - Configuração de ordenação
   */
  sortOrdens(sortConfig) {
    if (!sortConfig.field) return;

    this.ordensCompra.sort((a, b) => {
      let valueA = a[sortConfig.field];
      let valueB = b[sortConfig.field];

      // Tratamento especial para diferentes tipos
      if (typeof valueA === "string") {
        valueA = valueA.toLowerCase();
        valueB = valueB.toLowerCase();
      }

      if (valueA < valueB) {
        return sortConfig.direction === "asc" ? -1 : 1;
      }
      if (valueA > valueB) {
        return sortConfig.direction === "asc" ? 1 : -1;
      }
      return 0;
    });
  }

  /**
   * Mostra detalhes da ordem em modal
   * @param {Object} ordem - Dados da ordem
   */
  async showOrdemDetails(ordem) {
    if (!this.componentManager) {
      console.error(
        "[OrdemCompraManager] ComponentManager não disponível para exibir detalhes"
      );
      return;
    }

    try {
      // Abrir modal de visualização e deixar o próprio ComponentsManager carregar e renderizar os itens
      await this.componentManager.openViewModal(ordem);
    } catch (error) {
      console.error(
        "[OrdemCompraManager] Erro ao exibir detalhes da ordem:",
        error
      );
      notify.error("Erro ao carregar detalhes da ordem");
    }
  }

  /**
   * Carrega os itens de uma ordem de compra
   * @param {number} ordemId - ID da ordem
   */
  async loadOrdemItens(ordemId) {
    try {
      console.log("[OrdemCompraManager] Carregando itens da ordem:", ordemId);

      // Buscar itens no backend
      const itens = await apiManager.getItensOrdemCompra(ordemId);
      console.log("[OrdemCompraManager] Itens carregados:", itens);

      // Renderizar itens no modal
      this.renderOrdemItens(itens);
    } catch (error) {
      console.error(
        "[OrdemCompraManager] Erro ao carregar itens da ordem:",
        error
      );

      // Mostrar estado vazio em caso de erro
      this.renderOrdemItens([]);

      if (!error.message.includes("404")) {
        notify.warning("Erro ao carregar itens da ordem");
      }
    }
  }

  /**
   * Renderiza os itens da ordem no modal de visualização
   * @param {Array} itens - Array de itens da ordem
   */
  renderOrdemItens(itens) {
    const tbody = document.getElementById("viewItensTableBody");
    const totalItensElement = document.getElementById("viewTotalItens");
    const valorTotalElement = document.getElementById("viewValorTotalItens");

    if (!tbody) {
      console.error(
        "[OrdemCompraManager] Elemento viewItensTableBody não encontrado"
      );
      return;
    }

    if (!itens || itens.length === 0) {
      tbody.innerHTML = `
                <tr class="empty-row">
                    <td colspan="4" class="text-center">
                        <div class="empty-state">
                            <i data-feather="package" class="empty-icon"></i>
                            <p class="empty-message">Nenhum item encontrado</p>
                            <small class="empty-hint">Esta ordem não possui itens cadastrados</small>
                        </div>
                    </td>
                </tr>
            `;

      if (totalItensElement) totalItensElement.textContent = "0";
      if (valorTotalElement) valorTotalElement.textContent = "R$ 0,00";

      // Atualizar ícones
      if (typeof feather !== "undefined") {
        feather.replace();
      }
      return;
    }

    // Calcular totais
    let totalItens = 0;
    let valorTotal = 0;

    // Renderizar itens
    tbody.innerHTML = itens
      .map((item) => {
        const quantidade = parseInt(item.quantidade) || 0;
        const precoUnitario = parseFloat(item.precoUnitario) || 0;
        const valorItem = quantidade * precoUnitario;

        totalItens += quantidade;
        valorTotal += valorItem;

        return `
                <tr>
                    <td>
                        <div class="product-info">
                            <div class="product-name">${
                              item.produto?.nome || "Produto não encontrado"
                            }</div>
                            <div class="product-details">${
                              item.produto?.descricao || ""
                            }</div>
                        </div>
                    </td>
                    <td class="text-center">
                        <span class="quantity-badge">${quantidade}</span>
                    </td>
                    <td class="text-right">
                        <span class="price-value">R$ ${precoUnitario.toLocaleString(
                          "pt-BR",
                          { minimumFractionDigits: 2 }
                        )}</span>
                    </td>
                    <td class="text-right">
                        <span class="total-value">R$ ${valorItem.toLocaleString(
                          "pt-BR",
                          { minimumFractionDigits: 2 }
                        )}</span>
                    </td>
                </tr>
            `;
      })
      .join("");

    // Atualizar totais
    if (totalItensElement) totalItensElement.textContent = totalItens;
    if (valorTotalElement)
      valorTotalElement.textContent = `R$ ${valorTotal.toLocaleString("pt-BR", {
        minimumFractionDigits: 2,
      })}`;

    // Atualizar ícones
    if (typeof feather !== "undefined") {
      feather.replace();
    }
  }

  /**
   * Define estado de loading
   * @param {boolean} loading - Se está carregando
   */
  setLoading(loading) {
    this.isLoading = loading;

    if (loading && this.componentManager) {
      this.componentManager.showTableLoading();
    }
  }

  /**
   * Mostra estado de erro de conexão
   */
  showConnectionError() {
    if (!this.componentManager || !this.componentManager.elements.tableBody)
      return;

    this.componentManager.elements.tableBody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center">
                    <div class="empty-state" style="padding: 40px;">
                        <i data-feather="wifi-off" style="font-size: 48px; opacity: 0.5; color: #dc3545;"></i>
                        <h3 style="color: #dc3545; margin: 16px 0 8px 0;">Backend Não Conectado</h3>
                        <p style="margin-bottom: 20px;">Para visualizar e gerenciar as ordens de compra:</p>
                        <ol style="text-align: left; max-width: 400px; margin: 0 auto 20px auto;">
                            <li>Abra o terminal no diretório do projeto</li>
                            <li>Execute: <code style="background: #f8f9fa; padding: 2px 6px; border-radius: 4px;">mvn spring-boot:run</code></li>
                            <li>Aguarde até ver "Started EstoqueApplication"</li>
                            <li>Recarregue esta página</li>
                        </ol>
                        <button class="btn btn-primary" onclick="window.location.reload()">
                            <i data-feather="refresh-cw"></i>
                            Tentar Novamente
                        </button>
                    </div>
                </td>
            </tr>
        `;

    if (typeof feather !== "undefined") {
      feather.replace();
    }
  }

  /**
   * Atualiza cache local
   */
  updateCache() {
    this.ordensCompra.forEach((ordem) => {
      this.cache.set(ordem.id, { ...ordem, timestamp: Date.now() });
    });
  }

  /**
   * Limpa cache expirado
   * @param {number} maxAge - Idade máxima em ms (padrão: 5 minutos)
   */
  clearExpiredCache(maxAge = 5 * 60 * 1000) {
    const now = Date.now();

    for (const [id, data] of this.cache.entries()) {
      if (now - data.timestamp > maxAge) {
        this.cache.delete(id);
      }
    }
  }

  /**
   * Atualiza dados periodicamente
   */
  startPeriodicUpdate() {
    // Atualizar a cada 30 segundos se não estiver em uma operação
    setInterval(() => {
      if (
        !this.isLoading &&
        this.componentManager &&
        !this.componentManager.isModalOpen()
      ) {
        this.loadOrdens();
      }
    }, 30000);

    // Limpar cache expirado a cada 5 minutos
    setInterval(() => {
      this.clearExpiredCache();
    }, 5 * 60 * 1000);
  }

  /**
   * Obtém estatísticas das ordens
   * @returns {Object} - Estatísticas
   */
  getStatistics() {
    const stats = {
      total: this.ordensCompra.length,
      pendentes: 0,
      andamento: 0,
      concluidas: 0,
      valorTotal: 0,
    };

    this.ordensCompra.forEach((ordem) => {
      stats.valorTotal += ordem.valor || 0;

      switch (ordem.statusOrdemCompra) {
        case "PEND":
          stats.pendentes++;
          break;
        case "ANDA":
          stats.andamento++;
          break;
        case "CONC":
          stats.concluidas++;
          break;
      }
    });

    return stats;
  }

  /**
   * Exporta dados para Excel (.xlsx)
   */
  exportToExcel() {
    if (this.ordensCompra.length === 0) {
      notify.warning("Nenhuma ordem para exportar");
      return;
    }

    try {
      // Preparar dados para exportação
      const exportData = this.ordensCompra.map((ordem) => ({
        ID: ordem.id,
        Status: ordem.statusOrdemCompra,
        "Valor (R$)": `R$ ${parseFloat(ordem.valor).toLocaleString("pt-BR", {
          minimumFractionDigits: 2,
        })}`,
        "Data Prevista": ordem.dataPrev
          ? new Date(ordem.dataPrev).toLocaleDateString("pt-BR")
          : "",
        "Data da Ordem": ordem.dataOrdem
          ? new Date(ordem.dataOrdem).toLocaleDateString("pt-BR")
          : "",
        "Data de Entrega": ordem.dataEntre
          ? new Date(ordem.dataEntre).toLocaleDateString("pt-BR")
          : "Não entregue",
      }));

      // Criar workbook e worksheet
      const workbook = XLSX.utils.book_new();
      const worksheet = XLSX.utils.json_to_sheet(exportData);

      // Configurar largura das colunas
      const columnWidths = [
        { wch: 8 }, // ID
        { wch: 12 }, // Status
        { wch: 15 }, // Valor
        { wch: 15 }, // Data Prevista
        { wch: 15 }, // Data da Ordem
        { wch: 15 }, // Data de Entrega
      ];
      worksheet["!cols"] = columnWidths;

      // Aplicar formatação ao cabeçalho
      const headerStyle = {
        font: { bold: true, color: { rgb: "FFFFFF" } },
        fill: { fgColor: { rgb: "4472C4" } },
        alignment: { horizontal: "center" },
      };

      // Aplicar estilo ao cabeçalho
      const range = XLSX.utils.decode_range(worksheet["!ref"]);
      for (let col = range.s.c; col <= range.e.c; col++) {
        const cellAddress = XLSX.utils.encode_cell({ r: 0, c: col });
        if (!worksheet[cellAddress]) continue;
        worksheet[cellAddress].s = headerStyle;
      }

      // Adicionar worksheet ao workbook
      XLSX.utils.book_append_sheet(workbook, worksheet, "Ordens de Compra");

      // Adicionar estatísticas em uma segunda planilha
      const stats = this.getStatistics();
      const statsData = [
        { Métrica: "Total de Ordens", Valor: stats.total },
        {
          Métrica: "Valor Total",
          Valor: `R$ ${stats.valorTotal.toLocaleString("pt-BR", {
            minimumFractionDigits: 2,
          })}`,
        },
        { Métrica: "Concluídas", Valor: stats.concluidas },
        { Métrica: "Pendentes", Valor: stats.pendentes },
        { Métrica: "Em Processamento", Valor: stats.processamento },
        { Métrica: "Canceladas", Valor: stats.canceladas },
        {
          Métrica: "Data de Exportação",
          Valor: new Date().toLocaleString("pt-BR"),
        },
      ];

      const statsWorksheet = XLSX.utils.json_to_sheet(statsData);
      statsWorksheet["!cols"] = [{ wch: 20 }, { wch: 25 }];
      XLSX.utils.book_append_sheet(workbook, statsWorksheet, "Estatísticas");

      // Gerar nome do arquivo
      const fileName = `ordens_compra_${
        new Date().toISOString().split("T")[0]
      }.xlsx`;

      // Baixar arquivo
      XLSX.writeFile(workbook, fileName);

      notify.success("Planilha Excel gerada com sucesso!");
    } catch (error) {
      console.error("Erro ao exportar Excel:", error);
      notify.error("Erro ao gerar planilha Excel");
    }
  }

  /**
   * Calcula estatísticas das ordens
   */
  getStatistics() {
    const stats = {
      total: this.ordensCompra.length,
      valorTotal: 0,
      concluidas: 0,
      pendentes: 0,
      processamento: 0,
      canceladas: 0,
    };

    this.ordensCompra.forEach((ordem) => {
      stats.valorTotal += parseFloat(ordem.valor) || 0;

      switch (ordem.statusOrdemCompra) {
        case "CONC":
          stats.concluidas++;
          break;
        case "PEND":
          stats.pendentes++;
          break;
        case "PROC":
          stats.processamento++;
          break;
        case "CANC":
          stats.canceladas++;
          break;
      }
    });

    return stats;
  }

  /**
   * Atualiza as estatísticas no modal de informações
   */
  updateInfoModalStats() {
    const stats = this.getStatistics();

    const elements = {
      statTotalOrdens: document.getElementById("statTotalOrdens"),
      statValorTotal: document.getElementById("statValorTotal"),
      statConcluidas: document.getElementById("statConcluidas"),
      statPendentes: document.getElementById("statPendentes"),
      apiStatus: document.getElementById("apiStatus"),
      lastUpdate: document.getElementById("lastUpdate"),
    };

    if (elements.statTotalOrdens)
      elements.statTotalOrdens.textContent = stats.total;
    if (elements.statValorTotal)
      elements.statValorTotal.textContent = `R$ ${stats.valorTotal.toLocaleString(
        "pt-BR",
        { minimumFractionDigits: 2 }
      )}`;
    if (elements.statConcluidas)
      elements.statConcluidas.textContent = stats.concluidas;
    if (elements.statPendentes)
      elements.statPendentes.textContent = stats.pendentes;
    if (elements.apiStatus) {
      elements.apiStatus.textContent = "Online";
      elements.apiStatus.className = "status-online";
    }
    if (elements.lastUpdate)
      elements.lastUpdate.textContent = new Date().toLocaleString("pt-BR");
  }
}

// Inicializar quando o DOM estiver pronto
document.addEventListener("DOMContentLoaded", () => {
  // Aguardar um pouco para garantir que todos os scripts foram carregados
  setTimeout(() => {
    window.ordemCompraManager = new OrdemCompraManager();

    // Iniciar atualizações periódicas após 1 minuto
    setTimeout(() => {
      ordemCompraManager.startPeriodicUpdate();
    }, 60000);
  }, 100);
});

// Exportar para uso em outros módulos
if (typeof module !== "undefined" && module.exports) {
  module.exports = OrdemCompraManager;
}

console.log("[OrdemCompraManager] Script carregado");
