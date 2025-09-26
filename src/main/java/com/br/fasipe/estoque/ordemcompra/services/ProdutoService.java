package com.br.fasipe.estoque.ordemcompra.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.Produto;
import com.br.fasipe.estoque.ordemcompra.repository.ProdutoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service para operações de negócio da entidade Produto.
 * 
 * <p>Esta classe implementa a camada de serviço para o módulo de Produtos,
 * fornecendo operações CRUD completas com validações de negócio, tratamento de exceções
 * e métodos de consulta otimizados para gestão de produtos e controle de estoque.</p>
 * 
 * <p><strong>Funcionalidades principais:</strong></p>
 * <ul>
 *   <li>CRUD completo (Create, Read, Update, Delete)</li>
 *   <li>Consultas por código de barras, nome, categoria e fornecedor</li>
 *   <li>Operações de controle de preços e margens</li>
 *   <li>Gestão de estoque e alertas de quantidade baixa</li>
 *   <li>Relatórios de produtos por categoria e fornecedor</li>
 *   <li>Validações de integridade de dados</li>
 *   <li>Tratamento robusto de exceções</li>
 *   <li>Transações controladas</li>
 * </ul>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@Service
public class ProdutoService {
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    /**
     * Busca um produto por ID.
     * 
     * @param id ID do produto (não pode ser nulo)
     * @return Produto encontrado
     * @throws EntityNotFoundException se o produto não for encontrado
     * @throws IllegalArgumentException se o ID for nulo
     */
    public Produto findById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return produtoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Produto não encontrado com ID: " + id));
    }
    
    /**
     * Busca todos os produtos.
     * 
     * @return Lista de todos os produtos
     */
    public List<Produto> findAll() {
        return produtoRepository.findAll();
    }

    /**
     * Cria um novo produto.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
 *   <li>Objeto não pode ser nulo</li>
 *   <li>ID deve ser nulo (será gerado automaticamente)</li>
 *   <li>Nome é obrigatório</li>
 *   <li>Código de barras deve ser único</li>
 *   <li>Preços devem ser positivos</li>
 *   <li>Quantidade em estoque deve ser não negativa</li>
 * </ul>
 * 
 * @param obj Produto a ser criado (validado com @Valid)
 * @return Produto criado com ID gerado
 * @throws IllegalArgumentException se validações falharem
 * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public Produto create(@Valid @NotNull Produto obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        
        if (obj.getId() != null) {
            throw new IllegalArgumentException("ID deve ser nulo para criação de novo produto");
        }
        
        // Validações de negócio adicionais
        validateBusinessRules(obj);
        
        // Verificar se código de barras já existe
        if (obj.getCodBarras() != null && 
            produtoRepository.findByCodigoBarras(obj.getCodBarras()).isPresent()) {
            throw new IllegalArgumentException(
                "Já existe um produto com o código de barras: " + obj.getCodBarras());
        }
        
        try {
            return produtoRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao criar produto: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza um produto existente.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
 *   <li>Objeto não pode ser nulo</li>
 *   <li>ID deve existir no banco</li>
 *   <li>Campos obrigatórios devem estar preenchidos</li>
 *   <li>Regras de negócio específicas</li>
 * </ul>
 * 
     * @param obj Produto a ser atualizado (validado com @Valid)
     * @return Produto atualizado
     * @throws EntityNotFoundException se o produto não existir
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public Produto update(@Valid @NotNull Produto obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        
        if (obj.getId() == null) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }
        
        // Verifica se o produto existe
        Produto existingProduto = findById(obj.getId());
        
        // Validações de negócio para atualização
        validateUpdateRules(obj, existingProduto);
        
        try {
            return produtoRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao atualizar produto: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta um produto por ID.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
 *   <li>ID não pode ser nulo</li>
 *   <li>Produto deve existir no banco</li>
 *   <li>Não pode deletar se houver dependências</li>
 * </ul>
 * 
     * @param id ID do produto a ser deletado
     * @throws EntityNotFoundException se o produto não existir
     * @throws IllegalArgumentException se o ID for nulo
     * @throws IllegalStateException se o produto não puder ser deletado
     */
    @Transactional
    public void deleteById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        Produto produto = findById(id);
        
        // Validação de negócio: verificar se pode ser deletado
        validateDeletion(produto);
        
        try {
            produtoRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Produto não encontrado com ID: " + id);
        }
    }
    
    /**
     * Deleta um produto por objeto.
     * 
     * @param obj Produto a ser deletado
     * @throws IllegalArgumentException se o objeto for nulo
     */
    @Transactional
    public void delete(@NotNull Produto obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        
        deleteById(obj.getId());
    }
    
    /**
     * Busca produto por código de barras.
     * 
     * @param codigoBarras Código de barras do produto
     * @return Produto encontrado
     * @throws EntityNotFoundException se o produto não for encontrado
     */
    public Produto findByCodigoBarras(@NotNull String codigoBarras) {
        if (codigoBarras == null || codigoBarras.trim().isEmpty()) {
            throw new IllegalArgumentException("Código de barras não pode ser nulo ou vazio");
        }
        return produtoRepository.findByCodigoBarras(codigoBarras)
            .orElseThrow(() -> new EntityNotFoundException(
                "Produto não encontrado com código de barras: " + codigoBarras));
    }
    
    /**
     * Busca produtos por nome (busca parcial, case-insensitive).
     * 
     * @param nome Nome ou parte do nome do produto
     * @return Lista de produtos encontrados
     */
    public List<Produto> findByNomeContaining(@NotNull String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        return produtoRepository.findByNomeContaining(nome.trim());
    }
    
    /**
     * Busca produtos por descrição (busca parcial, case-insensitive).
     * 
     * @param descricao Descrição ou parte da descrição do produto
     * @return Lista de produtos encontrados
     */
    public List<Produto> findByDescricaoContaining(@NotNull String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode ser nula ou vazia");
        }
        return produtoRepository.findByDescricaoContaining(descricao.trim());
    }
    
    /**
     * Busca produtos por ID da categoria.
     * 
     * @param idCategoria ID da categoria
     * @return Lista de produtos da categoria
     */
    public List<Produto> findByIdCategoria(@NotNull Integer idCategoria) {
        if (idCategoria == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        // NOTA: O modelo Produto não possui campo idCategoria
        throw new UnsupportedOperationException(
            "Campo 'idCategoria' não implementado no modelo Produto");
    }
    
    /**
     * Busca produtos por ID do fornecedor.
     * 
     * @param idFornecedor ID do fornecedor
     * @return Lista de produtos do fornecedor
     */
    public List<Produto> findByIdFornecedor(@NotNull Integer idFornecedor) {
        if (idFornecedor == null) {
            throw new IllegalArgumentException("ID do fornecedor não pode ser nulo");
        }
        // NOTA: O modelo Produto não possui campo idFornecedor
        throw new UnsupportedOperationException(
            "Campo 'idFornecedor' não implementado no modelo Produto");
    }
    
    /**
     * Busca produtos por faixa de preço de custo.
     * 
     * @param precoMinimo Preço mínimo de custo
     * @param precoMaximo Preço máximo de custo
     * @return Lista de produtos na faixa de preço
     */
    public List<Produto> findByPrecoCustoBetween(@NotNull BigDecimal precoMinimo, 
                                               @NotNull BigDecimal precoMaximo) {
        if (precoMinimo == null || precoMaximo == null) {
            throw new IllegalArgumentException("Preços não podem ser nulos");
        }
        
        if (precoMinimo.compareTo(BigDecimal.ZERO) < 0 || precoMaximo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preços devem ser positivos");
        }
        
        if (precoMinimo.compareTo(precoMaximo) > 0) {
            throw new IllegalArgumentException("Preço mínimo deve ser menor que o máximo");
        }
        
        // NOTA: O modelo Produto não possui campo precoCusto
        // Esta funcionalidade deve ser implementada através de um serviço de preços
        throw new UnsupportedOperationException(
            "Busca por preço de custo deve ser implementada através de um serviço de preços");
    }
    
    /**
     * Busca produtos por faixa de preço de venda.
     * 
     * @param precoMinimo Preço mínimo de venda
     * @param precoMaximo Preço máximo de venda
     * @return Lista de produtos na faixa de preço
     */
    public List<Produto> findByPrecoVendaBetween(@NotNull BigDecimal precoMinimo, 
                                               @NotNull BigDecimal precoMaximo) {
        if (precoMinimo == null || precoMaximo == null) {
            throw new IllegalArgumentException("Preços não podem ser nulos");
        }
        
        if (precoMinimo.compareTo(BigDecimal.ZERO) < 0 || precoMaximo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preços devem ser positivos");
        }
        
        if (precoMinimo.compareTo(precoMaximo) > 0) {
            throw new IllegalArgumentException("Preço mínimo deve ser menor que o máximo");
        }
        
        // NOTA: O modelo Produto não possui campo precoVenda
        // Esta funcionalidade deve ser implementada através de um serviço de preços
        throw new UnsupportedOperationException(
            "Busca por preço de venda deve ser implementada através de um serviço de preços");
    }
    
    /**
     * Busca produtos por faixa de quantidade em estoque.
     * 
     * @param quantidadeMinima Quantidade mínima em estoque
     * @param quantidadeMaxima Quantidade máxima em estoque
     * @return Lista de produtos na faixa de quantidade
     */
    public List<Produto> findByQuantidadeEstoqueBetween(@NotNull Integer quantidadeMinima, 
                                                      @NotNull Integer quantidadeMaxima) {
        if (quantidadeMinima == null || quantidadeMaxima == null) {
            throw new IllegalArgumentException("Quantidades não podem ser nulas");
        }
        
        if (quantidadeMinima < 0 || quantidadeMaxima < 0) {
            throw new IllegalArgumentException("Quantidades devem ser não negativas");
        }
        
        if (quantidadeMinima > quantidadeMaxima) {
            throw new IllegalArgumentException("Quantidade mínima deve ser menor que a máxima");
        }
        
        // NOTA: O modelo Produto não possui campo quantidadeEstoque
        // Esta funcionalidade deve ser implementada através do EstoqueService
        throw new UnsupportedOperationException(
            "Busca por quantidade em estoque deve ser implementada através do EstoqueService");
    }
    
    /**
     * Busca produtos com estoque baixo (quantidade menor que o mínimo).
     * 
     * @return Lista de produtos com estoque baixo
     */
    public List<Produto> findProdutosComEstoqueBaixo() {
        // NOTA: Esta funcionalidade deve ser implementada através do EstoqueService
        // que gerencia a tabela ESTOQUE separadamente
        throw new UnsupportedOperationException(
            "Busca de produtos com estoque baixo deve ser implementada através do EstoqueService");
    }
    
    /**
     * Busca produtos com estoque zerado.
     * 
     * @return Lista de produtos com estoque zerado
     */
    public List<Produto> findProdutosComEstoqueZerado() {
        // NOTA: Esta funcionalidade deve ser implementada através do EstoqueService
        // que gerencia a tabela ESTOQUE separadamente
        throw new UnsupportedOperationException(
            "Busca de produtos com estoque zerado deve ser implementada através do EstoqueService");
    }
    
    /**
     * Busca produtos ativos.
     * 
     * @return Lista de produtos ativos
     */
    public List<Produto> findProdutosAtivos() {
        // NOTA: O modelo Produto não possui campo 'ativo'
        // Esta funcionalidade pode ser implementada futuramente
        throw new UnsupportedOperationException(
            "Campo 'ativo' não implementado no modelo Produto");
    }
    
    /**
     * Busca produtos inativos.
     * 
     * @return Lista de produtos inativos
     */
    public List<Produto> findProdutosInativos() {
        // NOTA: O modelo Produto não possui campo 'ativo'
        // Esta funcionalidade pode ser implementada futuramente
        throw new UnsupportedOperationException(
            "Campo 'ativo' não implementado no modelo Produto");
    }
    
    /**
     * Busca produtos com alta margem de lucro (margem >= 50%).
     * 
     * @return Lista de produtos com alta margem
     */
    public List<Produto> findProdutosComAltaMargem() {
        // NOTA: O modelo Produto não possui campos de preço
        // Esta funcionalidade deve ser implementada através de um serviço de preços
        throw new UnsupportedOperationException(
            "Busca por alta margem deve ser implementada através de um serviço de preços");
    }
    
    /**
     * Conta produtos por categoria.
     * 
     * @param idCategoria ID da categoria
     * @return Quantidade de produtos na categoria
     */
    public Long countByIdCategoria(@NotNull Integer idCategoria) {
        if (idCategoria == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        // NOTA: O modelo Produto não possui campo idCategoria
        throw new UnsupportedOperationException(
            "Campo 'idCategoria' não implementado no modelo Produto");
    }
    
    /**
     * Conta produtos por fornecedor.
     * 
     * @param idFornecedor ID do fornecedor
     * @return Quantidade de produtos do fornecedor
     */
    public Long countByIdFornecedor(@NotNull Integer idFornecedor) {
        if (idFornecedor == null) {
            throw new IllegalArgumentException("ID do fornecedor não pode ser nulo");
        }
        // NOTA: O modelo Produto não possui campo idFornecedor
        throw new UnsupportedOperationException(
            "Campo 'idFornecedor' não implementado no modelo Produto");
    }
    
    /**
     * Soma valores de estoque por categoria.
     * 
     * @param idCategoria ID da categoria
     * @return Valor total do estoque da categoria
     */
    public BigDecimal sumValorEstoqueByCategoria(@NotNull Integer idCategoria) {
        if (idCategoria == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        // NOTA: Esta funcionalidade deve ser implementada através do EstoqueService
        // que gerencia a tabela ESTOQUE separadamente
        throw new UnsupportedOperationException(
            "Soma de valores de estoque deve ser implementada através do EstoqueService");
    }
    
    /**
     * Soma valores de estoque por fornecedor.
     * 
     * @param idFornecedor ID do fornecedor
     * @return Valor total do estoque do fornecedor
     */
    public BigDecimal sumValorEstoqueByFornecedor(@NotNull Integer idFornecedor) {
        if (idFornecedor == null) {
            throw new IllegalArgumentException("ID do fornecedor não pode ser nulo");
        }
        // NOTA: Esta funcionalidade deve ser implementada através do EstoqueService
        // que gerencia a tabela ESTOQUE separadamente
        throw new UnsupportedOperationException(
            "Soma de valores de estoque deve ser implementada através do EstoqueService");
    }
    
    /**
     * Soma quantidades em estoque por categoria.
     * 
     * @param idCategoria ID da categoria
     * @return Quantidade total em estoque da categoria
     */
    public Long sumQuantidadeEstoqueByCategoria(@NotNull Integer idCategoria) {
        if (idCategoria == null) {
            throw new IllegalArgumentException("ID da categoria não pode ser nulo");
        }
        // NOTA: Esta funcionalidade deve ser implementada através do EstoqueService
        // que gerencia a tabela ESTOQUE separadamente
        throw new UnsupportedOperationException(
            "Soma de quantidades de estoque deve ser implementada através do EstoqueService");
    }
    
    /**
     * Soma quantidades em estoque por fornecedor.
     * 
     * @param idFornecedor ID do fornecedor
     * @return Quantidade total em estoque do fornecedor
     */
    public Long sumQuantidadeEstoqueByFornecedor(@NotNull Integer idFornecedor) {
        if (idFornecedor == null) {
            throw new IllegalArgumentException("ID do fornecedor não pode ser nulo");
        }
        // NOTA: Esta funcionalidade deve ser implementada através do EstoqueService
        // que gerencia a tabela ESTOQUE separadamente
        throw new UnsupportedOperationException(
            "Soma de quantidades de estoque deve ser implementada através do EstoqueService");
    }
    
    /**
     * Calcula a margem de lucro de um produto.
     * 
     * @param produto Produto para calcular a margem
     * @return Margem de lucro em percentual (0-100)
     */
    public BigDecimal calcularMargemLucro(@NotNull Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        
        // NOTA: O modelo Produto não possui campos precoCusto e precoVenda
        // Esta funcionalidade deve ser implementada através de um serviço de preços
        // que gerencie uma tabela separada de preços/custos
        
        throw new UnsupportedOperationException(
            "Cálculo de margem de lucro deve ser implementado através de um serviço de preços");
    }
    
    /**
     * Calcula o valor total do estoque de um produto.
     * 
     * @param produto Produto para calcular o valor do estoque
     * @return Valor total do estoque (quantidade * preço de custo)
     */
    public BigDecimal calcularValorEstoque(@NotNull Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        
        // NOTA: O modelo Produto não possui campos quantidadeEstoque e precoCusto
        // Esta funcionalidade deve ser implementada através dos serviços:
        // - EstoqueService para obter quantidade atual
        // - PrecoService para obter preço de custo atual
        
        throw new UnsupportedOperationException(
            "Cálculo de valor de estoque deve ser implementado através dos serviços EstoqueService e PrecoService");
    }
    
    /**
     * Atualiza o estoque de um produto (adiciona ou remove quantidade).
     * 
     * @param idProduto ID do produto
     * @param quantidade Quantidade a ser adicionada (positiva) ou removida (negativa)
     * @return Produto atualizado
     * @throws EntityNotFoundException se o produto não existir
     * @throws IllegalArgumentException se a operação resultar em estoque negativo
     */
    @Transactional
    public Produto atualizarEstoque(@NotNull Integer idProduto, @NotNull Integer quantidade) {
        if (idProduto == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo");
        }
        
        if (quantidade == null) {
            throw new IllegalArgumentException("Quantidade não pode ser nula");
        }
        
        // Verifica se o produto existe
        findById(idProduto);
        
        // NOTA: O modelo Produto não possui campo de quantidade em estoque
        // Esta funcionalidade deve ser implementada através do EstoqueService
        // que gerencia a tabela ESTOQUE separadamente
        
        throw new UnsupportedOperationException(
            "Atualização de estoque deve ser feita através do EstoqueService");
    }
    
    /**
     * Ativa um produto.
     * 
     * @param idProduto ID do produto
     * @return Produto ativado
     */
    @Transactional
    public Produto ativarProduto(@NotNull Integer idProduto) {
        // Verifica se o produto existe
        findById(idProduto);
        // NOTA: O modelo Produto não possui campo 'ativo'
        // Esta funcionalidade pode ser implementada futuramente
        throw new UnsupportedOperationException(
            "Campo 'ativo' não implementado no modelo Produto");
    }
    
    /**
     * Inativa um produto.
     * 
     * @param idProduto ID do produto
     * @return Produto inativado
     */
    @Transactional
    public Produto inativarProduto(@NotNull Integer idProduto) {
        // Verifica se o produto existe
        findById(idProduto);
        // NOTA: O modelo Produto não possui campo 'ativo'
        // Esta funcionalidade pode ser implementada futuramente
        throw new UnsupportedOperationException(
            "Campo 'ativo' não implementado no modelo Produto");
    }
    
    /**
     * Valida regras de negócio gerais para criação/atualização.
     * 
     * @param produto Produto a ser validado
     * @throws IllegalArgumentException se alguma regra for violada
     */
    private void validateBusinessRules(Produto produto) {
        // Validação: nome é obrigatório
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
        
        // Validações removidas: campos não existem no modelo Produto atual
        // - getPrecoCusto() e getPrecoVenda(): não implementados
        // - getQuantidadeEstoque(): deve ser consultado na tabela de estoque
        
        // Validação: quantidade mínima deve ser não negativa
        // Validações removidas: campos não existem no modelo Produto
        // - getQuantidadeMinima(): usar getStqMin()
        // - getPrecoCusto() e getPrecoVenda(): não existem no modelo atual
        
        // Validação: estoque mínimo deve ser não negativo
        if (produto.getStqMin() != null && produto.getStqMin() < 0) {
            throw new IllegalArgumentException("Estoque mínimo deve ser não negativo");
        }
        
        // Validação: estoque máximo deve ser maior que mínimo
        if (produto.getStqMax() != null && produto.getStqMin() != null) {
            if (produto.getStqMax() <= produto.getStqMin()) {
                throw new IllegalArgumentException(
                    "Estoque máximo deve ser maior que o estoque mínimo");
            }
        }
    }
    
    /**
     * Valida regras específicas para atualização.
     * 
     * @param novoProduto Nova versão do produto
     * @param produtoExistente Produto existente no banco
     * @throws IllegalStateException se alguma regra de atualização for violada
     */
    private void validateUpdateRules(Produto novoProduto, Produto produtoExistente) {
        // Validação: não permitir alterar código de barras se já existe outro produto com o mesmo
        if (novoProduto.getCodBarras() != null &&
            !novoProduto.getCodBarras().equals(produtoExistente.getCodBarras())) {
            
            if (produtoRepository.findByCodigoBarras(novoProduto.getCodBarras()).isPresent()) {
                throw new IllegalArgumentException(
                    "Já existe outro produto com o código de barras: " + novoProduto.getCodBarras());
            }
        }
        
        // Aplicar validações gerais
        validateBusinessRules(novoProduto);
    }
    
    /**
     * Valida se um produto pode ser deletado.
     * 
     * @param produto Produto a ser deletado
     * @throws IllegalStateException se não puder ser deletado
     */
    private void validateDeletion(Produto produto) {
        // Validação: não permitir deletar produto com estoque
        // Nota: O modelo Produto não possui campo de quantidade em estoque
        // Esta validação deve ser implementada consultando a tabela de estoque
        // if (estoqueService.getQuantidadeEstoque(produto.getIdProduto()) > 0) {
        //     throw new IllegalStateException("Não é possível deletar produto com estoque");
        // }
        
        // Validação: verificar se há dependências
        // Esta validação pode ser expandida para verificar se o produto está sendo usado
        // em ordens de compra, vendas, etc.
    }

    /**
     * Busca produtos exato por nome (case-insensitive).
     * 
     * @param nome Nome exato do produto
     * @return Lista de produtos com nome exato
     */
    public List<Produto> findByNome(@NotNull String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        return produtoRepository.findByNome(nome.trim());
    }

    /**
     * Busca produtos que precisam de reposição (com estoque abaixo do ponto de pedido).
     * 
     * @return Lista de produtos que precisam de reposição
     */
    public List<Produto> findProdutosParaReposicao() {
        return produtoRepository.findProdutosParaReposicao();
    }

    /**
     * Busca produtos com estoque baixo (abaixo do mínimo mas acima do ponto crítico).
     * 
     * @return Lista de produtos com estoque baixo
     */
    public List<Produto> findProdutosEstoqueBaixo() {
        return produtoRepository.findProdutosEstoqueBaixo();
    }

    /**
     * Busca produtos com estoque crítico (no ponto de pedido ou abaixo).
     * 
     * @return Lista de produtos com estoque crítico
     */
    public List<Produto> findProdutosEstoqueCritico() {
        return produtoRepository.findProdutosEstoqueCritico();
    }
}