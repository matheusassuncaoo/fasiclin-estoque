package com.br.fasipe.estoque.ordemcompra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.br.fasipe.estoque.ordemcompra.models.Produto;

import jakarta.persistence.QueryHint;


import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados da entidade Produto.
 * 
 * <p>Este repository fornece métodos otimizados para consultas na tabela PRODUTO
 * do banco de dados MySQL. Todas as consultas utilizam QueryHints para melhor performance:</p>
 * 
 * <ul>
 *   <li><strong>readOnly:</strong> Otimiza consultas somente leitura</li>
 *   <li><strong>fetchSize:</strong> Define tamanho do lote para busca (50 registros)</li>
 *   <li><strong>cacheable:</strong> Habilita cache de segundo nível do Hibernate</li>
 *   <li><strong>timeout:</strong> Define timeout de 2 segundos para consultas</li>
 * </ul>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    
    /**
     * Busca um produto pelo ID.
     * 
     * <p><strong>Coluna do banco:</strong> IDPRODUTO (INT, PRIMARY KEY, AUTO_INCREMENT)</p>
     * 
     * @param id ID do produto
     * @return Optional contendo o produto encontrado ou vazio se não existir
     */
    @Query("SELECT p FROM Produto p WHERE p.idProduto = :id")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    Optional<Produto> findByIdProduto(@Param("id") Integer id);

    /**
     * Busca produto pelo código de barras.
     * 
     * <p><strong>Coluna do banco:</strong> CODBARRAS (VARCHAR(50), UNIQUE)</p>
     * 
     * @param codigoBarras Código de barras do produto
     * @return Optional contendo o produto encontrado ou vazio se não existir
     */
    @Query("SELECT p FROM Produto p WHERE p.codBarras = :codigoBarras")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    Optional<Produto> findByCodigoBarras(@Param("codigoBarras") String codigoBarras);

    /**
     * Busca produtos pelo nome (busca parcial, case-insensitive).
     * 
     * <p><strong>Coluna do banco:</strong> NOME (VARCHAR(50), NOT NULL)</p>
     * 
     * @param nome Nome ou parte do nome do produto
     * @return Lista de produtos que contenham o nome especificado
     */
    @Query("SELECT p FROM Produto p WHERE UPPER(p.nome) LIKE UPPER(CONCAT('%', :nome, '%')) ORDER BY p.nome ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Produto> findByNomeContaining(@Param("nome") String nome);

    /**
     * Busca produtos pela descrição (busca parcial, case-insensitive).
     * 
     * <p><strong>Coluna do banco:</strong> DESCRICAO (TEXT)</p>
     * 
     * @param descricao Descrição ou parte da descrição do produto
     * @return Lista de produtos que contenham a descrição especificada
     */
    @Query("SELECT p FROM Produto p WHERE UPPER(p.descricao) LIKE UPPER(CONCAT('%', :descricao, '%')) ORDER BY p.nome ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Produto> findByDescricaoContaining(@Param("descricao") String descricao);

    // Métodos removidos: campos idCategoria e idFornecedor não existem no modelo Produto atual
    // - findByIdCategoria (campo idCategoria não existe no modelo Produto)
    // - findByIdFornecedor (campo idFornecedor não existe no modelo Produto)
    // - countByIdCategoria (campo idCategoria não existe no modelo Produto)
    // - countByIdFornecedor (campo idFornecedor não existe no modelo Produto)

    // Métodos removidos: campos precoCusto, precoVenda, qtdEstoque e ativo não existem no modelo Produto atual
    // - findByPrecoCustoBetween
    // - findByPrecoVendaBetween
    // - findByQuantidadeEstoqueBetween (campo qtdEstoque não existe no Produto, está na tabela Estoque)
    // - findProdutosComEstoqueBaixo (campo qtdEstoque não existe no Produto)
    // - findProdutosZerados (campo qtdEstoque não existe no Produto)
    // - findProdutosAtivos (campo ativo não existe no modelo Produto)
    // - findProdutosInativos (campo ativo não existe no modelo Produto)
    // - findProdutosComAltaMargem (campos precoCusto e precoVenda não existem)
    // - sumValorTotalEstoque (campos qtdEstoque e precoCusto não existem)
    // - sumQuantidadeTotalEstoque (campo qtdEstoque não existe no Produto)

    // Métodos removidos: campos qtdEstoque, precoCusto e ativo não existem no modelo Produto atual
    // - calcularValorTotalEstoque (campos qtdEstoque e precoCusto não existem)
    // - calcularQuantidadeTotalEstoque (campo qtdEstoque não existe no Produto)

}