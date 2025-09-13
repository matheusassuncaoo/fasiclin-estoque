/**
 * OrdemCompra Components Manager - Gerenciador de componentes da interface
 * Funcionalidades: Modal, Table, Pagination, Form validation
 */

import apiManager from './ApiManager.js';
import notificationManager from './NotificationManager.js';

class OrdemCompraComponentsManager {
    constructor() {
        this.currentPage = 0;
        this.pageSize = 10;
        this.totalPages = 0;
        this.selectedOrders = new Set();
        this.isLoading = false;
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadInitialData();
        this.initializeAnimations();
    }

    /**
     * Bind event listeners
     */
    bindEvents() {
        // Modal events
        this.bindModalEvents();
        
        // Table events
        this.bindTableEvents();
        
        // Pagination events
        this.bindPaginationEvents();
        
        // Form events
        this.bindFormEvents();
        
        // Action buttons
        this.bindActionButtons();
    }

    /**
     * Modal event bindings
     */
    bindModalEvents() {
        const modal = document.getElementById('orderModal');
        const overlay = document.getElementById('modalOverlay');
        const closeBtn = document.getElementById('closeModal');
        const cancelBtn = document.getElementById('cancelOrder');

        // Fechar modal
        [overlay, closeBtn, cancelBtn].forEach(element => {
            if (element) {
                element.addEventListener('click', (e) => {
                    if (e.target === element) {
                        this.closeModal();
                    }
                });
            }
        });

        // ESC para fechar
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modal && modal.classList.contains('show')) {
                this.closeModal();
            }
        });
    }

    /**
     * Table event bindings
     */
    bindTableEvents() {
        const selectAllCheckbox = document.getElementById('selectAll');
        
        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', (e) => {
                this.handleSelectAll(e.target.checked);
            });
        }
    }

    /**
     * Pagination event bindings
     */
    bindPaginationEvents() {
        // Previous/Next buttons
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        const pageSizeSelect = document.getElementById('pageSize');

        if (prevBtn) {
            prevBtn.addEventListener('click', () => this.goToPreviousPage());
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => this.goToNextPage());
        }

        if (pageSizeSelect) {
            pageSizeSelect.addEventListener('change', (e) => {
                this.changePageSize(parseInt(e.target.value));
            });
        }
    }

    /**
     * Form event bindings
     */
    bindFormEvents() {
        const orderForm = document.getElementById('orderForm');
        const addProductBtn = document.getElementById('addProduct');
        const saveOrderBtn = document.getElementById('saveOrder');

        if (orderForm) {
            orderForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleFormSubmit();
            });
        }

        if (addProductBtn) {
            addProductBtn.addEventListener('click', () => {
                this.addProductRow();
            });
        }

        if (saveOrderBtn) {
            saveOrderBtn.addEventListener('click', () => {
                this.handleFormSubmit();
            });
        }
    }

    /**
     * Action button bindings
     */
    bindActionButtons() {
        const newOrderBtn = document.getElementById('newOrder');
        const deleteSelectedBtn = document.getElementById('deleteSelected');

        if (newOrderBtn) {
            newOrderBtn.addEventListener('click', () => {
                this.openModal();
            });
        }

        if (deleteSelectedBtn) {
            deleteSelectedBtn.addEventListener('click', () => {
                this.deleteSelectedOrders();
            });
        }
    }

    /**
     * Initialize animations
     */
    initializeAnimations() {
        // Animar elementos quando carregarem
        setTimeout(() => {
            const actionBar = document.querySelector('.action-bar');
            const tableContainer = document.querySelector('.table-container');
            const paginationContainer = document.querySelector('.pagination-container');

            if (actionBar) actionBar.classList.add('animate-in');
            if (tableContainer) tableContainer.classList.add('animate-in');
            if (paginationContainer) paginationContainer.classList.add('animate-in');
        }, 100);
    }

    /**
     * Load initial data
     */
    async loadInitialData() {
        await this.loadOrders();
    }

    /**
     * Load orders with pagination
     */
    async loadOrders() {
        if (this.isLoading) return;

        this.isLoading = true;
        this.showLoadingState();

        try {
            const response = await apiManager.getOrdensCompra(this.currentPage, this.pageSize);
            this.renderTable(response.content);
            this.updatePagination(response);
            this.selectedOrders.clear();
            this.updateActionButtons();
        } catch (error) {
            console.error('Erro ao carregar ordens:', error);
            notificationManager.error('Erro ao carregar ordens de compra');
        } finally {
            this.isLoading = false;
            this.hideLoadingState();
        }
    }

    /**
     * Render table with data
     */
    renderTable(orders) {
        const tbody = document.querySelector('#ordersTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (orders.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 40px; color: #666;">
                        <i data-feather="inbox" style="width: 48px; height: 48px; margin-bottom: 10px;"></i>
                        <div>Nenhuma ordem de compra encontrada</div>
                    </td>
                </tr>
            `;
            feather.replace();
            return;
        }

        orders.forEach(order => {
            const row = this.createTableRow(order);
            tbody.appendChild(row);
        });

        feather.replace();
    }

    /**
     * Create table row
     */
    createTableRow(order) {
        const row = document.createElement('tr');
        row.className = 'table-row';
        row.dataset.orderId = order.id;

        const statusClass = this.getStatusClass(order.status);
        const formattedValue = new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        }).format(order.valorTotal || 0);

        row.innerHTML = `
            <td class="checkbox-col">
                <label class="checkbox">
                    <input type="checkbox" onchange="componentsManager.handleRowSelect(${order.id}, this.checked)">
                    <span class="checkmark"></span>
                </label>
            </td>
            <td>${order.numero || 'N/A'}</td>
            <td>${order.fornecedor || 'N/A'}</td>
            <td>${this.formatDate(order.dataEmissao)}</td>
            <td>${this.formatDate(order.dataPrevista)}</td>
            <td><span class="status-badge status-${statusClass}">${order.status || 'PENDENTE'}</span></td>
            <td style="text-align: right; font-weight: 600;">${formattedValue}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn-icon btn-edit" onclick="componentsManager.editOrder(${order.id})" title="Editar">
                        <i data-feather="edit-2"></i>
                    </button>
                    <button class="btn-icon btn-delete" onclick="componentsManager.deleteOrder(${order.id})" title="Excluir">
                        <i data-feather="trash-2"></i>
                    </button>
                </div>
            </td>
        `;

        return row;
    }

    /**
     * Update pagination
     */
    updatePagination(response) {
        this.totalPages = response.totalPages;
        
        // Update pagination info
        const startItem = (this.currentPage * this.pageSize) + 1;
        const endItem = Math.min((this.currentPage + 1) * this.pageSize, response.totalElements);
        
        const paginationInfo = document.querySelector('.pagination-info');
        if (paginationInfo) {
            paginationInfo.innerHTML = `
                Mostrando ${startItem} a ${endItem} de ${response.totalElements} registros
            `;
        }

        // Update current page
        const currentPageEl = document.querySelector('.pagination-current');
        if (currentPageEl) {
            currentPageEl.textContent = this.currentPage + 1;
        }

        // Update buttons
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        
        if (prevBtn) {
            prevBtn.disabled = response.first;
        }
        
        if (nextBtn) {
            nextBtn.disabled = response.last;
        }
    }

    /**
     * Modal methods
     */
    openModal(orderId = null) {
        const overlay = document.getElementById('modalOverlay');
        const modal = document.getElementById('orderModal');
        
        if (overlay && modal) {
            overlay.classList.add('show');
            document.body.style.overflow = 'hidden';
            
            if (orderId) {
                this.loadOrderData(orderId);
            } else {
                this.resetForm();
            }
        }
    }

    closeModal() {
        const overlay = document.getElementById('modalOverlay');
        
        if (overlay) {
            overlay.classList.remove('show');
            document.body.style.overflow = '';
        }
    }

    /**
     * Form methods
     */
    resetForm() {
        const form = document.getElementById('orderForm');
        if (form) {
            form.reset();
            
            // Reset produto rows
            const produtosSection = document.getElementById('produtosSection');
            if (produtosSection) {
                const firstRow = produtosSection.querySelector('.product-row');
                if (firstRow) {
                    firstRow.querySelectorAll('input, select').forEach(input => {
                        input.value = '';
                    });
                }
                
                // Remove extra rows
                const extraRows = produtosSection.querySelectorAll('.product-row:not(:first-child)');
                extraRows.forEach(row => row.remove());
            }
        }
    }

    async handleFormSubmit() {
        const formData = this.collectFormData();
        
        if (!this.validateForm(formData)) {
            return;
        }

        const saveBtn = document.getElementById('saveOrder');
        
        try {
            await apiManager.withLoading(saveBtn, async () => {
                if (formData.id) {
                    await apiManager.updateOrdemCompra(formData.id, formData);
                    notificationManager.success('Ordem de compra atualizada com sucesso!');
                } else {
                    await apiManager.createOrdemCompra(formData);
                    notificationManager.success('Ordem de compra criada com sucesso!');
                }
            });

            this.closeModal();
            await this.loadOrders();
            
        } catch (error) {
            console.error('Erro ao salvar ordem:', error);
            notificationManager.error('Erro ao salvar ordem de compra');
        }
    }

    /**
     * Utility methods
     */
    formatDate(dateString) {
        return new Date(dateString + 'T00:00:00').toLocaleDateString('pt-BR');
    }

    getStatusClass(status) {
        const statusMap = {
            'PENDENTE': 'warning',
            'APROVADA': 'success',
            'RECEBIDA': 'info',
            'CANCELADA': 'danger'
        };
        return statusMap[status] || 'secondary';
    }

    showLoadingState() {
        const tbody = document.querySelector('#ordersTable tbody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 40px;">
                        <div class="loading"></div>
                        <div style="margin-top: 10px; color: #666;">Carregando...</div>
                    </td>
                </tr>
            `;
        }
    }

    hideLoadingState() {
        // Loading state will be replaced by actual data
    }

    // === MÉTODOS PÚBLICOS CHAMADOS PELOS EVENT HANDLERS ===

    handleRowSelect(orderId, isSelected) {
        if (isSelected) {
            this.selectedOrders.add(orderId);
        } else {
            this.selectedOrders.delete(orderId);
        }
        this.updateActionButtons();
    }

    handleSelectAll(isSelected) {
        const checkboxes = document.querySelectorAll('#ordersTable tbody input[type="checkbox"]');
        checkboxes.forEach(cb => {
            cb.checked = isSelected;
            const orderId = parseInt(cb.closest('tr').dataset.orderId);
            if (isSelected) {
                this.selectedOrders.add(orderId);
            } else {
                this.selectedOrders.delete(orderId);
            }
        });
        this.updateActionButtons();
    }

    updateActionButtons() {
        const deleteBtn = document.getElementById('deleteSelected');
        if (deleteBtn) {
            deleteBtn.disabled = this.selectedOrders.size === 0;
        }
    }

    async editOrder(orderId) {
        this.openModal(orderId);
    }

    async deleteOrder(orderId) {
        if (confirm('Tem certeza que deseja excluir esta ordem de compra?')) {
            try {
                await apiManager.deleteOrdemCompra(orderId);
                notificationManager.success('Ordem de compra excluída com sucesso!');
                await this.loadOrders();
            } catch (error) {
                console.error('Erro ao excluir ordem:', error);
                notificationManager.error('Erro ao excluir ordem de compra');
            }
        }
    }

    async deleteSelectedOrders() {
        if (this.selectedOrders.size === 0) return;

        const count = this.selectedOrders.size;
        if (confirm(`Tem certeza que deseja excluir ${count} ordem(ns) de compra selecionada(s)?`)) {
            try {
                for (const orderId of this.selectedOrders) {
                    await apiManager.deleteOrdemCompra(orderId);
                }
                
                notificationManager.success(`${count} ordem(ns) excluída(s) com sucesso!`);
                await this.loadOrders();
            } catch (error) {
                console.error('Erro ao excluir ordens:', error);
                notificationManager.error('Erro ao excluir ordens de compra');
            }
        }
    }

    goToPreviousPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadOrders();
        }
    }

    goToNextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.loadOrders();
        }
    }

    changePageSize(newSize) {
        this.pageSize = newSize;
        this.currentPage = 0;
        this.loadOrders();
    }

    // Placeholder methods (to be implemented)
    collectFormData() {
        // TODO: Implement form data collection
        return {};
    }

    validateForm(formData) {
        // TODO: Implement form validation
        return true;
    }

    loadOrderData(orderId) {
        // TODO: Implement order data loading for edit
    }

    addProductRow() {
        // TODO: Implement add product row
    }
}

// Initialize global instance
const componentsManager = new OrdemCompraComponentsManager();

// Make it globally accessible for onclick handlers
window.componentsManager = componentsManager;

export default componentsManager;