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
        
        this.init();
    }

    /**
     * Inicializa o manager
     */
    async init() {
        console.log('[OrdemCompraManager] Inicializando...');
        
        try {
            this.setupEventListeners();
            await this.loadInitialData();
            
            console.log('[OrdemCompraManager] Inicializado com sucesso');
        } catch (error) {
            console.error('[OrdemCompraManager] Erro na inicialização:', error);
            notify.error('Erro ao inicializar a página. Recarregue e tente novamente.');
        }
    }

    /**
     * Configura os event listeners
     */
    setupEventListeners() {
        // Eventos do componente de UI
        document.addEventListener('ordemcompra:form:submit', (e) => {
            this.handleFormSubmit(e.detail);
        });

        document.addEventListener('ordemcompra:ordem:view', (e) => {
            this.handleViewOrdem(e.detail.id);
        });

        document.addEventListener('ordemcompra:ordem:edit', (e) => {
            this.handleEditOrdem(e.detail.id);
        });

        document.addEventListener('ordemcompra:ordem:delete', (e) => {
            this.handleDeleteOrdem(e.detail.id);
        });

        document.addEventListener('ordemcompra:ordem:bulkDelete', (e) => {
            this.handleBulkDelete(e.detail.ids);
        });

        document.addEventListener('ordemcompra:table:sort', (e) => {
            this.handleSort(e.detail);
        });

        document.addEventListener('ordemcompra:pagination:change', () => {
            this.loadOrdens();
        });
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
            console.error('[OrdemCompraManager] Erro ao carregar dados iniciais:', error);
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
            const params = componentsManager.getPaginationParams();
            
            // Buscar do backend
            const response = await apiManager.getOrdensCompra(params);
            this.ordensCompra = Array.isArray(response) ? response : response.content || [];
            
            // Atualizar cache
            this.updateCache();
            
            // Renderizar dados
            this.renderOrdens();
            
        } catch (error) {
            console.error('[OrdemCompraManager] Erro ao carregar ordens:', error);
            
            if (error.message.includes('CORS') || error.message.includes('NetworkError')) {
                notify.error('❌ Backend não conectado. Para resolver:<br>1. Inicie o Spring Boot: <code>mvn spring-boot:run</code><br>2. Verifique se está rodando em http://localhost:8080', {
                    duration: 10000,
                    title: 'Erro de Conexão',
                    allowHtml: true
                });
            } else {
                notify.error('Erro ao carregar ordens de compra. Verifique se o backend está rodando.');
            }
            
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
        componentsManager.showFormLoading(true);

        try {
            // Criar no backend
            const novaOrdem = await apiManager.createOrdemCompra(ordemData);
            notify.success('Ordem de compra criada com sucesso!');
            
            // Atualizar interface
            await this.loadOrdens();
            componentsManager.closeModal();
            
        } catch (error) {
            console.error('[OrdemCompraManager] Erro ao criar ordem:', error);
            
            if (error.message.includes('CORS') || error.message.includes('NetworkError')) {
                notify.error('Erro de CORS: Configure o backend para permitir requisições do frontend');
            } else {
                notify.error(`Erro ao criar ordem de compra: ${error.message}`);
            }
        } finally {
            this.setLoading(false);
            componentsManager.showFormLoading(false);
        }
    }

    /**
     * Atualiza uma ordem de compra existente
     * @param {number} id - ID da ordem
     * @param {Object} ordemData - Dados atualizados
     */
    async updateOrdem(id, ordemData) {
        this.setLoading(true);
        componentsManager.showFormLoading(true);

        try {
            // Atualizar no backend
            const ordemAtualizada = await apiManager.updateOrdemCompra(id, ordemData);
            notify.success('Ordem de compra atualizada com sucesso!');
            
            // Atualizar interface
            await this.loadOrdens();
            componentsManager.closeModal();
            
        } catch (error) {
            console.error('[OrdemCompraManager] Erro ao atualizar ordem:', error);
            
            if (error.message.includes('CORS') || error.message.includes('NetworkError')) {
                notify.error('Erro de CORS: Configure o backend para permitir requisições do frontend');
            } else {
                notify.error(`Erro ao atualizar ordem de compra: ${error.message}`);
            }
        } finally {
            this.setLoading(false);
            componentsManager.showFormLoading(false);
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
            notify.success('Ordem de compra excluída com sucesso!');
            
            // Atualizar interface
            componentsManager.selectedItems.delete(id);
            await this.loadOrdens();
            
        } catch (error) {
            console.error('[OrdemCompraManager] Erro ao excluir ordem:', error);
            
            if (error.message.includes('CORS') || error.message.includes('NetworkError')) {
                notify.error('Erro de CORS: Configure o backend para permitir requisições do frontend');
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
        const loadingNotification = notify.loading(`Excluindo ${ids.length} ordem(ns) de compra...`);

        try {
            // Excluir no backend
            const results = await apiManager.deleteMultipleOrdensCompra(ids);
            
            let successCount = 0;
            let errorCount = 0;
            
            results.forEach(result => {
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
            componentsManager.clearSelections();
            await this.loadOrdens();
            
        } catch (error) {
            notify.hide(loadingNotification);
            console.error('[OrdemCompraManager] Erro na exclusão em massa:', error);
            
            if (error.message.includes('CORS') || error.message.includes('NetworkError')) {
                notify.error('Erro de CORS: Configure o backend para permitir requisições do frontend');
            } else {
                notify.error('Erro na exclusão em massa das ordens');
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
                notify.error('Ordem de compra não encontrada');
            }
        } catch (error) {
            console.error('[OrdemCompraManager] Erro ao visualizar ordem:', error);
            notify.error('Erro ao carregar detalhes da ordem');
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
                componentsManager.openModal('edit', ordem);
            } else {
                notify.error('Ordem de compra não encontrada');
            }
        } catch (error) {
            console.error('[OrdemCompraManager] Erro ao editar ordem:', error);
            notify.error('Erro ao carregar dados da ordem');
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
            console.error('[OrdemCompraManager] Erro ao buscar ordem no backend:', error);
            
            if (error.message.includes('CORS') || error.message.includes('NetworkError')) {
                notify.error('Erro de CORS: Configure o backend para permitir requisições do frontend');
            } else {
                notify.error('Erro ao carregar dados da ordem');
            }
            
            return null;
        }
    }

    /**
     * Renderiza as ordens na interface
     */
    renderOrdens() {
        componentsManager.renderTable(this.ordensCompra);
        componentsManager.updatePagination(this.ordensCompra.length);
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
            if (typeof valueA === 'string') {
                valueA = valueA.toLowerCase();
                valueB = valueB.toLowerCase();
            }

            if (valueA < valueB) {
                return sortConfig.direction === 'asc' ? -1 : 1;
            }
            if (valueA > valueB) {
                return sortConfig.direction === 'asc' ? 1 : -1;
            }
            return 0;
        });
    }

    /**
     * Mostra detalhes da ordem em modal
     * @param {Object} ordem - Dados da ordem
     */
    showOrdemDetails(ordem) {
        const statusText = componentsManager.getStatusText(ordem.statusOrdemCompra);
        const valorFormatado = componentsManager.formatCurrency(ordem.valor);
        const dataPrevFormatada = componentsManager.formatDate(ordem.dataPrev);
        const dataOrdemFormatada = componentsManager.formatDate(ordem.dataOrdem);
        const dataEntreFormatada = ordem.dataEntre ? 
              componentsManager.formatDate(ordem.dataEntre) : 'Não informada';

        notify.info(`
            <strong>Ordem de Compra #${ordem.id}</strong><br>
            Status: ${statusText}<br>
            Valor: ${valorFormatado}<br>
            Data da Ordem: ${dataOrdemFormatada}<br>
            Data Prevista: ${dataPrevFormatada}<br>
            Data de Entrega: ${dataEntreFormatada}
        `, { 
            duration: 8000,
            title: 'Detalhes da Ordem'
        });
    }

    /**
     * Define estado de loading
     * @param {boolean} loading - Se está carregando
     */
    setLoading(loading) {
        this.isLoading = loading;
        
        if (loading) {
            componentsManager.showTableLoading();
        }
    }

    /**
     * Mostra estado de erro de conexão
     */
    showConnectionError() {
        if (!componentsManager.elements.tableBody) return;

        componentsManager.elements.tableBody.innerHTML = `
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
        
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    }

    /**
     * Atualiza cache local
     */
    updateCache() {
        this.ordensCompra.forEach(ordem => {
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
            if (!this.isLoading && !componentsManager.isModalOpen()) {
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
            valorTotal: 0
        };

        this.ordensCompra.forEach(ordem => {
            stats.valorTotal += ordem.valor || 0;
            
            switch (ordem.statusOrdemCompra) {
                case 'PEND':
                    stats.pendentes++;
                    break;
                case 'ANDA':
                    stats.andamento++;
                    break;
                case 'CONC':
                    stats.concluidas++;
                    break;
            }
        });

        return stats;
    }

    /**
     * Exporta dados para CSV
     */
    exportToCSV() {
        if (this.ordensCompra.length === 0) {
            notify.warning('Nenhuma ordem para exportar');
            return;
        }

        const headers = ['ID', 'Status', 'Valor', 'Data Prevista', 'Data da Ordem', 'Data de Entrega'];
        const csvContent = [
            headers.join(','),
            ...this.ordensCompra.map(ordem => [
                ordem.id,
                ordem.statusOrdemCompra,
                ordem.valor,
                ordem.dataPrev,
                ordem.dataOrdem,
                ordem.dataEntre || ''
            ].join(','))
        ].join('\n');

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        
        link.setAttribute('href', url);
        link.setAttribute('download', `ordens_compra_${new Date().toISOString().split('T')[0]}.csv`);
        link.style.visibility = 'hidden';
        
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        notify.success('Dados exportados com sucesso!');
    }
}

// Inicializar quando o DOM estiver pronto
document.addEventListener('DOMContentLoaded', () => {
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
if (typeof module !== 'undefined' && module.exports) {
    module.exports = OrdemCompraManager;
}

console.log('[OrdemCompraManager] Script carregado');