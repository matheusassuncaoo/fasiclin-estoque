package com.br.fasipe.estoque.ordemcompra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.br.fasipe.estoque.ordemcompra.models.Lote;

import jakarta.persistence.QueryHint;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados da entidade Lote.
 * 
 * <p>Este repository fornece métodos otimizados para consultas na tabela LOTE
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
public interface LoteRepository extends JpaRepository<Lote, Integer> {
    
    /**
     * Busca um lote pelo ID.
     * 
     * <p><strong>Coluna do banco:</strong> IDLOTE (INT, PRIMARY KEY, AUTO_INCREMENT)</p>
     * 
     * @param id ID do lote
     * @return Optional contendo o lote encontrado ou vazio se não existir
     */
    @Query("SELECT l FROM Lote l WHERE l.idLote = :id")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    Optional<Lote> findByIdLote(@Param("id") Integer id);

    /**
     * Busca lotes por ID da ordem de compra.
     * 
     * <p><strong>Coluna do banco:</strong> ID_ORDCOMP (INT, NOT NULL, FK -> ORDEMCOMPRA(IDORDCOMP))</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Lista de lotes da ordem de compra especificada
     */
    @Query("SELECT l FROM Lote l WHERE l.idOrdComp = :idOrdComp ORDER BY l.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

    /**
     * Busca lotes por data de vencimento.
     * 
     * <p><strong>Coluna do banco:</strong> DATAVENC (DATE, NOT NULL)</p>
     * 
     * @param dataVencimento Data de vencimento
     * @return Lista de lotes com a data de vencimento especificada
     */
    @Query("SELECT l FROM Lote l WHERE l.dataVenc = :dataVencimento ORDER BY l.idLote ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findByDataVencimento(@Param("dataVencimento") LocalDate dataVencimento);

    /**
     * Busca lotes com vencimento em uma faixa de datas.
     * 
     * <p>Útil para relatórios de lotes próximos ao vencimento.</p>
     * 
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Lista de lotes com vencimento na faixa especificada
     */
    @Query("SELECT l FROM Lote l WHERE l.dataVenc BETWEEN :dataInicio AND :dataFim ORDER BY l.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findByDataVencimentoBetween(@Param("dataInicio") LocalDate dataInicio, 
                                          @Param("dataFim") LocalDate dataFim);

    /**
     * Busca lotes vencidos (data de vencimento anterior à data atual).
     * 
     * <p>Consulta otimizada para alertas de lotes vencidos.</p>
     * 
     * @return Lista de lotes vencidos
     */
    @Query("SELECT l FROM Lote l WHERE l.dataVenc < CURRENT_DATE ORDER BY l.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findLotesVencidos();

    /**
     * Busca lotes próximos ao vencimento (próximos 30 dias).
     * 
     * <p>Consulta otimizada para alertas de vencimento próximo.</p>
     * 
     * @return Lista de lotes próximos ao vencimento
     */
    @Query("SELECT l FROM Lote l WHERE l.dataVenc BETWEEN CURRENT_DATE AND :dataLimite ORDER BY l.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findLotesProximosVencimento(@Param("dataLimite") LocalDate dataLimite);

    /**
     * Busca lotes por quantidade específica.
     * 
     * <p><strong>Coluna do banco:</strong> QNTD (INT, NOT NULL)</p>
     * 
     * @param quantidade Quantidade no lote
     * @return Lista de lotes com a quantidade especificada
     */
    @Query("SELECT l FROM Lote l WHERE l.qntd = :quantidade ORDER BY l.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findByQuantidade(@Param("quantidade") Integer quantidade);

    /**
     * Busca lotes com quantidade dentro de uma faixa.
     * 
     * <p>Útil para relatórios de lotes por tamanho.</p>
     * 
     * @param quantidadeMinima Quantidade mínima
     * @param quantidadeMaxima Quantidade máxima
     * @return Lista de lotes dentro da faixa especificada
     */
    @Query("SELECT l FROM Lote l WHERE l.qntd BETWEEN :quantidadeMinima AND :quantidadeMaxima ORDER BY l.qntd ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findByQuantidadeBetween(@Param("quantidadeMinima") Integer quantidadeMinima, 
                                      @Param("quantidadeMaxima") Integer quantidadeMaxima);

    /**
     * Busca lotes com quantidade baixa (menor que o valor especificado).
     * 
     * <p>Consulta otimizada para alertas de lotes com pouca quantidade.</p>
     * 
     * @param quantidadeMinima Quantidade mínima considerada como baixa
     * @return Lista de lotes com quantidade baixa
     */
    @Query("SELECT l FROM Lote l WHERE l.qntd < :quantidadeMinima ORDER BY l.qntd ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findLotesQuantidadeBaixa(@Param("quantidadeMinima") Integer quantidadeMinima);

    /**
     * Busca lotes zerados (quantidade = 0).
     * 
     * <p>Consulta otimizada para lotes sem quantidade disponível.</p>
     * 
     * @return Lista de lotes zerados
     */
    @Query("SELECT l FROM Lote l WHERE l.qntd = 0 ORDER BY l.dataVenc ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<Lote> findLotesZerados();

    /**
     * Conta o total de lotes por ordem de compra.
     * 
     * <p>Consulta agregada otimizada para dashboards e relatórios.</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Quantidade de lotes da ordem de compra
     */
    @Query("SELECT COUNT(l) FROM Lote l WHERE l.idOrdComp = :idOrdComp")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long countByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

    /**
     * Soma a quantidade total de produtos em todos os lotes de uma ordem de compra.
     * 
     * <p>Consulta agregada para obter a quantidade total recebida de uma ordem de compra.</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Quantidade total de produtos nos lotes da ordem de compra
     */
    @Query("SELECT COALESCE(SUM(l.qntd), 0) FROM Lote l WHERE l.idOrdComp = :idOrdComp")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long sumQuantidadeByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

    /**
     * Conta lotes vencidos por ordem de compra.
     * 
     * <p>Consulta agregada para relatórios de qualidade de recebimento.</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Quantidade de lotes vencidos da ordem de compra
     */
    @Query("SELECT COUNT(l) FROM Lote l WHERE l.idOrdComp = :idOrdComp AND l.dataVenc < CURRENT_DATE")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long countLotesVencidosByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);



}