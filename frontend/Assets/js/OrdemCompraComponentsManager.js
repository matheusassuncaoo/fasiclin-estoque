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
        this.data = []; // Armazenar dados das ordens
        
        // Wizard da ordem de compra
        this.currentStep = 1;
        this.totalSteps = 4;
        this.ordemData = {
            informacoes: {},
            produtos: [],
            fornecedores: {},
            resumo: {}
        };
        this.produtosDisponiveis = [];
        this.fornecedoresDisponiveis = [];
        
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
            inputValorInicial: document.getElementById('valorInicial'),
            inputDataPrev: document.getElementById('dataPrev'),
            inputDataOrdem: document.getElementById('dataOrdem'),
            inputDataEntre: document.getElementById('dataEntre'),
            inputObservacoes: document.getElementById('observacoesOrdem'),
            
            // Wizard Elements
            progressSteps: document.querySelectorAll('.progress-steps .step'),
            formSteps: document.querySelectorAll('.form-step'),
            btnAnterior: document.getElementById('btnAnterior'),
            btnProximo: document.getElementById('btnProximo'),
            btnFinalizarOrdem: document.getElementById('btnFinalizarOrdem'),
            
            // Step specific elements
            produtosContainer: document.getElementById('produtosContainer'),
            fornecedoresContainer: document.getElementById('fornecedoresContainer'),
            filtrarProdutos: document.getElementById('filtrarProdutos'),
            filtroUrgencia: document.getElementById('filtroUrgencia'),
            produtosSelecionados: document.getElementById('produtosSelecionados'),
            valorEstimado: document.getElementById('valorEstimado'),

            // Modal de credenciais
            modalCredentials: document.getElementById('modalCredentials'),
            btnCloseCredentials: document.getElementById('btnCloseCredentials'),
            btnCancelDeactivation: document.getElementById('btnCancelDeactivation'),
            btnConfirmDeactivation: document.getElementById('btnConfirmDeactivation'),
            credentialsForm: document.getElementById('credentialsForm'),
            credentialsLogin: document.getElementById('credentialsLogin'),
            credentialsPassword: document.getElementById('credentialsPassword'),
            deactivationReason: document.getElementById('deactivationReason'),

            // Modal de itens
            modalItens: document.getElementById('modalItens'),
            btnCloseItens: document.getElementById('btnCloseItens'),
            btnCancelarItens: document.getElementById('btnCancelarItens'),
            btnSalvarItens: document.getElementById('btnSalvarItens'),
            ordemNumero: document.getElementById('ordemNumero'),
            ordemStatus: document.getElementById('ordemStatus'),
            ordemDataPrev: document.getElementById('ordemDataPrev'),
            ordemValorAtual: document.getElementById('ordemValorAtual'),
            produtoSelect: document.getElementById('produtoSelect'),
            quantidadeInput: document.getElementById('quantidadeInput'),
            precoUnitarioInput: document.getElementById('precoUnitarioInput'),
            valorTotalItem: document.getElementById('valorTotalItem'),
            btnAdicionarItem: document.getElementById('btnAdicionarItem'),
            itensTableBody: document.getElementById('itensTableBody'),
            totalItens: document.getElementById('totalItens'),
            subtotalOrdem: document.getElementById('subtotalOrdem'),
            totalOrdem: document.getElementById('totalOrdem')
        };

        // Variável para armazenar o ID da ordem a ser desativada
        this.currentDeactivationId = null;

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
        this.setupWizardListeners();
        this.setupPaginationListeners();
        this.setupFormListeners();
        this.setupKeyboardListeners();
        this.setupCredentialsListeners();
        this.setupItensListeners();
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
     * Configura listeners do wizard de ordem de compra
     */
    setupWizardListeners() {
        // Botão Anterior
        if (this.elements.btnAnterior) {
            this.elements.btnAnterior.addEventListener('click', () => {
                this.previousStep();
            });
        }

        // Botão Próximo
        if (this.elements.btnProximo) {
            this.elements.btnProximo.addEventListener('click', () => {
                this.nextStep();
            });
        }

        // Botão Finalizar
        if (this.elements.btnFinalizarOrdem) {
            this.elements.btnFinalizarOrdem.addEventListener('click', () => {
                this.finalizarOrdemCompra();
            });
        }

        // Filtro de produtos
        if (this.elements.filtrarProdutos) {
            this.elements.filtrarProdutos.addEventListener('input', (e) => {
                this.filtrarProdutos(e.target.value);
            });
        }

        // Filtro de urgência
        if (this.elements.filtroUrgencia) {
            this.elements.filtroUrgencia.addEventListener('change', (e) => {
                this.filtrarPorUrgencia(e.target.value);
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

        // View Modal Event Listeners
        const btnCloseViewModal = document.getElementById('btnCloseViewModal');
        const btnCloseViewModalBtn = document.getElementById('btnCloseViewModalBtn');
        const modalViewOrdem = document.getElementById('modalViewOrdem');

        if (btnCloseViewModal) {
            btnCloseViewModal.addEventListener('click', () => {
                this.closeViewModal();
            });
        }

        if (btnCloseViewModalBtn) {
            btnCloseViewModalBtn.addEventListener('click', () => {
                this.closeViewModal();
            });
        }

        if (modalViewOrdem) {
            modalViewOrdem.addEventListener('click', (e) => {
                if (e.target === modalViewOrdem) {
                    this.closeViewModal();
                }
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
            if (e.key === 'Escape') {
                if (this.isViewModalOpen()) {
                    this.closeViewModal();
                } else if (this.isModalOpen()) {
                    this.closeModal();
                }
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
     * Configura listeners do modal de credenciais
     */
    setupCredentialsListeners() {
        if (!this.elements.modalCredentials) return;

        // Fechar modal de credenciais
        if (this.elements.btnCloseCredentials) {
            this.elements.btnCloseCredentials.addEventListener('click', () => {
                this.closeCredentialsModal();
            });
        }

        if (this.elements.btnCancelDeactivation) {
            this.elements.btnCancelDeactivation.addEventListener('click', () => {
                this.closeCredentialsModal();
            });
        }

        // Submissão do formulário de credenciais
        if (this.elements.credentialsForm) {
            this.elements.credentialsForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleDeactivationSubmit();
            });
        }

        // Fechar modal clicando no overlay
        this.elements.modalCredentials.addEventListener('click', (e) => {
            if (e.target === this.elements.modalCredentials) {
                this.closeCredentialsModal();
            }
        });

        // Escape key para fechar modal
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.isCredentialsModalOpen()) {
                this.closeCredentialsModal();
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

        // Armazenar dados localmente
        this.data = ordensCompra || [];

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
            <td><strong>${ordem.id}</strong></td>
            <td>
                <span class="status-badge ${statusClass}">${statusText}</span>
            </td>
            <td>${valorFormatado}</td>
            <td>${dataPrevFormatada}</td>
            <td>${dataOrdemFormatada}</td>
            <td>${dataEntreFormatada}</td>
            <td class="actions">
                <button class="action-btn action-view" onclick="componentsManager.viewOrdem(${ordem.id})" 
                        title="Visualizar">
                    <i data-feather="eye"></i>
                </button>
                <button class="action-btn action-items" onclick="componentsManager.manageItens(${ordem.id})" 
                        title="Gerenciar Itens" style="background: #28a745;">
                    <i data-feather="package"></i>
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

        // Adicionar evento de clique para seleção da linha
        tr.addEventListener('click', (e) => {
            if (e.target.type !== 'checkbox' && !e.target.closest('.actions')) {
                const checkbox = tr.querySelector('input[type="checkbox"]');
                checkbox.checked = !checkbox.checked;
                this.handleRowSelection(ordem.id, checkbox.checked);
            }
        });

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
        console.log('[ComponentsManager] Abrindo modal em modo:', mode);
        
        if (!this.elements.modalOrdemCompra) {
            console.error('[ComponentsManager] Modal não encontrado!');
            return;
        }

        this.clearForm();
        this.resetWizard();
        
        if (mode === 'create') {
            this.elements.modalTitle.innerHTML = '<i data-feather="shopping-cart"></i> Nova Ordem de Compra';
            this.setDefaultDates();
            
            console.log('[ComponentsManager] Carregando produtos para reposição...');
            this.loadProdutosParaReposicao();
        } else {
            this.elements.modalTitle.innerHTML = '<i data-feather="edit-2"></i> Editar Ordem de Compra';
            if (data) {
                this.populateForm(data);
            }
        }

        this.elements.modalOrdemCompra.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        console.log('[ComponentsManager] Modal aberto com sucesso');
        
        // Refresh icons
        setTimeout(() => {
            if (typeof feather !== 'undefined') {
                feather.replace();
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

        if (this.elements.inputId) this.elements.inputId.value = data.id || '';
        if (this.elements.inputStatusOrdemCompra) this.elements.inputStatusOrdemCompra.value = data.statusOrdemCompra || 'PEND';
        if (this.elements.inputValorInicial) this.elements.inputValorInicial.value = data.valor || '';
        if (this.elements.inputDataPrev) this.elements.inputDataPrev.value = data.dataPrev || '';
        if (this.elements.inputDataOrdem) this.elements.inputDataOrdem.value = data.dataOrdem || '';
        if (this.elements.inputDataEntre) this.elements.inputDataEntre.value = data.dataEntre || '';
        if (this.elements.inputObservacoes) this.elements.inputObservacoes.value = data.observacoes || '';
    }

    /**
     * Define datas padrão para nova ordem
     */
    setDefaultDates() {
        const today = new Date().toISOString().split('T')[0];
        if (this.elements.inputDataOrdem) this.elements.inputDataOrdem.value = today;
        
        // Data prevista para 30 dias a partir de hoje
        const futureDate = new Date();
        futureDate.setDate(futureDate.getDate() + 30);
        if (this.elements.inputDataPrev) this.elements.inputDataPrev.value = futureDate.toISOString().split('T')[0];
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
        // Garantir que as datas estejam no formato correto (YYYY-MM-DD)
        const formatDateForBackend = (dateValue) => {
            if (!dateValue) return null;
            // Se já está no formato YYYY-MM-DD, retornar como está
            if (/^\d{4}-\d{2}-\d{2}$/.test(dateValue)) {
                return dateValue;
            }
            // Tentar converter para o formato correto
            const date = new Date(dateValue);
            if (!isNaN(date.getTime())) {
                return date.toISOString().split('T')[0];
            }
            return null;
        };

        // Buscar campos pelos IDs
        const idField = document.getElementById('id');
        const statusField = document.getElementById('statusOrdemCompra');
        const valorInicialField = document.getElementById('valorInicial');
        const dataPrevField = document.getElementById('dataPrev');
        const dataOrdemField = document.getElementById('dataOrdem');
        const dataEntreField = document.getElementById('dataEntre');
        const observacoesField = document.getElementById('observacoesOrdem');

        // Processar valor inicial - se vazio, usar 0
        const valorInicial = valorInicialField?.value ? 
            parseFloat(valorInicialField.value.replace(/[^\d.,]/g, '').replace(',', '.')) : 0;

        return {
            id: idField?.value || null,
            statusOrdemCompra: statusField?.value || '',
            valor: valorInicial,
            dataPrev: formatDateForBackend(dataPrevField?.value),
            dataOrdem: formatDateForBackend(dataOrdemField?.value),
            dataEntre: formatDateForBackend(dataEntreField?.value),
            observacoes: observacoesField?.value || ''
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
            const statusField = document.getElementById('statusOrdemCompra');
            this.markFieldInvalid(statusField);
            isValid = false;
        }

        // Validar datas
        if (!data.dataPrev) {
            errors.push('Data prevista é obrigatória');
            const dataPrevField = document.getElementById('dataPrev');
            this.markFieldInvalid(dataPrevField);
            isValid = false;
        }

        if (!data.dataOrdem) {
            errors.push('Data da ordem é obrigatória');
            const dataOrdemField = document.getElementById('dataOrdem');
            this.markFieldInvalid(dataOrdemField);
            isValid = false;
        }

        // Validar se data prevista não é anterior à data da ordem
        if (data.dataPrev && data.dataOrdem) {
            const dataPrev = new Date(data.dataPrev);
            const dataOrdem = new Date(data.dataOrdem);
            
            if (dataPrev < dataOrdem) {
                errors.push('Data prevista não pode ser anterior à data da ordem');
                const dataPrevField = document.getElementById('dataPrev');
                this.markFieldInvalid(dataPrevField);
                isValid = false;
            }
        }

        // Validar valor inicial se informado
        if (data.valorInicial && data.valorInicial < 0) {
            errors.push('Valor inicial deve ser maior ou igual a zero');
            const valorField = document.getElementById('valorInicial');
            this.markFieldInvalid(valorField);
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
    async viewOrdem(id) {
        try {
            console.log('Visualizando ordem:', id);
            console.log('Dados disponíveis:', this.data);
            
            // Primeiro tenta buscar nos dados locais
            let ordem = null;
            
            if (this.data && Array.isArray(this.data) && this.data.length > 0) {
                ordem = this.data.find(o => o.id == id);
                console.log('Ordem encontrada localmente:', ordem);
            }
            
            // Se não encontrou localmente, busca via API
            if (!ordem && window.api) {
                console.log('Buscando ordem via API:', id);
                try {
                    ordem = await api.get(`/api/ordens-compra/${id}`);
                    console.log('Ordem obtida via API:', ordem);
                } catch (apiError) {
                    console.warn('Erro ao buscar via API:', apiError);
                }
            }
            
            if (ordem) {
                await this.openViewModal(ordem);
            } else {
                console.error('Ordem não encontrada:', id);
                alert('Ordem de compra não encontrada');
            }
        } catch (error) {
            console.error('Erro ao visualizar ordem:', error);
            alert('Erro ao carregar detalhes da ordem');
        }
    }

    /**
     * Abre o modal de visualização com dados relacionais
     * @param {Object} ordem - Dados da ordem
     */
    async openViewModal(ordem) {
        // Elementos do modal
        const modal = document.getElementById('modalViewOrdem');
        const viewId = document.getElementById('viewId');
        const viewStatus = document.getElementById('viewStatus');
        const viewValor = document.getElementById('viewValor');
        const viewDataOrdem = document.getElementById('viewDataOrdem');
        const viewDataPrev = document.getElementById('viewDataPrev');
        const viewDataEntre = document.getElementById('viewDataEntre');
        const btnEditFromView = document.getElementById('btnEditFromView');

        // Preencher dados básicos
        if (viewId) viewId.textContent = ordem.id;
        if (viewStatus) {
            viewStatus.textContent = this.getStatusText(ordem.statusOrdemCompra);
            viewStatus.className = `value status-badge ${this.getStatusClass(ordem.statusOrdemCompra)}`;
        }
        if (viewValor) viewValor.textContent = this.formatCurrency(ordem.valor);
        if (viewDataOrdem) viewDataOrdem.textContent = this.formatDate(ordem.dataOrdem);
        if (viewDataPrev) viewDataPrev.textContent = this.formatDate(ordem.dataPrev);
        if (viewDataEntre) viewDataEntre.textContent = ordem.dataEntre ? this.formatDate(ordem.dataEntre) : 'Não informada';

        // Configurar botão editar
        if (btnEditFromView) {
            btnEditFromView.onclick = () => {
                this.closeViewModal();
                this.editOrdem(ordem.id);
            };
        }

        // Buscar dados relacionais
        await this.loadRelatedData(ordem.id);

        // Mostrar modal
        if (modal) {
            modal.style.display = 'flex';
        }
    }

    /**
     * Carrega dados relacionais da ordem (itens, lotes, movimentações)
     * @param {number} ordemId - ID da ordem
     */
    async loadRelatedData(ordemId) {
        try {
            // Verificar se o ApiManager está disponível
            if (!window.api) {
                console.warn('ApiManager não disponível - pulando dados relacionais');
                return;
            }

            // Verificar se existem endpoints para dados relacionais
            const endpoints = [
                `/api/ordens-compra/${ordemId}/itens`,
                `/api/ordens-compra/${ordemId}/lotes`, 
                `/api/ordens-compra/${ordemId}/movimentacoes`
            ];

            const promises = endpoints.map(async (endpoint) => {
                try {
                    const response = await api.get(endpoint);
                    return { endpoint, data: response, success: true };
                } catch (error) {
                    console.warn(`Endpoint ${endpoint} não disponível:`, error);
                    return { endpoint, data: null, success: false };
                }
            });

            const results = await Promise.all(promises);
            
            // Processar resultados
            results.forEach(result => {
                if (result.success && result.data && Array.isArray(result.data)) {
                    this.renderRelatedData(result.endpoint, result.data);
                } else if (result.success && result.data) {
                    console.log(`Dados recebidos de ${result.endpoint}:`, result.data);
                }
            });

        } catch (error) {
            console.error('Erro ao carregar dados relacionais:', error);
        }
    }

    /**
     * Renderiza dados relacionais no modal
     * @param {string} endpoint - Endpoint que retornou os dados
     * @param {Array} data - Dados retornados
     */
    renderRelatedData(endpoint, data) {
        // Criar seções dinâmicas baseadas nos dados relacionais
        const modalBody = document.querySelector('#modalViewOrdem .modal-body .view-sections');
        
        if (endpoint.includes('/itens') && data.length > 0) {
            this.addItensSection(modalBody, data);
        }
        
        if (endpoint.includes('/lotes') && data.length > 0) {
            this.addLotesSection(modalBody, data);
        }
        
        if (endpoint.includes('/movimentacoes') && data.length > 0) {
            this.addMovimentacoesSection(modalBody, data);
        }
    }

    /**
     * Adiciona seção de itens da ordem
     */
    addItensSection(container, itens) {
        const section = document.createElement('div');
        section.className = 'view-section';
        section.innerHTML = `
            <h3><i data-feather="package"></i> Itens da Ordem</h3>
            <div class="related-table">
                <table class="mini-table">
                    <thead>
                        <tr>
                            <th>Produto</th>
                            <th>Quantidade</th>
                            <th>Valor Unit.</th>
                            <th>Valor Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${itens.map(item => `
                            <tr>
                                <td>${item.produto?.nome || item.descricao || '-'}</td>
                                <td>${item.quantidade || '-'}</td>
                                <td>${this.formatCurrency(item.valorUnitario)}</td>
                                <td>${this.formatCurrency(item.valorTotal)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
        container.appendChild(section);
        feather.replace(); // Re-renderizar ícones
    }

    /**
     * Adiciona seção de lotes
     */
    addLotesSection(container, lotes) {
        const section = document.createElement('div');
        section.className = 'view-section';
        section.innerHTML = `
            <h3><i data-feather="layers"></i> Lotes Relacionados</h3>
            <div class="related-table">
                <table class="mini-table">
                    <thead>
                        <tr>
                            <th>Lote</th>
                            <th>Produto</th>
                            <th>Quantidade</th>
                            <th>Status</th>
                            <th>Data Venc.</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${lotes.map(lote => `
                            <tr>
                                <td>${lote.numeroLote || lote.id}</td>
                                <td>${lote.produto?.nome || '-'}</td>
                                <td>${lote.quantidade || '-'}</td>
                                <td><span class="status-badge">${lote.status || '-'}</span></td>
                                <td>${this.formatDate(lote.dataVencimento)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
        container.appendChild(section);
        feather.replace();
    }

    /**
     * Adiciona seção de movimentações contábeis
     */
    addMovimentacoesSection(container, movimentacoes) {
        const section = document.createElement('div');
        section.className = 'view-section';
        section.innerHTML = `
            <h3><i data-feather="trending-up"></i> Movimentações Contábeis</h3>
            <div class="related-table">
                <table class="mini-table">
                    <thead>
                        <tr>
                            <th>Data</th>
                            <th>Tipo</th>
                            <th>Descrição</th>
                            <th>Valor</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${movimentacoes.map(mov => `
                            <tr>
                                <td>${this.formatDate(mov.data)}</td>
                                <td>${mov.tipo || '-'}</td>
                                <td>${mov.descricao || '-'}</td>
                                <td>${this.formatCurrency(mov.valor)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
        container.appendChild(section);
        feather.replace();
    }

    /**
     * Fecha o modal de visualização
     */
    closeViewModal() {
        const modal = document.getElementById('modalViewOrdem');
        if (modal) {
            modal.style.display = 'none';
            // Limpar dados relacionais
            this.clearRelatedData();
        }
    }

    /**
     * Limpa dados relacionais do modal
     */
    clearRelatedData() {
        const modalBody = document.querySelector('#modalViewOrdem .modal-body .view-sections');
        if (modalBody) {
            // Remove apenas seções que foram adicionadas dinamicamente
            const dynamicSections = modalBody.querySelectorAll('.view-section:nth-child(n+3)');
            dynamicSections.forEach(section => section.remove());
        }
    }

    /**
     * Verifica se o modal de visualização está aberto
     */
    isViewModalOpen() {
        const modal = document.getElementById('modalViewOrdem');
        return modal && modal.style.display === 'flex';
    }

    /**
     * Edita uma ordem
     * @param {number} id - ID da ordem
     */
    editOrdem(id) {
        this.dispatchEvent('ordem:edit', { id });
    }

    /**
     * Gerencia itens de uma ordem de compra
     * @param {number} id - ID da ordem
     */
    async manageItens(id) {
        try {
            // Encontrar dados da ordem
            const ordem = this.data ? this.data.find(o => o.id == id) : null;
            
            if (!ordem) {
                notify.error('Ordem não encontrada');
                return;
            }

            // Abrir modal de itens
            this.openItensModal(ordem);
            
        } catch (error) {
            console.error('[OrdemCompraComponentsManager] Erro ao gerenciar itens:', error);
            notify.error('Erro ao abrir gerenciamento de itens');
        }
    }

    /**
     * Desativa uma ordem (agora usa autenticação)
     * @param {number} id - ID da ordem
     */
    deleteOrdem(id) {
        // Abrir modal de credenciais em vez de confirmar diretamente
        this.openCredentialsModal(id);
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
    // MÉTODOS DO MODAL DE CREDENCIAIS
    // ============================================

    /**
     * Abre o modal de credenciais para desativar uma ordem
     * @param {number} id - ID da ordem a ser desativada
     */
    openCredentialsModal(id) {
        if (!this.elements.modalCredentials || !id) return;

        this.currentDeactivationId = id;

        // Limpar formulário
        this.clearCredentialsForm();

        // Mostrar modal
        this.elements.modalCredentials.style.display = 'flex';
        
        // Focar no campo login
        if (this.elements.credentialsLogin) {
            setTimeout(() => {
                this.elements.credentialsLogin.focus();
            }, 100);
        }

        console.log('[OrdemCompraComponentsManager] Modal de credenciais aberto para ordem:', id);
    }

    /**
     * Fecha o modal de credenciais
     */
    closeCredentialsModal() {
        if (!this.elements.modalCredentials) return;

        this.elements.modalCredentials.style.display = 'none';
        this.currentDeactivationId = null;

        // Limpar formulário
        this.clearCredentialsForm();

        console.log('[OrdemCompraComponentsManager] Modal de credenciais fechado');
    }

    /**
     * Verifica se o modal de credenciais está aberto
     * @returns {boolean}
     */
    isCredentialsModalOpen() {
        return this.elements.modalCredentials && 
               this.elements.modalCredentials.style.display !== 'none';
    }

    /**
     * Limpa o formulário de credenciais
     */
    clearCredentialsForm() {
        if (!this.elements.credentialsForm) return;

        if (this.elements.credentialsLogin) this.elements.credentialsLogin.value = '';
        if (this.elements.credentialsPassword) this.elements.credentialsPassword.value = '';
        if (this.elements.deactivationReason) this.elements.deactivationReason.value = '';

        // Remover classes de erro se houver
        const inputs = this.elements.credentialsForm.querySelectorAll('input, textarea');
        inputs.forEach(input => {
            input.classList.remove('is-invalid');
        });
    }

    /**
     * Obtém dados do formulário de credenciais
     * @returns {Object} - Dados das credenciais
     */
    getCredentialsData() {
        return {
            login: this.elements.credentialsLogin?.value?.trim() || '',
            senha: this.elements.credentialsPassword?.value || '',
            motivo: this.elements.deactivationReason?.value?.trim() || ''
        };
    }

    /**
     * Valida as credenciais
     * @param {Object} credentials - Dados das credenciais
     * @returns {boolean} - Se são válidas
     */
    validateCredentials(credentials) {
        let isValid = true;

        // Validar login
        if (!credentials.login) {
            this.markFieldInvalid(this.elements.credentialsLogin);
            isValid = false;
        } else {
            this.markFieldValid(this.elements.credentialsLogin);
        }

        // Validar senha
        if (!credentials.senha) {
            this.markFieldInvalid(this.elements.credentialsPassword);
            isValid = false;
        } else {
            this.markFieldValid(this.elements.credentialsPassword);
        }

        return isValid;
    }

    /**
     * Manipula submissão do formulário de desativação
     */
    async handleDeactivationSubmit() {
        if (!this.currentDeactivationId) {
            notify.error('Erro: ID da ordem não encontrado');
            return;
        }

        const credentials = this.getCredentialsData();

        if (!this.validateCredentials(credentials)) {
            notify.error('Por favor, preencha login e senha');
            return;
        }

        try {
            // Mostrar loading
            this.showCredentialsLoading(true);

            // Disparar evento para o manager principal
            this.dispatchEvent('ordem:deactivate', {
                id: this.currentDeactivationId,
                credentials: credentials
            });

        } catch (error) {
            console.error('[OrdemCompraComponentsManager] Erro na desativação:', error);
            notify.error('Erro inesperado ao processar desativação');
            this.showCredentialsLoading(false);
        }
    }

    /**
     * Mostra/esconde loading no modal de credenciais
     * @param {boolean} show - Se deve mostrar loading
     */
    showCredentialsLoading(show = true) {
        if (!this.elements.modalCredentials) return;

        if (show) {
            this.elements.modalCredentials.classList.add('loading');
            if (this.elements.btnConfirmDeactivation) {
                this.elements.btnConfirmDeactivation.disabled = true;
                this.elements.btnConfirmDeactivation.innerHTML = '<i data-feather="loader"></i> Processando...';
            }
        } else {
            this.elements.modalCredentials.classList.remove('loading');
            if (this.elements.btnConfirmDeactivation) {
                this.elements.btnConfirmDeactivation.disabled = false;
                this.elements.btnConfirmDeactivation.innerHTML = '<i data-feather="trash-2"></i> Confirmar Remoção';
            }
        }

        // Atualizar ícones Feather
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    }

    // ============================================
    // MODAL DE GERENCIAMENTO DE ITENS
    // ============================================

    /**
     * Configura listeners do modal de itens
     */
    setupItensListeners() {
        if (!this.elements.modalItens) return;

        // Variável para armazenar itens temporários
        this.tempItens = [];
        this.currentOrdemId = null;

        // Fechar modal
        if (this.elements.btnCloseItens) {
            this.elements.btnCloseItens.addEventListener('click', () => {
                this.closeItensModal();
            });
        }

        if (this.elements.btnCancelarItens) {
            this.elements.btnCancelarItens.addEventListener('click', () => {
                this.closeItensModal();
            });
        }

        // Adicionar item
        if (this.elements.btnAdicionarItem) {
            this.elements.btnAdicionarItem.addEventListener('click', () => {
                this.adicionarItem();
            });
        }

        // Salvar itens
        if (this.elements.btnSalvarItens) {
            this.elements.btnSalvarItens.addEventListener('click', () => {
                this.salvarItens();
            });
        }

        // Cálculo automático do valor total do item
        if (this.elements.quantidadeInput && this.elements.precoUnitarioInput) {
            const calcularTotal = () => {
                const qtd = parseFloat(this.elements.quantidadeInput.value) || 0;
                const preco = parseFloat(this.elements.precoUnitarioInput.value) || 0;
                const total = qtd * preco;
                
                if (this.elements.valorTotalItem) {
                    this.elements.valorTotalItem.value = this.formatCurrency(total);
                }
            };

            this.elements.quantidadeInput.addEventListener('input', calcularTotal);
            this.elements.precoUnitarioInput.addEventListener('input', calcularTotal);
        }

        // Fechar modal clicando no overlay
        this.elements.modalItens.addEventListener('click', (e) => {
            if (e.target === this.elements.modalItens) {
                this.closeItensModal();
            }
        });
    }

    /**
     * Abre o modal de gerenciamento de itens
     * @param {Object} ordem - Dados da ordem
     */
    async openItensModal(ordem) {
        if (!this.elements.modalItens || !ordem) return;

        try {
            this.currentOrdemId = ordem.id;
            this.tempItens = [];

            // Preencher informações da ordem
            if (this.elements.ordemNumero) this.elements.ordemNumero.textContent = ordem.id;
            if (this.elements.ordemStatus) {
                this.elements.ordemStatus.textContent = this.getStatusText(ordem.statusOrdemCompra);
                this.elements.ordemStatus.className = `status-badge ${this.getStatusClass(ordem.statusOrdemCompra)}`;
            }
            if (this.elements.ordemDataPrev) this.elements.ordemDataPrev.textContent = this.formatDate(ordem.dataPrev);
            if (this.elements.ordemValorAtual) this.elements.ordemValorAtual.textContent = this.formatCurrency(ordem.valor);

            // Carregar produtos disponíveis
            await this.carregarProdutos();

            // ✅ CARREGAR ITENS EXISTENTES DA ORDEM
            await this.carregarItensExistentes(ordem.id);

            // Limpar formulário (mas manter os itens carregados)
            this.limparFormularioItem();

            // Atualizar tabela de itens
            this.atualizarTabelaItens();

            // Mostrar modal com nova classe
            this.elements.modalItens.style.display = 'flex';
            setTimeout(() => {
                this.elements.modalItens.classList.add('active');
            }, 10);
            
            // Focar no primeiro campo
            setTimeout(() => {
                if (this.elements.produtoSelect) {
                    this.elements.produtoSelect.focus();
                }
            }, 100);

            // Atualizar ícones
            if (typeof feather !== 'undefined') {
                feather.replace();
            }

        } catch (error) {
            console.error('[OrdemCompraComponentsManager] Erro ao abrir modal de itens:', error);
            notify.error('Erro ao carregar dados do modal');
        }
    }

    /**
     * Fecha o modal de itens
     */
    closeItensModal() {
        if (!this.elements.modalItens) return;
        
        // Remover classe de animação primeiro
        this.elements.modalItens.classList.remove('active');
        
        // Aguardar animação e esconder modal
        setTimeout(() => {
            this.elements.modalItens.style.display = 'none';
        }, 300);
        
        this.currentOrdemId = null;
        this.tempItens = [];
    }

    /**
     * Carrega produtos disponíveis para seleção
     */
    async carregarProdutos() {
        if (!this.elements.produtoSelect) return;

        try {
            this.elements.produtoSelect.innerHTML = '<option value="">Carregando produtos...</option>';
            
            // Verificar se apiManager existe
            if (typeof apiManager === 'undefined') {
                console.error('[ComponentsManager] ApiManager não está disponível!');
                throw new Error('ApiManager não disponível');
            }

            console.log('[ComponentsManager] Buscando todos os produtos da API...');
            
            // Buscar produtos da API real
            const produtos = await apiManager.getProdutos();
            console.log('[ComponentsManager] Produtos recebidos da API:', produtos);

            // Preencher select
            this.elements.produtoSelect.innerHTML = '<option value="">Selecione um produto...</option>';
            
            if (produtos && Array.isArray(produtos) && produtos.length > 0) {
                produtos.forEach(produto => {
                    const option = document.createElement('option');
                    // Usar o campo correto do modelo Produto (id)
                    option.value = produto.id;
                    
                    // Construir texto da opção com informações do produto
                    let textoOpcao = produto.nome || 'Produto sem nome';
                    if (produto.descricao && produto.descricao !== produto.nome) {
                        textoOpcao += ' - ' + produto.descricao;
                    }
                    
                    option.textContent = textoOpcao;
                    this.elements.produtoSelect.appendChild(option);
                });
                
                console.log(`[ComponentsManager] ${produtos.length} produtos carregados no select`);
            } else {
                this.elements.produtoSelect.innerHTML = '<option value="">Nenhum produto disponível</option>';
                console.warn('[ComponentsManager] Nenhum produto retornado pela API');
            }

        } catch (error) {
            console.error('[OrdemCompraComponentsManager] Erro ao carregar produtos:', error);
            
            // Fallback: tentar carregar produtos básicos
            try {
                console.log('[ComponentsManager] Tentando fallback com produtos básicos...');
                const produtosFallback = [
                    { id: 1, nome: 'Paracetamol 500mg', categoria: 'Analgésico' },
                    { id: 2, nome: 'Ibuprofeno 600mg', categoria: 'Anti-inflamatório' },
                    { id: 3, nome: 'Amoxicilina 500mg', categoria: 'Antibiótico' },
                    { id: 4, nome: 'Dipirona Sódica 500mg', categoria: 'Analgésico' },
                    { id: 5, nome: 'Omeprazol 20mg', categoria: 'Antiácido' },
                    { id: 6, nome: 'Losartana 50mg', categoria: 'Anti-hipertensivo' }
                ];
                
                this.elements.produtoSelect.innerHTML = '<option value="">Selecione um produto...</option>';
                produtosFallback.forEach(produto => {
                    const option = document.createElement('option');
                    option.value = produto.id;
                    option.textContent = `${produto.nome} - ${produto.categoria}`;
                    this.elements.produtoSelect.appendChild(option);
                });
                
                console.warn('[ComponentsManager] Usando produtos fallback');
                
            } catch (fallbackError) {
                console.error('[ComponentsManager] Erro também no fallback:', fallbackError);
                this.elements.produtoSelect.innerHTML = '<option value="">Erro ao carregar produtos</option>';
            }
        }
    }

    /**
     * Carrega itens existentes de uma ordem de compra
     * @param {number} ordemId - ID da ordem de compra
     */
    async carregarItensExistentes(ordemId) {
        if (!ordemId) return;

        try {
            console.log(`[ComponentsManager] Carregando itens existentes da ordem ${ordemId}...`);
            
            // Verificar se apiManager existe
            if (typeof apiManager === 'undefined') {
                console.error('[ComponentsManager] ApiManager não está disponível para carregar itens!');
                return;
            }

            // Buscar itens da ordem via API
            const response = await apiManager.getItensOrdem(ordemId);
            
            if (response && response.success && response.itens) {
                console.log(`[ComponentsManager] ${response.itens.length} itens encontrados na ordem ${ordemId}`);
                
                // Converter itens para o formato do tempItens
                this.tempItens = response.itens.map(item => {
                    // Encontrar o produto correspondente
                    const produto = this.produtosDisponiveis.find(p => p.id === item.idProduto);
                    
                    return {
                        id: item.idItemOrd, // ID do item para futuras operações
                        produtoId: item.idProduto,
                        produtoNome: produto ? produto.nome : `Produto ID ${item.idProduto}`,
                        quantidade: item.qntd,
                        precoUnitario: item.valor,
                        total: item.qntd * item.valor,
                        dataVencimento: item.dataVenc,
                        existeNoBanco: true // Flag para indicar que já existe no banco
                    };
                });

                // Atualizar resumo financeiro
                this.atualizarResumoFinanceiro();
                
                console.log(`[ComponentsManager] Itens carregados:`, this.tempItens);
                
            } else {
                console.log(`[ComponentsManager] Nenhum item encontrado na ordem ${ordemId}`);
                this.tempItens = [];
            }

        } catch (error) {
            console.error(`[ComponentsManager] Erro ao carregar itens da ordem ${ordemId}:`, error);
            
            // Em caso de erro, manter array vazio mas mostrar notificação
            this.tempItens = [];
            
            if (typeof notify !== 'undefined') {
                notify.warning('Não foi possível carregar itens existentes da ordem');
            }
        }
    }

    /**
     * Adiciona um item à lista temporária
     */
    adicionarItem() {
        try {
            const produtoId = this.elements.produtoSelect?.value;
            const quantidade = parseFloat(this.elements.quantidadeInput?.value);
            const precoUnitario = parseFloat(this.elements.precoUnitarioInput?.value);

            // Validações
            if (!produtoId) {
                notify.error('Selecione um produto');
                return;
            }

            if (!quantidade || quantidade <= 0) {
                notify.error('Quantidade deve ser maior que zero');
                return;
            }

            if (!precoUnitario || precoUnitario < 0) {
                notify.error('Preço unitário deve ser maior ou igual a zero');
                return;
            }

            // Verificar se produto já foi adicionado
            if (this.tempItens.find(item => item.produtoId == produtoId)) {
                notify.error('Este produto já foi adicionado à ordem');
                return;
            }

            // Obter nome do produto
            const produtoOption = this.elements.produtoSelect.options[this.elements.produtoSelect.selectedIndex];
            const produtoNome = produtoOption.textContent;

            // Criar item
            const item = {
                id: Date.now(), // ID temporário
                produtoId: parseInt(produtoId),
                produtoNome: produtoNome,
                quantidade: quantidade,
                precoUnitario: precoUnitario,
                total: quantidade * precoUnitario, // Usar 'total' ao invés de 'valorTotal'
                existeNoBanco: false // Flag para indicar item novo
            };

            // Adicionar à lista temporária
            this.tempItens.push(item);

            // Atualizar interface
            this.atualizarTabelaItens();
            this.limparFormularioItem();

            notify.success('Item adicionado com sucesso!');

        } catch (error) {
            console.error('[OrdemCompraComponentsManager] Erro ao adicionar item:', error);
            notify.error('Erro ao adicionar item');
        }
    }

    /**
     * Remove um item da lista temporária
     */
    removerItem(itemId) {
        this.tempItens = this.tempItens.filter(item => item.id !== itemId);
        this.atualizarTabelaItens();
        notify.success('Item removido com sucesso!');
    }

    /**
     * Atualiza a tabela de itens
     */
    atualizarTabelaItens() {
        if (!this.elements.itensTableBody) return;

        if (this.tempItens.length === 0) {
            this.elements.itensTableBody.innerHTML = `
                <tr class="empty-row">
                    <td colspan="5" class="text-center">
                        <div class="empty-state">
                            <i data-feather="package" style="font-size: 48px; opacity: 0.3;"></i>
                            <p>Nenhum item adicionado ainda</p>
                            <small>Use o formulário acima para adicionar produtos à ordem</small>
                        </div>
                    </td>
                </tr>
            `;
        } else {
            let html = '';
            this.tempItens.forEach((item, index) => {
                // Diferentes ações para itens existentes vs novos
                let acoes = '';
                if (item.existeNoBanco) {
                    // Item já salvo no banco - botões de editar e remover do banco
                    acoes = `
                        <div class="item-actions">
                            <button class="action-btn action-edit" onclick="componentsManager.editarItem(${index})" title="Editar item">
                                <i data-feather="edit-3"></i>
                            </button>
                            <button class="action-btn action-delete" onclick="componentsManager.removerItemDoBanco(${item.id}, ${index})" title="Remover do banco">
                                <i data-feather="trash-2"></i>
                            </button>
                            <span class="item-status saved" title="Item salvo no banco">
                                <i data-feather="check-circle"></i>
                            </span>
                        </div>
                    `;
                } else {
                    // Item novo (apenas temporário) - botão de remover da lista temporária
                    acoes = `
                        <div class="item-actions">
                            <button class="action-btn action-delete" onclick="componentsManager.removerItemTemporario(${index})" title="Remover da lista">
                                <i data-feather="x-circle"></i>
                            </button>
                            <span class="item-status unsaved" title="Item não salvo">
                                <i data-feather="clock"></i>
                            </span>
                        </div>
                    `;
                }

                html += `
                    <tr class="${item.existeNoBanco ? 'item-saved' : 'item-new'}">
                        <td>
                            <div class="produto-info">
                                <span class="produto-nome">${item.produtoNome}</span>
                                ${item.existeNoBanco ? '<small class="produto-id">ID: ' + item.produtoId + '</small>' : ''}
                            </div>
                        </td>
                        <td class="text-center">${item.quantidade}</td>
                        <td class="text-right">${this.formatCurrency(item.precoUnitario)}</td>
                        <td class="text-right">${this.formatCurrency(item.total)}</td>
                        <td class="text-center">${acoes}</td>
                    </tr>
                `;
            });
            this.elements.itensTableBody.innerHTML = html;
        }

        // Atualizar resumo financeiro
        this.atualizarResumoFinanceiro();

        // Atualizar ícones
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    }

    /**
     * Atualiza o resumo financeiro
     */
    atualizarResumoFinanceiro() {
        const totalItens = this.tempItens.length;
        const subtotal = this.tempItens.reduce((sum, item) => sum + item.total, 0);

        // Atualizar contadores
        if (this.elements.totalItens) this.elements.totalItens.textContent = totalItens;
        
        // Atualizar badge do cabeçalho
        const itemsCountBadge = document.getElementById('itemsCountBadge');
        if (itemsCountBadge) {
            itemsCountBadge.textContent = `${totalItens} ${totalItens === 1 ? 'item' : 'itens'}`;
        }
        
        // Atualizar valores financeiros
        if (this.elements.subtotalOrdem) this.elements.subtotalOrdem.textContent = this.formatCurrency(subtotal);
        if (this.elements.totalOrdem) this.elements.totalOrdem.textContent = this.formatCurrency(subtotal);
    }

    /**
     * Limpa o formulário de adicionar item
     */
    limparFormularioItem() {
        if (this.elements.produtoSelect) this.elements.produtoSelect.value = '';
        if (this.elements.quantidadeInput) this.elements.quantidadeInput.value = '';
        if (this.elements.precoUnitarioInput) this.elements.precoUnitarioInput.value = '';
        if (this.elements.valorTotalItem) this.elements.valorTotalItem.value = '';
    }

    /**
     * Salva os itens da ordem
     */
    async salvarItens() {
        console.log('[ComponentsManager] Iniciando salvarItens()');
        console.log('[ComponentsManager] tempItens:', this.tempItens);
        console.log('[ComponentsManager] currentOrdemId:', this.currentOrdemId);
        
        // Filtrar apenas itens novos (não salvos no banco)
        const itensNovos = this.tempItens.filter(item => !item.existeNoBanco);
        console.log('[ComponentsManager] Itens novos encontrados:', itensNovos);
        
        if (itensNovos.length === 0) {
            notify.warning('Não há itens novos para salvar');
            return;
        }

        // Verificar se existe ordem de compra
        if (!this.currentOrdemId) {
            notify.error('Erro: Nenhuma ordem de compra selecionada. Crie uma ordem primeiro!');
            return;
        }

        try {
            // Mostrar loading
            if (this.elements.btnSalvarItens) {
                this.elements.btnSalvarItens.disabled = true;
                this.elements.btnSalvarItens.innerHTML = '<i data-feather="loader"></i> Salvando...';
            }

            // Preparar dados para envio (apenas itens novos)
            const itensParaEnvio = itensNovos.map(item => ({
                produtoId: item.produtoId,
                quantidade: item.quantidade,
                precoUnitario: item.precoUnitario
            }));

            console.log('[ComponentsManager] Dados para envio:', itensParaEnvio);
            console.log('[ComponentsManager] window.apiManager disponível:', !!window.apiManager);

            // Enviar para a API
            if (window.apiManager) {
                console.log('[ComponentsManager] Chamando adicionarItensOrdem...');
                const response = await window.apiManager.adicionarItensOrdem(this.currentOrdemId, itensParaEnvio);

                notify.success(`${itensNovos.length} itens novos salvos com sucesso!`);
                
                // Fechar modal
                this.closeItensModal();

                // Recarregar lista de ordens (para atualizar valores)
                if (window.ordemCompraManager) {
                    window.ordemCompraManager.loadOrdens();
                }
            } else {
                throw new Error('ApiManager não disponível');
            }

        } catch (error) {
            console.error('[OrdemCompraComponentsManager] Erro ao salvar itens:', error);
            
            let errorMessage = 'Erro ao salvar itens da ordem';
            if (error.message.includes('404')) {
                errorMessage = 'Ordem de compra não encontrada';
            } else if (error.message.includes('400')) {
                errorMessage = 'Dados dos itens inválidos';
            } else if (error.message) {
                errorMessage = error.message;
            }
            
            notify.error(errorMessage);
        } finally {
            if (this.elements.btnSalvarItens) {
                this.elements.btnSalvarItens.disabled = false;
                this.elements.btnSalvarItens.innerHTML = '<i data-feather="check"></i> Salvar Itens';
            }
        }
    }

    /**
     * Remove um item temporário da lista (ainda não salvo)
     * @param {number} index - Índice do item na lista temporária
     */
    removerItemTemporario(index) {
        if (index >= 0 && index < this.tempItens.length) {
            const item = this.tempItens[index];
            
            // Remover da lista temporária
            this.tempItens.splice(index, 1);
            
            // Atualizar tabela
            this.atualizarTabelaItens();
            
            notify.success(`Item "${item.produtoNome}" removido da lista`);
        }
    }

    /**
     * Remove um item do banco de dados
     * @param {number} itemId - ID do item no banco
     * @param {number} index - Índice do item na lista temporária
     */
    async removerItemDoBanco(itemId, index) {
        if (!itemId || !this.currentOrdemId) {
            notify.error('Dados inválidos para remoção');
            return;
        }

        // Confirmar remoção
        if (!confirm('Tem certeza que deseja remover este item da ordem? Esta ação não pode ser desfeita.')) {
            return;
        }

        try {
            // Verificar se apiManager existe
            if (!window.apiManager) {
                throw new Error('ApiManager não disponível');
            }

            // Remover do banco via API
            const response = await window.apiManager.removerItemOrdem(this.currentOrdemId, itemId);
            
            if (response && response.success) {
                // Remover da lista temporária
                this.tempItens.splice(index, 1);
                
                // Atualizar tabela
                this.atualizarTabelaItens();
                
                notify.success('Item removido com sucesso!');
                
                // Recarregar ordens para atualizar valores
                if (window.ordemCompraManager) {
                    window.ordemCompraManager.loadOrdens();
                }
            } else {
                throw new Error(response?.message || 'Erro ao remover item');
            }

        } catch (error) {
            console.error('[ComponentsManager] Erro ao remover item do banco:', error);
            notify.error(`Erro ao remover item: ${error.message}`);
        }
    }

    /**
     * Edita um item existente
     * @param {number} index - Índice do item na lista temporária
     */
    editarItem(index) {
        if (index >= 0 && index < this.tempItens.length) {
            const item = this.tempItens[index];
            
            // Preencher o formulário com os dados do item
            if (this.elements.produtoSelect) {
                this.elements.produtoSelect.value = item.produtoId;
            }
            if (this.elements.quantidadeInput) {
                this.elements.quantidadeInput.value = item.quantidade;
            }
            if (this.elements.precoUnitarioInput) {
                this.elements.precoUnitarioInput.value = item.precoUnitario;
            }
            
            // Calcular valor total
            this.calcularValorTotal();
            
            // Armazenar índice para atualização
            this.editingItemIndex = index;
            
            // Alterar texto do botão
            if (this.elements.btnAdicionarItem) {
                this.elements.btnAdicionarItem.innerHTML = '<i data-feather="save"></i> Atualizar Item';
                this.elements.btnAdicionarItem.onclick = () => this.atualizarItemEditando();
            }
            
            notify.info(`Editando item "${item.produtoNome}"`);
        }
    }

    /**
     * Atualiza o item que está sendo editado
     */
    async atualizarItemEditando() {
        if (typeof this.editingItemIndex !== 'number') {
            notify.error('Nenhum item sendo editado');
            return;
        }

        const item = this.tempItens[this.editingItemIndex];
        if (!item) {
            notify.error('Item não encontrado');
            return;
        }

        try {
            // Validar dados do formulário
            const produtoId = parseInt(this.elements.produtoSelect?.value);
            const quantidade = parseFloat(this.elements.quantidadeInput?.value);
            const precoUnitario = parseFloat(this.elements.precoUnitarioInput?.value);

            if (!produtoId || !quantidade || !precoUnitario) {
                notify.error('Preencha todos os campos');
                return;
            }

            if (quantidade <= 0 || precoUnitario <= 0) {
                notify.error('Quantidade e preço devem ser maiores que zero');
                return;
            }

            // Se o item existe no banco, atualizar via API
            if (item.existeNoBanco && item.id) {
                if (!window.apiManager) {
                    throw new Error('ApiManager não disponível');
                }

                const dadosAtualizacao = {
                    quantidade: quantidade,
                    precoUnitario: precoUnitario
                };

                const response = await window.apiManager.atualizarItemOrdem(
                    this.currentOrdemId, 
                    item.id, 
                    dadosAtualizacao
                );

                if (response && response.success) {
                    // Atualizar item na lista temporária
                    Object.assign(item, {
                        quantidade: quantidade,
                        precoUnitario: precoUnitario,
                        total: quantidade * precoUnitario
                    });

                    notify.success('Item atualizado com sucesso!');
                    
                    // Recarregar ordens para atualizar valores
                    if (window.ordemCompraManager) {
                        window.ordemCompraManager.loadOrdens();
                    }
                } else {
                    throw new Error(response?.message || 'Erro ao atualizar item');
                }

            } else {
                // Item apenas temporário - atualizar localmente
                const produtoNome = this.elements.produtoSelect?.selectedOptions[0]?.text || `Produto ${produtoId}`;
                
                Object.assign(item, {
                    produtoId: produtoId,
                    produtoNome: produtoNome,
                    quantidade: quantidade,
                    precoUnitario: precoUnitario,
                    total: quantidade * precoUnitario
                });

                notify.success('Item atualizado na lista temporária');
            }

            // Atualizar tabela e limpar formulário
            this.atualizarTabelaItens();
            this.limparFormularioItem();
            this.cancelarEdicao();

        } catch (error) {
            console.error('[ComponentsManager] Erro ao atualizar item:', error);
            notify.error(`Erro ao atualizar item: ${error.message}`);
        }
    }

    /**
     * Cancela a edição de item
     */
    cancelarEdicao() {
        delete this.editingItemIndex;
        
        // Restaurar botão de adicionar
        if (this.elements.btnAdicionarItem) {
            this.elements.btnAdicionarItem.innerHTML = '<i data-feather="plus"></i> Adicionar Item';
            this.elements.btnAdicionarItem.onclick = () => this.adicionarItem();
        }
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
     * Formata data para o padrão brasileiro dd/mm/yyyy
     * @param {string|Date|Array|null} dateInput - Data em diversos formatos
     * @returns {string} - Data formatada (dd/mm/yyyy)
     */
    formatDate(dateInput) {
        // Verificações de segurança
        if (!dateInput || dateInput === null || dateInput === undefined) {
            return '-';
        }
        
        // Se é um array (como [2025, 6, 10]), converte para data
        if (Array.isArray(dateInput) && dateInput.length >= 3) {
            const [year, month, day] = dateInput;
            // Mês no JavaScript é 0-indexed, então subtraímos 1
            const date = new Date(year, month - 1, day);
            if (!isNaN(date.getTime())) {
                return `${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/${year}`;
            }
        }
        
        // Se é um objeto Date, formata diretamente
        if (dateInput instanceof Date) {
            if (!isNaN(dateInput.getTime())) {
                return dateInput.toLocaleDateString('pt-BR');
            }
        }
        
        // Converte para string se não for
        let dateString = typeof dateInput === 'string' ? dateInput : String(dateInput);
        
        // Se a data já está no formato brasileiro, retorna como está
        if (dateString.includes('/')) {
            return dateString;
        }
        
        try {
            // Se contém vírgulas (formato do array convertido para string)
            if (dateString.includes(',')) {
                const parts = dateString.split(',');
                if (parts.length >= 3) {
                    const year = parts[0].trim();
                    const month = parts[1].trim();
                    const day = parts[2].trim();
                    return `${day.padStart(2, '0')}/${month.padStart(2, '0')}/${year}`;
                }
            }
            
            // Se está no formato yyyy-mm-dd ou yyyy-mm-ddThh:mm:ss
            const dateOnly = dateString.split('T')[0];
            const [year, month, day] = dateOnly.split('-');
            
            if (year && month && day && year.length === 4) {
                return `${day.padStart(2, '0')}/${month.padStart(2, '0')}/${year}`;
            }
        } catch (error) {
            console.warn('Erro ao formatar data:', dateInput, error);
        }
        
        // Fallback para o método original
        try {
            const date = new Date(dateString);
            if (!isNaN(date.getTime())) {
                return date.toLocaleDateString('pt-BR');
            }
        } catch (error) {
            console.warn('Erro no fallback de formatação de data:', dateInput, error);
        }
        
        // Se tudo falhar, retorna o valor original ou '-'
        return dateInput ? String(dateInput) : '-';
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

    // ============================================
    // MÉTODOS DO WIZARD DE ORDEM DE COMPRA
    // ============================================

    /**
     * Reseta o wizard para o estado inicial
     */
    resetWizard() {
        this.currentStep = 1;
        this.ordemData = {
            informacoes: {},
            produtos: [],
            fornecedores: {},
            resumo: {}
        };
        this.updateStepDisplay();
        this.updateNavigationButtons();
    }

    /**
     * Avança para o próximo passo
     */
    nextStep() {
        if (this.validateCurrentStep()) {
            if (this.currentStep < this.totalSteps) {
                this.saveCurrentStepData();
                this.currentStep++;
                this.updateStepDisplay();
                this.updateNavigationButtons();
                this.loadStepData();
            }
        }
    }

    /**
     * Volta para o passo anterior
     */
    previousStep() {
        if (this.currentStep > 1) {
            this.currentStep--;
            this.updateStepDisplay();
            this.updateNavigationButtons();
            this.loadStepData();
        }
    }

    /**
     * Atualiza a exibição dos passos
     */
    updateStepDisplay() {
        // Atualizar progress steps
        this.elements.progressSteps.forEach((step, index) => {
            const stepNumber = index + 1;
            step.classList.remove('active', 'completed');
            
            if (stepNumber === this.currentStep) {
                step.classList.add('active');
            } else if (stepNumber < this.currentStep) {
                step.classList.add('completed');
            }
        });

        // Atualizar form steps
        this.elements.formSteps.forEach((step, index) => {
            const stepNumber = index + 1;
            step.classList.remove('active');
            
            if (stepNumber === this.currentStep) {
                step.classList.add('active');
            }
        });
    }

    /**
     * Atualiza os botões de navegação
     */
    updateNavigationButtons() {
        // Botão Anterior
        if (this.elements.btnAnterior) {
            this.elements.btnAnterior.style.display = this.currentStep > 1 ? 'flex' : 'none';
        }

        // Botão Próximo
        if (this.elements.btnProximo) {
            this.elements.btnProximo.style.display = this.currentStep < this.totalSteps ? 'flex' : 'none';
        }

        // Botão Finalizar
        if (this.elements.btnFinalizarOrdem) {
            this.elements.btnFinalizarOrdem.style.display = this.currentStep === this.totalSteps ? 'flex' : 'none';
        }
    }

    /**
     * Valida o passo atual
     */
    validateCurrentStep() {
        switch (this.currentStep) {
            case 1:
                return this.validateStep1();
            case 2:
                return this.validateStep2();
            case 3:
                return this.validateStep3();
            case 4:
                return true; // Step 4 é apenas revisão
            default:
                return false;
        }
    }

    /**
     * Valida passo 1 - Informações básicas
     */
    validateStep1() {
        const status = document.getElementById('statusOrdemCompra').value;
        const dataPrev = document.getElementById('dataPrev').value;
        const dataOrdem = document.getElementById('dataOrdem').value;

        if (!status || !dataPrev || !dataOrdem) {
            notify.error('Preencha todos os campos obrigatórios');
            return false;
        }

        const today = new Date().toISOString().split('T')[0];
        if (dataPrev <= today) {
            notify.error('Data prevista deve ser futura');
            return false;
        }

        return true;
    }

    /**
     * Valida passo 2 - Produtos
     */
    validateStep2() {
        if (this.ordemData.produtos.length === 0) {
            notify.error('Selecione pelo menos um produto');
            return false;
        }

        return true;
    }

    /**
     * Valida passo 3 - Fornecedores
     */
    validateStep3() {
        const produtosSemFornecedor = this.ordemData.produtos.filter(produto => 
            !this.ordemData.fornecedores[produto.id]
        );

        if (produtosSemFornecedor.length > 0) {
            notify.error('Selecione fornecedores para todos os produtos');
            return false;
        }

        return true;
    }

    /**
     * Salva os dados do passo atual
     */
    saveCurrentStepData() {
        switch (this.currentStep) {
            case 1:
                this.saveStep1Data();
                break;
            case 2:
                this.saveStep2Data();
                break;
            case 3:
                this.saveStep3Data();
                break;
        }
    }

    /**
     * Carrega dados do passo atual
     */
    loadStepData() {
        switch (this.currentStep) {
            case 2:
                if (this.ordemData.produtos.length === 0) {
                    this.loadProdutosParaReposicao();
                }
                break;
            case 3:
                this.loadFornecedoresParaProdutos();
                break;
            case 4:
                this.loadResumoFinal();
                break;
        }
    }

    /**
     * Carrega produtos que precisam de reposição
     */
    async loadProdutosParaReposicao() {
        console.log('[ComponentsManager] Iniciando carregamento de produtos...');
        
        if (!this.elements.produtosContainer) {
            console.error('[ComponentsManager] Container de produtos não encontrado!');
            return;
        }

        try {
            this.elements.produtosContainer.innerHTML = `
                <div class="loading-products">
                    <i data-feather="loader" class="spinning"></i>
                    Carregando produtos que precisam de reposição...
                </div>
            `;

            // Verificar se apiManager existe
            if (typeof apiManager === 'undefined') {
                console.error('[ComponentsManager] ApiManager não está disponível!');
                throw new Error('ApiManager não disponível');
            }

            // Buscar produtos da API real - apenas endpoints reais
            console.log('[ComponentsManager] Chamando apiManager.getProdutosParaReposicao()...');
            const produtos = await apiManager.getProdutosParaReposicao();
            console.log('[ComponentsManager] Produtos recebidos da API:', produtos);
            
            // Processar dados da API para o formato esperado
            console.log('[ComponentsManager] Processando produtos da API...');
            const produtosProcessados = this.processarProdutosDaAPI(produtos);
            
            this.renderProdutos(produtosProcessados);
            this.updateProdutosSummary();

        } catch (error) {
            console.error('Erro ao carregar produtos:', error);
            this.elements.produtosContainer.innerHTML = `
                <div class="error-state">
                    <i data-feather="alert-triangle"></i>
                    <p>Erro ao carregar produtos. Tente novamente.</p>
                    <button class="btn btn-primary" onclick="componentsManager.loadProdutosParaReposicao()">
                        <i data-feather="refresh-cw"></i> Tentar Novamente
                    </button>
                </div>
            `;
            
            // Refresh icons
            setTimeout(() => {
                if (typeof feather !== 'undefined') {
                    feather.replace();
                }
            }, 100);
        }
    }

    /**
     * Processa dados de produtos vindos da API para o formato esperado
     */
    processarProdutosDaAPI(produtosAPI) {
        console.log('[ComponentsManager] Processando produtos da API:', produtosAPI);
        
        return produtosAPI.map(produto => {
            console.log('[ComponentsManager] Processando produto:', produto);
            
            // Usar os nomes de campos corretos do backend - tentar várias opções
            const estoqueAtual = produto.qtdEstoque || produto.estoqueAtual || produto.stqAtual || 0;
            const estoqueMinimo = produto.stqMin || produto.estoqueMinimo || produto.stqmin || 20;
            const estoqueMaximo = produto.stqMax || produto.estoqueMaximo || produto.stqmax || 200;
            const pontoPedido = produto.pntPedido || produto.pontoPedido || produto.pntpedido || 50;
            
            // Calcular urgência baseada no estoque
            let urgencia = 'REPOSICAO';
            let quantidadeSugerida = estoqueMaximo - estoqueAtual;
            
            if (estoqueAtual <= 0) {
                urgencia = 'CRITICO';
                quantidadeSugerida = estoqueMaximo;
            } else if (estoqueAtual < estoqueMinimo) {
                urgencia = 'BAIXO';
                quantidadeSugerida = estoqueMaximo - estoqueAtual;
            } else if (estoqueAtual <= pontoPedido) {
                urgencia = 'REPOSICAO';
                quantidadeSugerida = Math.max(estoqueMaximo - estoqueAtual, estoqueMinimo);
            }
            
            const produtoProcessado = {
                id: produto.id || produto.idProduto || produto.idproduto, // Tentar diferentes formatos
                nome: produto.nome || produto.nomeProduto || produto.nomeprod || 'Produto sem nome',
                descricao: produto.descricao || 'Sem descrição',
                estoqueAtual: estoqueAtual,
                estoqueMinimo: estoqueMinimo,
                estoqueMaximo: estoqueMaximo,
                pontoPedido: pontoPedido,
                urgencia: urgencia,
                quantidadeSugerida: Math.max(1, quantidadeSugerida),
                unidadeMedida: 'UN',
                preco: 2.50 // Preço padrão, pode ser melhorado depois
            };
            
            console.log('[ComponentsManager] Produto processado:', produtoProcessado);
            return produtoProcessado;
        });
    }



    /**
     * Renderiza lista de produtos
     */
    renderProdutos(produtos) {
        if (!this.elements.produtosContainer || !produtos) return;

        this.produtosDisponiveis = produtos;
        
        const html = produtos.map(produto => {
            const urgenciaInfo = this.getUrgenciaInfo(produto.urgencia);
            const precoEstimado = (produto.preco || 0) * produto.quantidadeSugerida;
            
            return `
            <div class="product-item" data-produto-id="${produto.id}">
                <div class="product-selection">
                    <label class="checkbox-container">
                        <input type="checkbox" 
                               data-produto-id="${produto.id}"
                               onchange="componentsManager.toggleProdutoSelecionado(${produto.id}, this.checked)">
                        <span class="checkmark"></span>
                    </label>
                </div>
                
                <div class="product-info">
                    <div class="product-name">${produto.nome}</div>
                    <div class="product-details">
                        ${produto.descricao} | 
                        Estoque: ${produto.estoqueAtual} ${produto.unidadeMedida || 'UN'} | 
                        Mín: ${produto.estoqueMinimo} | 
                        Máx: ${produto.estoqueMaximo} |
                        Ponto Pedido: ${produto.pontoPedido}
                    </div>
                    <div class="product-price">
                        Preço unitário: R$ ${(produto.preco || 0).toFixed(2)} | 
                        Estimativa: R$ ${precoEstimado.toFixed(2)}
                    </div>
                </div>
                
                <div class="stock-info">
                    <div class="stock-level stock-${produto.urgencia.toLowerCase()}">
                        <i data-feather="${urgenciaInfo.icon}"></i>
                        ${urgenciaInfo.label}
                    </div>
                </div>
                
                <div class="quantity-controls">
                    <button type="button" class="qty-btn" onclick="componentsManager.decrementQuantity(${produto.id})">
                        <i data-feather="minus"></i>
                    </button>
                    <input type="number" 
                           class="qty-input" 
                           value="${produto.quantidadeSugerida}" 
                           min="1" 
                           max="${produto.estoqueMaximo}" 
                           data-produto-id="${produto.id}"
                           onchange="componentsManager.updateQuantity(${produto.id}, this.value)">
                    <button type="button" class="qty-btn" onclick="componentsManager.incrementQuantity(${produto.id})">
                        <i data-feather="plus"></i>
                    </button>
                </div>
            </div>
        `;
        }).join('');

        this.elements.produtosContainer.innerHTML = html;

        // Refresh icons
        setTimeout(() => {
            if (typeof feather !== 'undefined') {
                feather.replace();
            }
        }, 100);
    }

    /**
     * Retorna informações de urgência com ícones do Feather
     */
    getUrgenciaInfo(urgencia) {
        switch (urgencia) {
            case 'CRITICO':
                return { icon: 'alert-circle', label: 'Crítico', class: 'critical' };
            case 'BAIXO':
                return { icon: 'alert-triangle', label: 'Estoque Baixo', class: 'warning' };
            case 'REPOSICAO':
                return { icon: 'info', label: 'Ponto de Pedido', class: 'info' };
            default:
                return { icon: 'info', label: 'Normal', class: 'normal' };
        }
    }

    /**
     * Finaliza a criação da ordem de compra
     */
    async finalizarOrdemCompra() {
        try {
            this.saveCurrentStepData();
            
            const ordemCompleta = this.buildOrdemCompraData();
            
            notify.info('Criando ordem de compra...');
            
            // Simular criação da ordem
            await this.criarOrdemCompra(ordemCompleta);
            
            notify.success('Ordem de compra criada com sucesso!');
            this.closeModal();
            
            // Recarregar dados da tabela
            this.dispatchEvent('data:refresh');
            
        } catch (error) {
            console.error('Erro ao criar ordem:', error);
            notify.error('Erro ao criar ordem de compra');
        }
    }

    /**
     * Constrói dados completos da ordem de compra
     */
    buildOrdemCompraData() {
        return {
            statusOrdemCompra: this.ordemData.informacoes.status,
            dataPrev: this.ordemData.informacoes.dataPrev,
            dataOrdem: this.ordemData.informacoes.dataOrdem,
            observacoes: this.ordemData.informacoes.observacoes,
            produtos: this.ordemData.produtos,
            fornecedores: this.ordemData.fornecedores,
            valorTotal: this.calcularValorTotal()
        };
    }

    /**
     * Calcula valor total da ordem
     */
    calcularValorTotal() {
        return this.ordemData.produtos.reduce((total, produto) => {
            const fornecedor = this.ordemData.fornecedores[produto.id];
            return total + (produto.quantidade * (fornecedor?.preco || 0));
        }, 0);
    }

    // Métodos auxiliares para manipulação de produtos
    toggleProdutoSelecionado(produtoId, selected) {
        const productItem = document.querySelector(`[data-produto-id="${produtoId}"]`);
        
        if (selected) {
            const produto = this.produtosDisponiveis.find(p => p.id === produtoId);
            if (produto) {
                this.ordemData.produtos.push({
                    id: produto.id,
                    nome: produto.nome,
                    quantidade: produto.quantidadeSugerida,
                    estoqueAtual: produto.estoqueAtual,
                    estoqueMaximo: produto.estoqueMaximo,
                    preco: produto.preco || 0
                });
                
                // Adicionar classe visual de selecionado
                if (productItem) {
                    productItem.classList.add('selected');
                }
            }
        } else {
            this.ordemData.produtos = this.ordemData.produtos.filter(p => p.id !== produtoId);
            
            // Remover classe visual de selecionado
            if (productItem) {
                productItem.classList.remove('selected');
            }
        }
        
        this.updateProdutosSummary();
    }

    updateQuantity(produtoId, quantidade) {
        const produto = this.ordemData.produtos.find(p => p.id === produtoId);
        if (produto) {
            produto.quantidade = parseInt(quantidade) || 1;
            this.updateProdutosSummary();
        }
    }

    incrementQuantity(produtoId) {
        const input = document.querySelector(`[data-produto-id="${produtoId}"] .qty-input`);
        if (input) {
            const newValue = parseInt(input.value) + 1;
            const max = parseInt(input.getAttribute('max'));
            if (newValue <= max) {
                input.value = newValue;
                this.updateQuantity(produtoId, newValue);
            }
        }
    }

    decrementQuantity(produtoId) {
        const input = document.querySelector(`[data-produto-id="${produtoId}"] .qty-input`);
        if (input) {
            const newValue = Math.max(1, parseInt(input.value) - 1);
            input.value = newValue;
            this.updateQuantity(produtoId, newValue);
        }
    }

    updateProdutosSummary() {
        if (this.elements.produtosSelecionados) {
            this.elements.produtosSelecionados.textContent = 
                `${this.ordemData.produtos.length} produtos selecionados`;
        }
        
        if (this.elements.valorEstimado) {
            const valorTotal = this.ordemData.produtos.reduce((total, produto) => {
                // Buscar preço do produto original
                const produtoOriginal = this.produtosDisponiveis.find(p => p.id === produto.id);
                const precoUnitario = produtoOriginal?.preco || 0;
                return total + (produto.quantidade * precoUnitario);
            }, 0);
            
            this.elements.valorEstimado.textContent = 
                `Valor estimado: R$ ${valorTotal.toFixed(2).replace('.', ',')}`;
        }
    }

    // Métodos de salvar dados dos passos
    saveStep1Data() {
        this.ordemData.informacoes = {
            status: document.getElementById('statusOrdemCompra').value,
            prioridade: document.getElementById('prioridadeOrdem').value,
            dataPrev: document.getElementById('dataPrev').value,
            dataOrdem: document.getElementById('dataOrdem').value,
            observacoes: document.getElementById('observacoesOrdem').value
        };
    }

    saveStep2Data() {
        // Os dados dos produtos já estão sendo salvos em tempo real
    }

    saveStep3Data() {
        // Os dados dos fornecedores serão implementados no próximo passo
    }

    // Métodos placeholder para fornecedores (implementar depois)
    async loadFornecedoresParaProdutos() {
        if (!this.elements.fornecedoresContainer) return;

        if (this.ordemData.produtos.length === 0) {
            this.elements.fornecedoresContainer.innerHTML = `
                <div class="suppliers-info">
                    <div class="info-message">
                        <i data-feather="info"></i>
                        <p>Selecione produtos primeiro para ver os fornecedores disponíveis.</p>
                    </div>
                </div>
            `;
            feather.replace();
            return;
        }

        try {
            this.elements.fornecedoresContainer.innerHTML = `
                <div class="loading-suppliers">
                    <i data-feather="loader" class="spinning"></i>
                    Carregando fornecedores...
                </div>
            `;

            // Buscar fornecedores para produtos selecionados
            const fornecedoresMap = new Map();
            
            for (const produto of this.ordemData.produtos) {
                try {
                    const fornecedores = await apiManager.getFornecedoresByProduto(produto.id);
                    fornecedoresMap.set(produto.id, fornecedores);
                } catch (error) {
                    console.error(`Erro ao buscar fornecedores para produto ${produto.id} do endpoint real:`, error);
                    // Se falhar, deixar vazio - não usar dados simulados
                    fornecedoresMap.set(produto.id, []);
                }
            }

            this.renderFornecedores(fornecedoresMap);

        } catch (error) {
            console.error('Erro ao carregar fornecedores:', error);
            this.elements.fornecedoresContainer.innerHTML = `
                <div class="error-state">
                    <i data-feather="alert-triangle"></i>
                    <p>Erro ao carregar fornecedores do backend. Verifique se o servidor está funcionando.</p>
                    <button class="btn btn-primary" onclick="componentsManager.loadFornecedoresParaProdutos()">
                        <i data-feather="refresh-cw"></i> Tentar Novamente
                    </button>
                </div>
            `;
            feather.replace();
        }
    }



    renderFornecedores(fornecedoresMap) {
        if (!this.elements.fornecedoresContainer) return;

        const html = Array.from(fornecedoresMap.entries()).map(([produtoId, fornecedores]) => {
            const produto = this.ordemData.produtos.find(p => p.id == produtoId);
            
            return `
                <div class="supplier-section">
                    <h4><i data-feather="package"></i> ${produto.nome}</h4>
                    <div class="suppliers-list">
                        ${fornecedores.map(fornecedor => {
                            console.log('[ComponentsManager] Renderizando fornecedor:', fornecedor);
                            const precoUnitario = 2.50; // Preço padrão - pode ser melhorado depois
                            return `
                            <div class="supplier-option">
                                <label class="supplier-radio">
                                    <input type="radio" name="fornecedor_${produtoId}" value="${fornecedor.id}"
                                           onchange="componentsManager.selectFornecedor(${produtoId}, ${fornecedor.id}, ${precoUnitario})">
                                    <span class="radio-mark"></span>
                                </label>
                                <div class="supplier-info">
                                    <div class="supplier-name">${fornecedor.representante || 'Fornecedor'}</div>
                                    <div class="supplier-details">
                                        <span class="price">R$ ${precoUnitario.toFixed(2)}/un</span>
                                        <span class="contact"><i data-feather="phone"></i> ${fornecedor.contatoRepresentante || 'N/A'}</span>
                                        <span class="description"><i data-feather="info"></i> ${fornecedor.descricao || 'Sem descrição'}</span>
                                    </div>
                                </div>
                            </div>
                        `}).join('')}
                    </div>
                </div>
            `;
        }).join('');

        this.elements.fornecedoresContainer.innerHTML = html;
        
        // Refresh icons
        setTimeout(() => {
            if (typeof feather !== 'undefined') {
                feather.replace();
            }
        }, 100);
    }

    selectFornecedor(produtoId, fornecedorId, preco) {
        if (!this.ordemData.fornecedores) {
            this.ordemData.fornecedores = {};
        }
        
        this.ordemData.fornecedores[produtoId] = {
            id: fornecedorId,
            preco: preco
        };
    }

    loadResumoFinal() {
        // Atualizar informações básicas no resumo
        document.getElementById('reviewStatus').textContent = 
            this.ordemData.informacoes.status || '-';
        document.getElementById('reviewPrioridade').textContent = 
            this.ordemData.informacoes.prioridade || '-';
        document.getElementById('reviewDataPrev').textContent = 
            this.ordemData.informacoes.dataPrev || '-';
        document.getElementById('reviewDataOrdem').textContent = 
            this.ordemData.informacoes.dataOrdem || '-';

        // Renderizar produtos no resumo
        const reviewProdutos = document.getElementById('reviewProdutos');
        if (reviewProdutos) {
            if (this.ordemData.produtos.length === 0) {
                reviewProdutos.innerHTML = `
                    <div class="no-products">
                        <i data-feather="package"></i>
                        <p>Nenhum produto selecionado</p>
                    </div>
                `;
            } else {
                reviewProdutos.innerHTML = this.ordemData.produtos.map(produto => {
                    const fornecedor = this.ordemData.fornecedores?.[produto.id];
                    const precoUnit = fornecedor?.preco || produto.preco || 0;
                    const subtotal = produto.quantidade * precoUnit;
                    
                    return `
                        <div class="review-product-item">
                            <div class="product-summary">
                                <div class="product-name">${produto.nome}</div>
                                <div class="product-qty">Quantidade: ${produto.quantidade}</div>
                            </div>
                            <div class="product-pricing">
                                <div class="unit-price">R$ ${precoUnit.toFixed(2)}/un</div>
                                <div class="subtotal">R$ ${subtotal.toFixed(2)}</div>
                            </div>
                        </div>
                    `;
                }).join('');
            }
        }

        // Calcular e exibir totais
        const valorTotal = this.calcularValorTotal();
        document.getElementById('reviewSubtotal').textContent = `R$ ${valorTotal.toFixed(2)}`;
        document.getElementById('reviewTotal').textContent = `R$ ${valorTotal.toFixed(2)}`;
        
        // Atualizar campo hidden para envio
        document.getElementById('valor').value = valorTotal.toFixed(2);

        // Refresh icons
        setTimeout(() => {
            if (typeof feather !== 'undefined') {
                feather.replace();
            }
        }, 100);
    }

    // Métodos de filtragem de produtos
    filtrarProdutos(searchTerm) {
        const filtroUrgencia = this.elements.filtroUrgencia?.value || '';
        this.aplicarFiltros(searchTerm, filtroUrgencia);
    }

    filtrarPorUrgencia(urgencia) {
        const searchTerm = this.elements.filtrarProdutos?.value || '';
        this.aplicarFiltros(searchTerm, urgencia);
    }

    aplicarFiltros(searchTerm, urgencia) {
        const produtos = document.querySelectorAll('.product-item');
        
        produtos.forEach(item => {
            const produtoId = item.getAttribute('data-produto-id');
            const produto = this.produtosDisponiveis.find(p => p.id == produtoId);
            
            if (!produto) {
                item.style.display = 'none';
                return;
            }

            const matchesSearch = !searchTerm || 
                produto.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
                produto.descricao.toLowerCase().includes(searchTerm.toLowerCase());

            const matchesUrgencia = !urgencia || produto.urgencia === urgencia;

            item.style.display = (matchesSearch && matchesUrgencia) ? 'flex' : 'none';
        });

        // Atualizar contador de produtos visíveis
        const visibleProducts = document.querySelectorAll('.product-item[style*="flex"], .product-item:not([style*="none"])');
        console.log(`Mostrando ${visibleProducts.length} de ${produtos.length} produtos`);
    }

    async criarOrdemCompra(dados) {
        try {
            console.log('[ComponentsManager] Criando ordem completa:', dados);
            
            // Extrair itens dos dados (se houver)
            const { itens, ...dadosOrdem } = dados;
            
            // Usar o novo método do ApiManager
            if (window.apiManager && window.apiManager.criarOrdemComItens) {
                const ordemCriada = await window.apiManager.criarOrdemComItens(dadosOrdem, itens);
                console.log('[ComponentsManager] Ordem completa criada:', ordemCriada);
                return ordemCriada;
            } else {
                // Fallback: criar ordem sem itens
                const ordemCriada = await window.apiManager.createOrdemCompra(dadosOrdem);
                console.log('[ComponentsManager] Ordem criada (sem itens):', ordemCriada);
                return ordemCriada;
            }
            
        } catch (error) {
            console.error('[ComponentsManager] Erro ao criar ordem:', error);
            throw error;
        }
    }
}

// Criar instância global apenas se não existir
if (typeof window !== 'undefined' && !window.componentsManager) {
    window.componentsManager = new OrdemCompraComponentsManager();
}

// Exportar para uso em outros módulos
if (typeof module !== 'undefined' && module.exports) {
    module.exports = OrdemCompraComponentsManager;
}

console.log('[OrdemCompraComponentsManager] Inicializado com sucesso');