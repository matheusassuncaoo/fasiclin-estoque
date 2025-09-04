package com.br.fasipe.estoque.ordemcompra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.br.fasipe.estoque.ordemcompra.models.Estoque;

import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados da entidade Estoque.
 * 
 * <p>Este repository fornece métodos otimizados para consultas na tabela ESTOQUE
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
public interface EstoqueRepository extends JpaRepository<Estoque, Integer> {
    
    /**
     * Busca um registro de estoque pelo ID.
     * 
     * <p><strong>Coluna do banco:</strong> IDESTOQUE (INT, PRIMARY KEY, AUTO_INCREMENT)</p>
     * 
     * @param id ID do registro de estoque
     * @return Optional contendo o estoque encontrado ou vazio se não existir
     */
    @Query("SELECT e FROM Estoque e WHERE e.id = :id")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    Optional<Estoque> findByIdEstoque(@Param("id") Integer id);

    /**
     * Busca registros de estoque por ID do produto.
     * 
     * <p><strong>Coluna do banco:</strong> ID_PRODUTO (INT, NOT NULL, FK -> PRODUTO(IDPRODUTO))</p>
     * 
     * @param idProduto ID do produto
     * @return Lista de registros de estoque do produto especificado
     */
    @Query("SELECT e FROM Estoque e WHERE e.idProduto = :idProduto ORDER BY e.qtdEstoque DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Estoque> findByIdProduto(@Param("idProduto") Integer idProduto);

    /**
     * Busca registros de estoque por ID do lote.
     * 
     * <p><strong>Coluna do banco:</strong> ID_LOTE (INT, FK -> LOTE(IDLOTE))</p>
     * 
     * @param idLote ID do lote
     * @return Lista de registros de estoque do lote especificado
     */
    @Query("SELECT e FROM Estoque e WHERE e.idLote = :idLote ORDER BY e.qtdEstoque DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Estoque> findByIdLote(@Param("idLote") Integer idLote);

    /**
     * Busca registros de estoque com quantidade específica.
     * 
     * <p><strong>Coluna do banco:</strong> QTDESTOQUE (INT, NOT NULL)</p>
     * 
     * @param quantidade Quantidade em estoque
     * @return Lista de registros com a quantidade especificada
     */
    @Query("SELECT e FROM Estoque e WHERE e.qtdEstoque = :quantidade ORDER BY e.id DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Estoque> findByQuantidade(@Param("quantidade") Integer quantidade);

    /**
     * Busca registros de estoque com quantidade dentro de uma faixa.
     * 
     * <p>Útil para relatórios de estoque baixo, médio ou alto.</p>
     * 
     * @param quantidadeMinima Quantidade mínima
     * @param quantidadeMaxima Quantidade máxima
     * @return Lista de registros dentro da faixa especificada
     */
    @Query("SELECT e FROM Estoque e WHERE e.qtdEstoque BETWEEN :quantidadeMinima AND :quantidadeMaxima ORDER BY e.qtdEstoque ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Estoque> findByQuantidadeBetween(@Param("quantidadeMinima") Integer quantidadeMinima, 
                                         @Param("quantidadeMaxima") Integer quantidadeMaxima);

    /**
     * Busca registros de estoque com quantidade baixa (menor que o valor especificado).
     * 
     * <p>Consulta otimizada para alertas de estoque baixo.</p>
     * 
     * @param quantidadeMinima Quantidade mínima considerada como estoque baixo
     * @return Lista de registros com estoque baixo
     */
    @Query("SELECT e FROM Estoque e WHERE e.qtdEstoque < :quantidadeMinima ORDER BY e.qtdEstoque ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Estoque> findEstoqueBaixo(@Param("quantidadeMinima") Integer quantidadeMinima);

    /**
     * Busca registros de estoque zerado (quantidade = 0).
     * 
     * <p>Consulta otimizada para produtos sem estoque.</p>
     * 
     * @return Lista de registros com estoque zerado
     */
    @Query("SELECT e FROM Estoque e WHERE e.qtdEstoque = 0 ORDER BY e.id DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Estoque> findEstoqueZerado();

    /**
     * Conta o total de registros de estoque por produto.
     * 
     * <p>Consulta agregada otimizada para dashboards e relatórios.</p>
     * 
     * @param idProduto ID do produto
     * @return Quantidade de registros de estoque do produto
     */
    @Query("SELECT COUNT(e) FROM Estoque e WHERE e.idProduto = :idProduto")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long countByIdProduto(@Param("idProduto") Integer idProduto);

    /**
     * Soma a quantidade total em estoque por produto.
     * 
     * <p>Consulta agregada para obter o estoque total de um produto considerando todos os lotes.</p>
     * 
     * @param idProduto ID do produto
     * @return Quantidade total em estoque do produto
     */
    @Query("SELECT COALESCE(SUM(e.qtdEstoque), 0) FROM Estoque e WHERE e.idProduto = :idProduto")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long sumQuantidadeByIdProduto(@Param("idProduto") Integer idProduto);

}
