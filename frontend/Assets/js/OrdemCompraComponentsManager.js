/**
 * OrdemCompraComponentsManager - Gerenciador de Componentes UI
 * Responsável por gerenciar todos os elementos de interface da página
 * Inclui modais, tabelas, formulários e interações do usuário
 */
class OrdemCompraComponentsManager {
    constructor() {
        this.elements = {};
        this.currentSort = { field: null, direction: 'asc' };
        this.selectedItems = new Set();
        this.currentPage = 1;
        this.itemsPerPage = 10;
        this.totalItems = 0;
        
        this.initializeElements();
        this.setupEventListeners();
        this.initializeFeatherIcons();
    }

    /**
     * Inicializa referências aos elementos DOM
     */
    initializeElements() {
        this.elements = {
            // Toolbar
            btnNovaOrdem: document.getElementById('btnNovaOrdem'),
            btnImportarXML: document.getElementById('btnImportarXML'),
            btnRemoverSelecionados: document.getElementById('btnRemoverSelecionados'),
            
            // Tabela
            tableBody: document.getElementById('ordemCompraTableBody'),
            selectAll: document.getElementById('selectAll'),
            
            // Paginação
            itemsPerPageSelect: document.getElementById('itemsPerPage'),
            prevPageBtn: document.getElementById('prevPage'),
            nextPageBtn: document.getElementById('nextPage'),
            paginationCurrent: document.querySelector('.pagination-current'),
            
            // Modal
            modalOrdemCompra: document.getElementById('modalOrdemCompra'),
            modalTitle: document.getElementById('modalTitle'),
            formOrdemCompra: document.getElementById('formOrdemCompra'),
            btnCloseModal: document.getElementById('btnCloseModal'),
            btnCancelModal: document.getElementById('btnCancelModal'),
            
            // Campos do formulário
            inputId: document.getElementById('id'),
            inputStatusOrdemCompra: document.getElementById('statusOrdemCompra'),
            inputValor: document.getElementById('valor'),
            inputDataPrev: document.getElementById('dataPrev'),
            inputDataOrdem: document.getElementById('dataOrdem'),
            inputDataEntre: document.getElementById('dataEntre')
        };

        // Verificar se todos os elementos foram encontrados
        this.validateElements();
    }

    /**
     * Valida se todos os elementos críticos foram encontrados
     */
    validateElements() {
        const criticalElements = [
            'tableBody', 'modalOrdemCompra', 'formOrdemCompra'
        ];

        for (const elementKey of criticalElements) {
            if (!this.elements[elementKey]) {
                console.error(`[ComponentsManager] Elemento crítico não encontrado: ${elementKey}`);
            }
        }
    }

    /**
     * Configura todos os event listeners
     */
    setupEventListeners() {
        this.setupToolbarListeners();
        this.setupTableListeners();
        this.setupModalListeners();
        this.setupPaginationListeners();
        this.setupFormListeners();
        this.setupKeyboardListeners();
    }

    /**
     * Configura listeners da toolbar
     */
    setupToolbarListeners() {
        // Nova ordem de compra
        if (this.elements.btnNovaOrdem) {
            this.elements.btnNovaOrdem.addEventListener('click', () => {
                this.openModal('create');
            });
        }

        // Importar XML (placeholder)
        if (this.elements.btnImportarXML) {
            this.elements.btnImportarXML.addEventListener('click', () => {
                notify.info('Funcionalidade de importação XML em desenvolvimento');
            });
        }

        // Remover selecionados
        if (this.elements.btnRemoverSelecionados) {
            this.elements.btnRemoverSelecionados.addEventListener('click', () => {
                this.handleBulkDelete();
            });
        }
    }

    /**
     * Configura listeners da tabela
     */
    setupTableListeners() {
        // Select all checkbox
        if (this.elements.selectAll) {
            this.elements.selectAll.addEventListener('change', (e) => {
                this.handleSelectAll(e.target.checked);
            });
        }

        // Sortable headers
        const sortableHeaders = document.querySelectorAll('.sortable');
        sortableHeaders.forEach(header => {
            header.addEventListener('click', () => {
                const field = header.getAttribute('data-sort');
                this.handleSort(field);
            });
        });
    }

    /**
     * Configura listeners do modal
     */
    setupModalListeners() {
        // Fechar modal
        [this.elements.btnCloseModal, this.elements.btnCancelModal].forEach(btn => {
            if (btn) {
                btn.addEventListener('click', () => {
                    this.closeModal();
                });
            }
        });

        // Fechar modal clicando no overlay
        if (this.elements.modalOrdemCompra) {
            this.elements.modalOrdemCompra.addEventListener('click', (e) => {
                if (e.target === this.elements.modalOrdemCompra) {
                    this.closeModal();
                }
            });
        }
    }

    /**
     * Configura listeners da paginação
     */
    setupPaginationListeners() {
        // Items per page
        if (this.elements.itemsPerPageSelect) {
            this.elements.itemsPerPageSelect.addEventListener('change', (e) => {
                this.itemsPerPage = parseInt(e.target.value);
                this.currentPage = 1;
                this.dispatchEvent('pagination:change');
            });
        }

        // Previous page
        if (this.elements.prevPageBtn) {
            this.elements.prevPageBtn.addEventListener('click', () => {
                if (this.currentPage > 1) {
                    this.currentPage--;
                    this.dispatchEvent('pagination:change');
                }
            });
        }

        // Next page
        if (this.elements.nextPageBtn) {
            this.elements.nextPageBtn.addEventListener('click', () => {
                const totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
                if (this.currentPage < totalPages) {
                    this.currentPage++;
                    this.dispatchEvent('pagination:change');
                }
            });
        }
    }

    /**
     * Configura listeners do formulário
     */
    setupFormListeners() {
        if (this.elements.formOrdemCompra) {
            this.elements.formOrdemCompra.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleFormSubmit();
            });
        }

        // Formatação de valor monetário (removido - deixar campo number normal)
        // if (this.elements.inputValor) {
        //     this.elements.inputValor.addEventListener('input', (e) => {
        //         this.formatCurrencyInput(e.target);
        //     });
        // }
    }

    /**
     * Configura listeners de teclado
     */
    setupKeyboardListeners() {
        document.addEventListener('keydown', (e) => {
            // ESC para fechar modal
            if (e.key === 'Escape' && this.isModalOpen()) {
                this.closeModal();
            }
            
            // Ctrl+N para nova ordem
            if (e.ctrlKey && e.key === 'n') {
                e.preventDefault();
                this.openModal('create');
            }
            
            // Delete para remover selecionados
            if (e.key === 'Delete' && this.selectedItems.size > 0 && !this.isModalOpen()) {
                this.handleBulkDelete();
            }
        });
    }

    /**
     * Inicializa os ícones Feather
     */
    initializeFeatherIcons() {
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    }

    // ============================================
    // MÉTODOS DA TABELA
    // ============================================

    /**
     * Renderiza os dados na tabela
     * @param {Array} ordensCompra - Array com ordens de compra
     */
    renderTable(ordensCompra) {
        if (!this.elements.tableBody) {
            console.error('[ComponentsManager] Elemento tableBody não encontrado');
            return;
        }

        if (!ordensCompra || ordensCompra.length === 0) {
            this.renderEmptyState();
            return;
        }

        const tbody = this.elements.tableBody;
        tbody.innerHTML = '';

        ordensCompra.forEach(ordem => {
            const row = this.createTableRow(ordem);
            tbody.appendChild(row);
        });

        // Atualizar seleções
        this.updateSelectionState();
        
        // Reinicializar ícones
        this.initializeFeatherIcons();
    }

    /**
     * Cria uma linha da tabela
     * @param {Object} ordem - Dados da ordem de compra
     * @returns {HTMLElement} - Elemento tr
     */
    createTableRow(ordem) {
        const tr = document.createElement('tr');
        tr.setAttribute('data-id', ordem.id);

        // Checkbox
        const isSelected = this.selectedItems.has(ordem.id);
        
        // Status badge
        const statusClass = this.getStatusClass(ordem.statusOrdemCompra);
        const statusText = this.getStatusText(ordem.statusOrdemCompra);

        // Formatar valores
        const valorFormatado = this.formatCurrency(ordem.valor);
        const dataPrevFormatada = this.formatDate(ordem.dataPrev);
        const dataOrdemFormatada = this.formatDate(ordem.dataOrdem);
        const dataEntreFormatada = ordem.dataEntre ? this.formatDate(ordem.dataEntre) : '-';

        tr.innerHTML = `
            <td class="checkbox-column">
                <input type="checkbox" ${isSelected ? 'checked' : ''} 
                       onchange="componentsManager.handleRowSelection(${ordem.id}, this.checked)">
            </td>
            <td>${ordem.id}</td>
            <td>${dataOrdemFormatada}</td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
            <td>${valorFormatado}</td>
            <td>-</td>
            <td class="actions">
                <button class="action-btn action-view" onclick="componentsManager.viewOrdem(${ordem.id})" 
                        title="Visualizar">
                    <i data-feather="eye"></i>
                </button>
                <button class="action-btn action-edit" onclick="componentsManager.editOrdem(${ordem.id})" 
                        title="Editar">
                    <i data-feather="edit-2"></i>
                </button>
                <button class="action-btn action-delete" onclick="componentsManager.deleteOrdem(${ordem.id})" 
                        title="Excluir">
                    <i data-feather="trash-2"></i>
                </button>
            </td>
        `;

        return tr;
    }

    /**
     * Renderiza estado vazio
     */
    renderEmptyState() {
        const tbody = this.elements.tableBody;
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center">
                    <div class="empty-state">
                        <i data-feather="inbox" style="font-size: 48px; opacity: 0.5;"></i>
                        <h3>Nenhuma ordem de compra encontrada</h3>
                        <p>Clique em "Nova Ordem de Compra" para começar</p>
                        <button class="btn btn-primary" onclick="componentsManager.openModal('create')">
                            <i data-feather="plus"></i>
                            Nova Ordem de Compra
                        </button>
                    </div>
                </td>
            </tr>
        `;
        this.initializeFeatherIcons();
    }

    // ============================================
    // MÉTODOS DE SELEÇÃO
    // ============================================

    /**
     * Manipula seleção de linha individual
     * @param {number} id - ID da ordem
     * @param {boolean} selected - Se está selecionado
     */
    handleRowSelection(id, selected) {
        if (selected) {
            this.selectedItems.add(id);
        } else {
            this.selectedItems.delete(id);
        }
        
        this.updateSelectionState();
        this.updateBulkActions();
    }

    /**
     * Manipula seleção de todos os itens
     * @param {boolean} selectAll - Se deve selecionar todos
     */
    handleSelectAll(selectAll) {
        const checkboxes = this.elements.tableBody.querySelectorAll('input[type="checkbox"]');
        
        checkboxes.forEach(checkbox => {
            const row = checkbox.closest('tr');
            const id = parseInt(row.getAttribute('data-id'));
            
            checkbox.checked = selectAll;
            
            if (selectAll) {
                this.selectedItems.add(id);
            } else {
                this.selectedItems.delete(id);
            }
        });
        
        this.updateBulkActions();
    }

    /**
     * Atualiza estado da seleção
     */
    updateSelectionState() {
        if (!this.elements.selectAll) return;

        const checkboxes = this.elements.tableBody.querySelectorAll('input[type="checkbox"]');
        const checkedBoxes = Array.from(checkboxes).filter(cb => cb.checked);
        
        if (checkedBoxes.length === 0) {
            this.elements.selectAll.checked = false;
            this.elements.selectAll.indeterminate = false;
        } else if (checkedBoxes.length === checkboxes.length) {
            this.elements.selectAll.checked = true;
            this.elements.selectAll.indeterminate = false;
        } else {
            this.elements.selectAll.checked = false;
            this.elements.selectAll.indeterminate = true;
        }
    }

    /**
     * Atualiza visibilidade das ações em massa
     */
    updateBulkActions() {
        if (this.elements.btnRemoverSelecionados) {
            const hasSelection = this.selectedItems.size > 0;
            this.elements.btnRemoverSelecionados.disabled = !hasSelection;
            
            if (hasSelection) {
                this.elements.btnRemoverSelecionados.textContent = 
                    `Remover selecionados (${this.selectedItems.size})`;
            } else {
                this.elements.btnRemoverSelecionados.innerHTML = `
                    <i data-feather="trash-2"></i>
                    Remover selecionados
                `;
                this.initializeFeatherIcons();
            }
        }
    }

    // ============================================
    // MÉTODOS DO MODAL
    // ============================================

    /**
     * Abre o modal para criar ou editar
     * @param {string} mode - 'create' ou 'edit'
     * @param {Object} data - Dados para edição
     */
    openModal(mode = 'create', data = null) {
        if (!this.elements.modalOrdemCompra) return;

        this.clearForm();
        
        if (mode === 'create') {
            this.elements.modalTitle.textContent = 'Cadastro de Ordem de Compra';
            this.setDefaultDates();
        } else {
            this.elements.modalTitle.textContent = 'Editar Ordem de Compra';
            if (data) {
                this.populateForm(data);
            }
        }

        this.elements.modalOrdemCompra.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Focar no primeiro campo
        setTimeout(() => {
            const firstInput = this.elements.formOrdemCompra.querySelector('input, select');
            if (firstInput) {
                firstInput.focus();
            }
        }, 100);
    }

    /**
     * Fecha o modal
     */
    closeModal() {
        if (!this.elements.modalOrdemCompra) return;

        this.elements.modalOrdemCompra.classList.remove('active');
        document.body.style.overflow = '';
        this.clearForm();
    }

    /**
     * Verifica se o modal está aberto
     * @returns {boolean}
     */
    isModalOpen() {
        return this.elements.modalOrdemCompra && 
               this.elements.modalOrdemCompra.classList.contains('active');
    }

    // ============================================
    // MÉTODOS DO FORMULÁRIO
    // ============================================

    /**
     * Limpa o formulário
     */
    clearForm() {
        if (!this.elements.formOrdemCompra) return;

        this.elements.formOrdemCompra.reset();
        this.elements.inputId.value = '';
        
        // Remover classes de validação
        const inputs = this.elements.formOrdemCompra.querySelectorAll('input, select');
        inputs.forEach(input => {
            input.classList.remove('is-valid', 'is-invalid');
        });
    }

    /**
     * Popula o formulário com dados
     * @param {Object} data - Dados da ordem
     */
    populateForm(data) {
        if (!data) return;

        this.elements.inputId.value = data.id || '';
        this.elements.inputStatusOrdemCompra.value = data.statusOrdemCompra || '';
        this.elements.inputValor.value = data.valor || '';
        this.elements.inputDataPrev.value = data.dataPrev || '';
        this.elements.inputDataOrdem.value = data.dataOrdem || '';
        this.elements.inputDataEntre.value = data.dataEntre || '';
    }

    /**
     * Define datas padrão para nova ordem
     */
    setDefaultDates() {
        const today = new Date().toISOString().split('T')[0];
        this.elements.inputDataOrdem.value = today;
        
        // Data prevista para 30 dias a partir de hoje
        const futureDate = new Date();
        futureDate.setDate(futureDate.getDate() + 30);
        this.elements.inputDataPrev.value = futureDate.toISOString().split('T')[0];
    }

    /**
     * Manipula submissão do formulário
     */
    handleFormSubmit() {
        const formData = this.getFormData();
        const isEdit = !!formData.id;
        
        if (this.validateForm(formData)) {
            this.dispatchEvent('form:submit', { data: formData, isEdit });
        }
    }

    /**
     * Obtém dados do formulário
     * @returns {Object} - Dados do formulário
     */
    getFormData() {
        return {
            id: this.elements.inputId.value || null,
            statusOrdemCompra: this.elements.inputStatusOrdemCompra.value,
            valor: parseFloat(this.elements.inputValor.value) || 0,
            dataPrev: this.elements.inputDataPrev.value,
            dataOrdem: this.elements.inputDataOrdem.value,
            dataEntre: this.elements.inputDataEntre.value || null
        };
    }

    /**
     * Valida o formulário
     * @param {Object} data - Dados a serem validados
     * @returns {boolean} - Se é válido
     */
    validateForm(data) {
        let isValid = true;
        const errors = [];

        // Validar status
        if (!data.statusOrdemCompra) {
            errors.push('Status da ordem é obrigatório');
            this.markFieldInvalid(this.elements.inputStatusOrdemCompra);
            isValid = false;
        }

        // Validar valor
        if (!data.valor || data.valor <= 0) {
            errors.push('Valor deve ser maior que zero');
            this.markFieldInvalid(this.elements.inputValor);
            isValid = false;
        }

        // Validar datas
        if (!data.dataPrev) {
            errors.push('Data prevista é obrigatória');
            this.markFieldInvalid(this.elements.inputDataPrev);
            isValid = false;
        }

        if (!data.dataOrdem) {
            errors.push('Data da ordem é obrigatória');
            this.markFieldInvalid(this.elements.inputDataOrdem);
            isValid = false;
        }

        // Mostrar erros se houver
        if (errors.length > 0) {
            notify.error(`Erro na validação: ${errors.join(', ')}`);
        }

        return isValid;
    }

    /**
     * Marca campo como inválido
     * @param {HTMLElement} field - Campo a ser marcado
     */
    markFieldInvalid(field) {
        if (field) {
            field.classList.add('is-invalid');
            field.classList.remove('is-valid');
        }
    }

    /**
     * Marca campo como válido
     * @param {HTMLElement} field - Campo a ser marcado
     */
    markFieldValid(field) {
        if (field) {
            field.classList.add('is-valid');
            field.classList.remove('is-invalid');
        }
    }

    // ============================================
    // MÉTODOS DE AÇÃO
    // ============================================

    /**
     * Visualiza uma ordem
     * @param {number} id - ID da ordem
     */
    viewOrdem(id) {
        this.dispatchEvent('ordem:view', { id });
    }

    /**
     * Edita uma ordem
     * @param {number} id - ID da ordem
     */
    editOrdem(id) {
        this.dispatchEvent('ordem:edit', { id });
    }

    /**
     * Exclui uma ordem
     * @param {number} id - ID da ordem
     */
    deleteOrdem(id) {
        if (confirm('Tem certeza que deseja excluir esta ordem de compra?')) {
            this.dispatchEvent('ordem:delete', { id });
        }
    }

    /**
     * Manipula exclusão em massa
     */
    handleBulkDelete() {
        if (this.selectedItems.size === 0) {
            notify.warning('Nenhum item selecionado');
            return;
        }

        const count = this.selectedItems.size;
        const message = `Tem certeza que deseja excluir ${count} ordem(ns) de compra selecionada(s)?`;
        
        if (confirm(message)) {
            const ids = Array.from(this.selectedItems);
            this.dispatchEvent('ordem:bulkDelete', { ids });
        }
    }

    /**
     * Manipula ordenação
     * @param {string} field - Campo para ordenar
     */
    handleSort(field) {
        if (this.currentSort.field === field) {
            this.currentSort.direction = this.currentSort.direction === 'asc' ? 'desc' : 'asc';
        } else {
            this.currentSort.field = field;
            this.currentSort.direction = 'asc';
        }

        this.updateSortIndicators();
        this.dispatchEvent('table:sort', this.currentSort);
    }

    /**
     * Atualiza indicadores de ordenação
     */
    updateSortIndicators() {
        // Remover todas as classes de ordenação
        document.querySelectorAll('.sortable').forEach(header => {
            header.classList.remove('sorted', 'desc');
        });

        // Adicionar classe ao header atual
        if (this.currentSort.field) {
            const header = document.querySelector(`[data-sort="${this.currentSort.field}"]`);
            if (header) {
                header.classList.add('sorted');
                if (this.currentSort.direction === 'desc') {
                    header.classList.add('desc');
                }
            }
        }
    }

    // ============================================
    // MÉTODOS DE PAGINAÇÃO
    // ============================================

    /**
     * Atualiza controles de paginação
     * @param {number} totalItems - Total de itens
     */
    updatePagination(totalItems) {
        this.totalItems = totalItems;
        const totalPages = Math.ceil(totalItems / this.itemsPerPage);

        // Atualizar página atual
        if (this.elements.paginationCurrent) {
            this.elements.paginationCurrent.textContent = this.currentPage;
        }

        // Atualizar botões
        if (this.elements.prevPageBtn) {
            this.elements.prevPageBtn.disabled = this.currentPage <= 1;
        }

        if (this.elements.nextPageBtn) {
            this.elements.nextPageBtn.disabled = this.currentPage >= totalPages;
        }
    }

    /**
     * Obtém parâmetros de paginação
     * @returns {Object} - Parâmetros de paginação
     */
    getPaginationParams() {
        return {
            page: this.currentPage,
            size: this.itemsPerPage,
            sort: this.currentSort.field ? 
                  `${this.currentSort.field},${this.currentSort.direction}` : null
        };
    }

    // ============================================
    // MÉTODOS UTILITÁRIOS
    // ============================================

    /**
     * Formata valor monetário
     * @param {number} value - Valor a ser formatado
     * @returns {string} - Valor formatado
     */
    formatCurrency(value) {
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        }).format(value || 0);
    }

    /**
     * Formata data
     * @param {string} dateString - Data em string
     * @returns {string} - Data formatada
     */
    formatDate(dateString) {
        if (!dateString) return '-';
        
        const date = new Date(dateString + 'T00:00:00');
        return date.toLocaleDateString('pt-BR');
    }

    /**
     * Formata input de moeda
     * @param {HTMLElement} input - Input a ser formatado
     */
    formatCurrencyInput(input) {
        let value = input.value.replace(/\D/g, '');
        value = (value / 100).toFixed(2);
        input.value = value;
    }

    /**
     * Obtém classe CSS para status
     * @param {string} status - Status da ordem
     * @returns {string} - Classe CSS
     */
    getStatusClass(status) {
        const classes = {
            'PEND': 'status-pend',
            'ANDA': 'status-anda',
            'CONC': 'status-conc'
        };
        return classes[status] || 'status-pend';
    }

    /**
     * Obtém texto do status
     * @param {string} status - Status da ordem
     * @returns {string} - Texto do status
     */
    getStatusText(status) {
        const texts = {
            'PEND': 'Pendente',
            'ANDA': 'Em Andamento',
            'CONC': 'Concluído'
        };
        return texts[status] || 'Desconhecido';
    }

    /**
     * Limpa seleções
     */
    clearSelections() {
        this.selectedItems.clear();
        this.updateSelectionState();
        this.updateBulkActions();
    }

    /**
     * Dispara evento customizado
     * @param {string} eventName - Nome do evento
     * @param {Object} detail - Dados do evento
     */
    dispatchEvent(eventName, detail = {}) {
        const event = new CustomEvent(`ordemcompra:${eventName}`, { detail });
        document.dispatchEvent(event);
    }

    /**
     * Mostra loading na tabela
     */
    showTableLoading() {
        if (this.elements.tableBody) {
            this.elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="9" class="text-center">
                        <div class="loading-state">
                            <div class="spinner"></div>
                            <p>Carregando ordens de compra...</p>
                        </div>
                    </td>
                </tr>
            `;
        }
    }

    /**
     * Mostra loading no formulário
     * @param {boolean} show - Se deve mostrar loading
     */
    showFormLoading(show = true) {
        if (this.elements.formOrdemCompra) {
            if (show) {
                this.elements.formOrdemCompra.classList.add('loading');
            } else {
                this.elements.formOrdemCompra.classList.remove('loading');
            }
        }
    }
}

// Criar instância global
const componentsManager = new OrdemCompraComponentsManager();

// Exportar para uso em outros módulos
if (typeof module !== 'undefined' && module.exports) {
    module.exports = OrdemCompraComponentsManager;
}

console.log('[OrdemCompraComponentsManager] Inicializado com sucesso');