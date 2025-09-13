package com.br.fasipe.estoque.ordemcompra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.br.fasipe.estoque.ordemcompra.models.ItemOrdemCompra;

import jakarta.persistence.QueryHint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados da entidade ItemOrdemCompra.
 * 
 * <p>Este repository fornece métodos otimizados para consultas na tabela ITEM_ORDCOMP
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
public interface ItemOrdemCompraRepository extends JpaRepository<ItemOrdemCompra, Integer> {
    
    /**
     * Busca um item de ordem de compra pelo ID.
     * 
     * <p><strong>Coluna do banco:</strong> IDITEMORD (INT, PRIMARY KEY, AUTO_INCREMENT)</p>
     * 
     * @param id ID do item da ordem de compra
     * @return Optional contendo o item encontrado ou vazio se não existir
     */
    @Query("SELECT i FROM ItemOrdemCompra i WHERE i.idItemOrd = :id")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    Optional<ItemOrdemCompra> findByIdItemOrd(@Param("id") Integer id);

    /**
     * Busca itens de ordem de compra por ID da ordem de compra.
     * 
     * <p><strong>Coluna do banco:</strong> ID_ORDCOMP (INT, NOT NULL, FK -> ORDEMCOMPRA(IDORDCOMP))</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Lista de itens da ordem de compra especificada
     */
    @Query("SELECT i FROM ItemOrdemCompra i WHERE i.idOrdComp = :idOrdComp ORDER BY i.idItemOrd ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<ItemOrdemCompra> findByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

    /**
     * Busca itens de ordem de compra por ID do produto.
     * 
     * <p><strong>Coluna do banco:</strong> ID_PRODUTO (INT, NOT NULL, FK -> PRODUTO(IDPRODUTO))</p>
     * 
     * @param idProduto ID do produto
     * @return Lista de itens com o produto especificado
     */
    @Query("SELECT i FROM ItemOrdemCompra i WHERE i.idProduto = :idProduto ORDER BY i.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<ItemOrdemCompra> findByIdProduto(@Param("idProduto") Integer idProduto);

    /**
     * Busca itens de ordem de compra por data de vencimento.
     * 
     * <p><strong>Coluna do banco:</strong> DATAVENC (DATE, NOT NULL)</p>
     * 
     * @param dataVencimento Data de vencimento
     * @return Lista de itens com a data de vencimento especificada
     */
    @Query("SELECT i FROM ItemOrdemCompra i WHERE i.dataVenc = :dataVencimento ORDER BY i.idItemOrd ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<ItemOrdemCompra> findByDataVencimento(@Param("dataVencimento") LocalDate dataVencimento);

    /**
     * Busca itens de ordem de compra com vencimento em uma faixa de datas.
     * 
     * <p>Útil para relatórios de itens próximos ao vencimento.</p>
     * 
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Lista de itens com vencimento na faixa especificada
     */
    @Query("SELECT i FROM ItemOrdemCompra i WHERE i.dataVenc BETWEEN :dataInicio AND :dataFim ORDER BY i.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<ItemOrdemCompra> findByDataVencimentoBetween(@Param("dataInicio") LocalDate dataInicio, 
                                                      @Param("dataFim") LocalDate dataFim);

    /**
     * Busca itens de ordem de compra vencidos (data de vencimento anterior à data atual).
     * 
     * <p>Consulta otimizada para alertas de itens vencidos.</p>
     * 
     * @return Lista de itens vencidos
     */
    @Query("SELECT i FROM ItemOrdemCompra i WHERE i.dataVenc < CURRENT_DATE ORDER BY i.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<ItemOrdemCompra> findItensVencidos();

    /**
     * Busca itens de ordem de compra próximos ao vencimento (próximos 30 dias).
     * 
     * <p>Consulta otimizada para alertas de vencimento próximo.</p>
     * 
     * @return Lista de itens próximos ao vencimento
     */
    @Query("SELECT i FROM ItemOrdemCompra i WHERE i.dataVenc BETWEEN CURRENT_DATE AND :dataLimite ORDER BY i.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<ItemOrdemCompra> findItensProximosVencimento(@Param("dataLimite") LocalDate dataLimite);

    /**
     * Busca itens de ordem de compra por faixa de valor unitário.
     * 
     * <p><strong>Coluna do banco:</strong> VALOR (DECIMAL(10,2), NOT NULL)</p>
     * 
     * @param valorMinimo Valor mínimo
     * @param valorMaximo Valor máximo
     * @return Lista de itens na faixa de valor especificada
     */
    @Query("SELECT i FROM ItemOrdemCompra i WHERE i.valor BETWEEN :valorMinimo AND :valorMaximo ORDER BY i.valor ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<ItemOrdemCompra> findByValorBetween(@Param("valorMinimo") BigDecimal valorMinimo, 
                                            @Param("valorMaximo") BigDecimal valorMaximo);

    /**
     * Conta o total de itens por ordem de compra.
     * 
     * <p>Consulta agregada otimizada para dashboards e relatórios.</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Quantidade de itens na ordem de compra
     */
    @Query("SELECT COUNT(i) FROM ItemOrdemCompra i WHERE i.idOrdComp = :idOrdComp")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long countByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

    /**
     * Soma o valor total dos itens por ordem de compra.
     * 
     * <p>Consulta agregada para obter o valor total de uma ordem de compra.
     * Agora utiliza o campo vlrTotal diretamente do banco de dados.</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Valor total dos itens da ordem de compra
     */
    @Query("SELECT COALESCE(SUM(i.vlrTotal), 0) FROM ItemOrdemCompra i WHERE i.idOrdComp = :idOrdComp")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    BigDecimal sumValorTotalByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

    /**
     * Soma a quantidade total de itens por produto.
     * 
     * <p>Consulta agregada para obter a quantidade total comprada de um produto.</p>
     * 
     * @param idProduto ID do produto
     * @return Quantidade total comprada do produto
     */
    @Query("SELECT COALESCE(SUM(i.qntd), 0) FROM ItemOrdemCompra i WHERE i.idProduto = :idProduto")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long sumQuantidadeByIdProduto(@Param("idProduto") Integer idProduto);

    /**
     * Soma a quantidade total de itens por ordem de compra.
     * 
     * <p>Consulta agregada para obter a quantidade total de itens em uma ordem de compra.</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Quantidade total de itens na ordem de compra
     */
    @Query("SELECT COALESCE(SUM(i.qntd), 0) FROM ItemOrdemCompra i WHERE i.idOrdComp = :idOrdComp")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long sumQuantidadeByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

}