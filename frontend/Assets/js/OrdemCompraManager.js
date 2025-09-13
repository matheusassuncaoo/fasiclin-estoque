/**
 * OrdemCompra Manager - Controlador principal da página
 * Coordena todos os componentes e funcionalidades
 */

import apiManager from './ApiManager.js';
import notificationManager from './NotificationManager.js';
import componentsManager from './OrdemCompraComponentsManager.js';

class OrdemCompraManager {
    constructor() {
        this.currentOrder = null;
        this.produtos = [];
        this.fornecedores = [];
        
        this.init();
    }

    init() {
        this.initializeFeatherIcons();
        this.loadReferenceData();
        this.setupProductAutocomplete();
        this.setupFornecedorAutocomplete();
        this.setupFormValidation();
        
        console.log('OrdemCompra Manager inicializado com sucesso!');
    }

    /**
     * Initialize Feather Icons
     */
    initializeFeatherIcons() {
        if (window.feather) {
            feather.replace();
        } else {
            // Aguardar carregamento do Feather
            const checkFeather = setInterval(() => {
                if (window.feather) {
                    feather.replace();
                    clearInterval(checkFeather);
                }
            }, 100);
        }
    }

    /**
     * Load reference data (produtos, fornecedores)
     */
    async loadReferenceData() {
        try {
            const [produtos, fornecedores] = await Promise.all([
                apiManager.searchProdutos(),
                apiManager.getFornecedores()
            ]);
            
            this.produtos = produtos;
            this.fornecedores = fornecedores;
            
            this.populateFornecedorSelect();
            
        } catch (error) {
            console.error('Erro ao carregar dados de referência:', error);
        }
    }

    /**
     * Setup produto autocomplete
     */
    setupProductAutocomplete() {
        document.addEventListener('change', (e) => {
            if (e.target.classList.contains('produto-select')) {
                this.handleProdutoSelect(e.target);
            }
        });
    }

    /**
     * Setup fornecedor autocomplete
     */
    setupFornecedorAutocomplete() {
        const fornecedorSelect = document.getElementById('fornecedor');
        if (fornecedorSelect) {
            fornecedorSelect.addEventListener('change', (e) => {
                this.handleFornecedorSelect(e.target.value);
            });
        }
    }

    /**
     * Setup form validation
     */
    setupFormValidation() {
        // Validation for required fields
        document.addEventListener('blur', (e) => {
            if (e.target.hasAttribute('required')) {
                this.validateField(e.target);
            }
        }, true);

        // Real-time calculation
        document.addEventListener('input', (e) => {
            if (e.target.classList.contains('quantidade-input') || 
                e.target.classList.contains('preco-input')) {
                this.calculateRowTotal(e.target.closest('.product-row'));
                this.calculateOrderTotal();
            }
        });
    }

    /**
     * Populate fornecedor select
     */
    populateFornecedorSelect() {
        const fornecedorSelect = document.getElementById('fornecedor');
        if (!fornecedorSelect || this.fornecedores.length === 0) return;

        fornecedorSelect.innerHTML = '<option value="">Selecione um fornecedor</option>';
        
        this.fornecedores.forEach(fornecedor => {
            const option = document.createElement('option');
            option.value = fornecedor.id;
            option.textContent = fornecedor.nome;
            fornecedorSelect.appendChild(option);
        });
    }

    /**
     * Handle produto selection
     */
    async handleProdutoSelect(selectElement) {
        const produtoId = selectElement.value;
        const row = selectElement.closest('.product-row');
        
        if (!produtoId || !row) return;

        try {
            const produto = await apiManager.getProduto(produtoId);
            
            // Preencher campos do produto
            const unidadeInput = row.querySelector('.unidade-display');
            const precoInput = row.querySelector('.preco-input');
            
            if (unidadeInput) unidadeInput.textContent = produto.unidade;
            if (precoInput) precoInput.value = produto.precoUnitario.toFixed(2);
            
            this.calculateRowTotal(row);
            this.calculateOrderTotal();
            
        } catch (error) {
            console.error('Erro ao carregar produto:', error);
            notificationManager.error('Erro ao carregar dados do produto');
        }
    }

    /**
     * Handle fornecedor selection
     */
    handleFornecedorSelect(fornecedorId) {
        // Additional logic for fornecedor selection if needed
        console.log('Fornecedor selecionado:', fornecedorId);
    }

    /**
     * Add new product row
     */
    addProductRow() {
        const container = document.getElementById('produtosSection');
        if (!container) return;

        const newRow = this.createProductRow();
        container.appendChild(newRow);
        
        // Populate produto select
        this.populateProdutoSelect(newRow.querySelector('.produto-select'));
        
        // Initialize feather icons
        feather.replace();
    }

    /**
     * Create product row element
     */
    createProductRow() {
        const row = document.createElement('div');
        row.className = 'product-row';
        
        row.innerHTML = `
            <div class="form-group">
                <select class="form-select produto-select" required>
                    <option value="">Selecione um produto</option>
                </select>
            </div>
            <div class="form-group">
                <input type="number" class="form-input quantidade-input" placeholder="Qtd" min="1" required>
            </div>
            <div class="form-group">
                <div class="unit-display unidade-display">-</div>
            </div>
            <div class="form-group">
                <div class="price-input">
                    <span class="currency">R$</span>
                    <input type="number" class="form-input preco-input" step="0.01" min="0" required>
                </div>
            </div>
            <div class="form-group">
                <div class="price-input">
                    <span class="currency">R$</span>
                    <input type="number" class="form-input total-input" step="0.01" readonly>
                </div>
            </div>
            <div>
                <button type="button" class="btn-icon btn-delete" onclick="this.closest('.product-row').remove(); ordemCompraManager.calculateOrderTotal();" title="Remover">
                    <i data-feather="trash-2"></i>
                </button>
            </div>
        `;

        return row;
    }

    /**
     * Populate produto select in a row
     */
    populateProdutoSelect(selectElement) {
        if (!selectElement || this.produtos.length === 0) return;

        // Clear existing options except first
        selectElement.innerHTML = '<option value="">Selecione um produto</option>';
        
        this.produtos.forEach(produto => {
            const option = document.createElement('option');
            option.value = produto.id;
            option.textContent = produto.nome;
            selectElement.appendChild(option);
        });
    }

    /**
     * Calculate row total
     */
    calculateRowTotal(row) {
        if (!row) return;

        const quantidadeInput = row.querySelector('.quantidade-input');
        const precoInput = row.querySelector('.preco-input');
        const totalInput = row.querySelector('.total-input');

        if (!quantidadeInput || !precoInput || !totalInput) return;

        const quantidade = parseFloat(quantidadeInput.value) || 0;
        const preco = parseFloat(precoInput.value) || 0;
        const total = quantidade * preco;

        totalInput.value = total.toFixed(2);
    }

    /**
     * Calculate order total
     */
    calculateOrderTotal() {
        const totalInputs = document.querySelectorAll('.total-input');
        let orderTotal = 0;

        totalInputs.forEach(input => {
            const value = parseFloat(input.value) || 0;
            orderTotal += value;
        });

        // Update total display if exists
        const totalDisplay = document.getElementById('valorTotal');
        if (totalDisplay) {
            totalDisplay.textContent = new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
            }).format(orderTotal);
        }

        return orderTotal;
    }

    /**
     * Validate field
     */
    validateField(field) {
        const isValid = field.checkValidity();
        
        field.classList.toggle('invalid', !isValid);
        
        // Remove existing error message
        const existingError = field.parentNode.querySelector('.error-message');
        if (existingError) {
            existingError.remove();
        }

        // Add error message if invalid
        if (!isValid) {
            const errorMessage = document.createElement('div');
            errorMessage.className = 'error-message';
            errorMessage.textContent = field.validationMessage;
            errorMessage.style.cssText = `
                color: #ef4444;
                font-size: 12px;
                margin-top: 4px;
            `;
            field.parentNode.appendChild(errorMessage);
        }

        return isValid;
    }

    /**
     * Collect form data
     */
    collectFormData() {
        const form = document.getElementById('orderForm');
        if (!form) return null;

        const formData = new FormData(form);
        const data = {
            fornecedor: formData.get('fornecedor'),
            dataEmissao: formData.get('dataEmissao'),
            dataPrevista: formData.get('dataPrevista'),
            observacoes: formData.get('observacoes'),
            produtos: []
        };

        // Collect produtos
        const productRows = document.querySelectorAll('.product-row');
        productRows.forEach(row => {
            const produtoId = row.querySelector('.produto-select')?.value;
            const quantidade = row.querySelector('.quantidade-input')?.value;
            const precoUnitario = row.querySelector('.preco-input')?.value;

            if (produtoId && quantidade && precoUnitario) {
                data.produtos.push({
                    produtoId: parseInt(produtoId),
                    quantidade: parseInt(quantidade),
                    precoUnitario: parseFloat(precoUnitario)
                });
            }
        });

        return data;
    }

    /**
     * Validate complete form
     */
    validateForm(data) {
        const errors = [];

        if (!data.fornecedor) {
            errors.push('Fornecedor é obrigatório');
        }

        if (!data.dataEmissao) {
            errors.push('Data de emissão é obrigatória');
        }

        if (!data.dataPrevista) {
            errors.push('Data prevista é obrigatória');
        }

        if (data.produtos.length === 0) {
            errors.push('Pelo menos um produto deve ser adicionado');
        }

        if (errors.length > 0) {
            notificationManager.error('Corrija os seguintes erros:\n' + errors.join('\n'));
            return false;
        }

        return true;
    }

    /**
     * Load order data for editing
     */
    async loadOrderData(orderId) {
        try {
            const order = await apiManager.getOrdemCompra(orderId);
            this.currentOrder = order;
            
            // Populate form fields
            this.populateForm(order);
            
        } catch (error) {
            console.error('Erro ao carregar ordem:', error);
            notificationManager.error('Erro ao carregar dados da ordem');
        }
    }

    /**
     * Populate form with order data
     */
    populateForm(order) {
        // Populate basic fields
        const fields = {
            'fornecedor': order.fornecedorId,
            'dataEmissao': order.dataEmissao,
            'dataPrevista': order.dataPrevista,
            'observacoes': order.observacoes
        };

        Object.entries(fields).forEach(([fieldName, value]) => {
            const field = document.getElementById(fieldName);
            if (field && value) {
                field.value = value;
            }
        });

        // TODO: Populate produtos if order has items
    }

    /**
     * Reset form to initial state
     */
    resetForm() {
        const form = document.getElementById('orderForm');
        if (form) {
            form.reset();
            
            // Remove extra product rows
            const productRows = document.querySelectorAll('.product-row:not(:first-child)');
            productRows.forEach(row => row.remove());
            
            // Reset first row
            const firstRow = document.querySelector('.product-row');
            if (firstRow) {
                firstRow.querySelectorAll('input, select').forEach(input => {
                    input.value = '';
                });
                
                const unidadeDisplay = firstRow.querySelector('.unidade-display');
                if (unidadeDisplay) {
                    unidadeDisplay.textContent = '-';
                }
            }
        }

        this.currentOrder = null;
        this.calculateOrderTotal();
    }

    /**
     * Export methods for global access
     */
    getPublicMethods() {
        return {
            addProductRow: () => this.addProductRow(),
            collectFormData: () => this.collectFormData(),
            validateForm: (data) => this.validateForm(data),
            loadOrderData: (orderId) => this.loadOrderData(orderId),
            resetForm: () => this.resetForm(),
            calculateOrderTotal: () => this.calculateOrderTotal()
        };
    }
}

// Initialize manager
const ordemCompraManager = new OrdemCompraManager();

// Make public methods globally accessible
window.ordemCompraManager = ordemCompraManager;

// Extend componentsManager with form methods
if (window.componentsManager) {
    const publicMethods = ordemCompraManager.getPublicMethods();
    Object.assign(window.componentsManager, publicMethods);
}

export default ordemCompraManager;