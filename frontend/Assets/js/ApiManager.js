/**
 * API Manager - Gerenciador centralizado de requisições para a API
 * Funcionalidades: HTTP requests, loading states, error handling
 */

class ApiManager {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
        this.defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
    }

    /**
     * Realiza requisição HTTP genérica
     */
    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: { ...this.defaultHeaders, ...options.headers },
            ...options
        };

        try {
            const response = await fetch(url, config);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }
            
            return await response.text();
        } catch (error) {
            console.error('Erro na requisição:', error);
            throw error;
        }
    }

    /**
     * GET request
     */
    async get(endpoint, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        
        return this.request(url, { method: 'GET' });
    }

    /**
     * POST request
     */
    async post(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    /**
     * PUT request
     */
    async put(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    /**
     * DELETE request
     */
    async delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }

    // === MÉTODOS ESPECÍFICOS PARA ORDEM DE COMPRA ===

    /**
     * Buscar todas as ordens de compra com paginação
     */
    async getOrdensCompra(page = 0, size = 10, filters = {}) {
        const params = { page, size, ...filters };
        return this.get('/ordens-compra', params);
    }

    /**
     * Buscar ordem de compra por ID
     */
    async getOrdemCompra(id) {
        return this.get(`/ordens-compra/${id}`);
    }

    /**
     * Criar nova ordem de compra
     */
    async createOrdemCompra(ordemData) {
        return this.post('/ordens-compra', ordemData);
    }

    /**
     * Atualizar ordem de compra
     */
    async updateOrdemCompra(id, ordemData) {
        return this.put(`/ordens-compra/${id}`, ordemData);
    }

    /**
     * Deletar ordem de compra
     */
    async deleteOrdemCompra(id) {
        return this.delete(`/ordens-compra/${id}`);
    }

    /**
     * Buscar produtos para autocomplete
     */
    async searchProdutos(query = '', limit = 20) {
        return this.get('/produtos/search', { q: query, limit });
    }

    /**
     * Buscar produto por ID
     */
    async getProduto(id) {
        return this.get(`/produtos/${id}`);
    }

    /**
     * Buscar fornecedores
     */
    async getFornecedores(query = '') {
        const params = query ? { q: query } : {};
        return this.get('/fornecedores', params);
    }

    // === MÉTODOS UTILITÁRIOS ===

    /**
     * Simular delay para demonstração de loading
     */
    async simulateDelay(ms = 1000) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * Interceptor para adicionar loading state
     */
    async withLoading(element, asyncFunction) {
        const originalText = element.textContent;
        element.classList.add('loading');
        element.disabled = true;

        try {
            const result = await asyncFunction();
            return result;
        } finally {
            element.classList.remove('loading');
            element.disabled = false;
            element.textContent = originalText;
        }
    }
}

// === DADOS MOCK PARA DESENVOLVIMENTO ===
class MockApiManager extends ApiManager {
    constructor() {
        super();
        this.mockData = {
            ordensCompra: [
                {
                    id: 1,
                    numero: 'OC-2024-001',
                    fornecedor: 'Fornecedor ABC Ltda',
                    dataEmissao: '2024-01-15',
                    dataPrevista: '2024-01-25',
                    status: 'PENDENTE',
                    valorTotal: 1250.50,
                    observacoes: 'Entrega urgente'
                },
                {
                    id: 2,
                    numero: 'OC-2024-002',
                    fornecedor: 'Distribuidora XYZ S.A.',
                    dataEmissao: '2024-01-16',
                    dataPrevista: '2024-01-30',
                    status: 'APROVADA',
                    valorTotal: 890.25,
                    observacoes: ''
                },
                {
                    id: 3,
                    numero: 'OC-2024-003',
                    fornecedor: 'Medicamentos 123',
                    dataEmissao: '2024-01-18',
                    dataPrevista: '2024-02-05',
                    status: 'RECEBIDA',
                    valorTotal: 2100.75,
                    observacoes: 'Produtos controlados'
                }
            ],
            produtos: [
                { id: 1, nome: 'Paracetamol 500mg', unidade: 'CX', precoUnitario: 12.50 },
                { id: 2, nome: 'Ibuprofeno 600mg', unidade: 'CX', precoUnitario: 18.90 },
                { id: 3, nome: 'Dipirona 500mg', unidade: 'CX', precoUnitario: 8.75 },
                { id: 4, nome: 'Amoxicilina 500mg', unidade: 'CX', precoUnitario: 25.30 }
            ],
            fornecedores: [
                { id: 1, nome: 'Fornecedor ABC Ltda', cnpj: '12.345.678/0001-90' },
                { id: 2, nome: 'Distribuidora XYZ S.A.', cnpj: '98.765.432/0001-10' },
                { id: 3, nome: 'Medicamentos 123', cnpj: '55.444.333/0001-22' }
            ]
        };
    }

    async getOrdensCompra(page = 0, size = 10) {
        await this.simulateDelay(800);
        
        const start = page * size;
        const end = start + size;
        const items = this.mockData.ordensCompra.slice(start, end);
        
        return {
            content: items,
            totalElements: this.mockData.ordensCompra.length,
            totalPages: Math.ceil(this.mockData.ordensCompra.length / size),
            number: page,
            size: size,
            first: page === 0,
            last: end >= this.mockData.ordensCompra.length
        };
    }

    async getOrdemCompra(id) {
        await this.simulateDelay(500);
        const ordem = this.mockData.ordensCompra.find(o => o.id == id);
        if (!ordem) throw new Error('Ordem de compra não encontrada');
        return ordem;
    }

    async createOrdemCompra(ordemData) {
        await this.simulateDelay(1000);
        const newOrdem = {
            id: this.mockData.ordensCompra.length + 1,
            numero: `OC-2024-${String(this.mockData.ordensCompra.length + 1).padStart(3, '0')}`,
            ...ordemData
        };
        this.mockData.ordensCompra.push(newOrdem);
        return newOrdem;
    }

    async updateOrdemCompra(id, ordemData) {
        await this.simulateDelay(800);
        const index = this.mockData.ordensCompra.findIndex(o => o.id == id);
        if (index === -1) throw new Error('Ordem de compra não encontrada');
        
        this.mockData.ordensCompra[index] = { ...this.mockData.ordensCompra[index], ...ordemData };
        return this.mockData.ordensCompra[index];
    }

    async deleteOrdemCompra(id) {
        await this.simulateDelay(600);
        const index = this.mockData.ordensCompra.findIndex(o => o.id == id);
        if (index === -1) throw new Error('Ordem de compra não encontrada');
        
        this.mockData.ordensCompra.splice(index, 1);
        return { success: true };
    }

    async searchProdutos(query = '') {
        await this.simulateDelay(300);
        if (!query) return this.mockData.produtos;
        
        return this.mockData.produtos.filter(p => 
            p.nome.toLowerCase().includes(query.toLowerCase())
        );
    }

    async getFornecedores(query = '') {
        await this.simulateDelay(400);
        if (!query) return this.mockData.fornecedores;
        
        return this.mockData.fornecedores.filter(f => 
            f.nome.toLowerCase().includes(query.toLowerCase())
        );
    }
}

// Exportar instância singleton
const apiManager = new ApiManager(); // Use ApiManager para produção
// const apiManager = new MockApiManager(); // Use MockApiManager para desenvolvimento

export default apiManager;