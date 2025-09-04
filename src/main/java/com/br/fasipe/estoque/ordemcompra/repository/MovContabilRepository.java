package com.br.fasipe.estoque.ordemcompra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.br.fasipe.estoque.ordemcompra.models.MovContabil;

import jakarta.persistence.QueryHint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados da entidade MovContabil.
 * 
 * <p>Este repository fornece métodos otimizados para consultas na tabela MOVCONTABIL
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
public interface MovContabilRepository extends JpaRepository<MovContabil, Integer> {
    
    /**
     * Busca uma movimentação contábil pelo ID.
     * 
     * <p><strong>Coluna do banco:</strong> IDMOVCONTAB (INT, PRIMARY KEY, AUTO_INCREMENT)</p>
     * 
     * @param id ID da movimentação contábil
     * @return Optional contendo a movimentação encontrada ou vazio se não existir
     */
    @Query("SELECT m FROM MovContabil m WHERE m.idMovContab = :id")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    Optional<MovContabil> findByIdMovContab(@Param("id") Integer id);

    /**
     * Busca movimentação contábil pelo número do lançamento.
     * 
     * <p><strong>Coluna do banco:</strong> NUMELANCAM (INT, NOT NULL, UNIQUE)</p>
     * 
     * @param numeroLancamento Número único do lançamento
     * @return Optional contendo a movimentação encontrada ou vazio se não existir
     */
    @Query("SELECT m FROM MovContabil m WHERE m.numeLancam = :numeroLancamento")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    Optional<MovContabil> findByNumeroLancamento(@Param("numeroLancamento") Integer numeroLancamento);

    /**
     * Busca movimentações contábeis por ID da ordem de compra.
     * 
     * <p><strong>Coluna do banco:</strong> ID_ORDCOMP (INT, FK -> ORDEMCOMPRA(IDORDCOMP))</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Lista de movimentações da ordem de compra especificada
     */
    @Query("SELECT m FROM MovContabil m WHERE m.idOrdComp = :idOrdComp ORDER BY m.dataLancame DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

    /**
     * Busca movimentações contábeis por ID do item de venda.
     * 
     * <p><strong>Coluna do banco:</strong> ID_ITEMVENDA (INT, FK -> ITEMVENDA(IDITEMVENDA))</p>
     * 
     * @param idItemVenda ID do item de venda
     * @return Lista de movimentações do item de venda especificado
     */
    @Query("SELECT m FROM MovContabil m WHERE m.idItemVenda = :idItemVenda ORDER BY m.dataLancame DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findByIdItemVenda(@Param("idItemVenda") Integer idItemVenda);

    /**
     * Busca movimentações contábeis por ID do plano de contas.
     * 
     * <p><strong>Coluna do banco:</strong> ID_PLANOCONTA (INT, NOT NULL, FK -> PLANOCONTA(IDPLANOCONTA))</p>
     * 
     * @param idPlanoConta ID do plano de contas
     * @return Lista de movimentações do plano de contas especificado
     */
    @Query("SELECT m FROM MovContabil m WHERE m.idPlanoConta = :idPlanoConta ORDER BY m.dataLancame DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findByIdPlanoConta(@Param("idPlanoConta") Integer idPlanoConta);

    /**
     * Busca movimentações contábeis por data de lançamento.
     * 
     * <p><strong>Coluna do banco:</strong> DATALANCAME (DATE)</p>
     * 
     * @param dataLancamento Data do lançamento
     * @return Lista de movimentações na data especificada
     */
    @Query("SELECT m FROM MovContabil m WHERE m.dataLancame = :dataLancamento ORDER BY m.numeLancam ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findByDataLancamento(@Param("dataLancamento") LocalDate dataLancamento);

    /**
     * Busca movimentações contábeis em uma faixa de datas.
     * 
     * <p>Útil para relatórios contábeis por período.</p>
     * 
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Lista de movimentações na faixa de datas especificada
     */
    @Query("SELECT m FROM MovContabil m WHERE m.dataLancame BETWEEN :dataInicio AND :dataFim ORDER BY m.dataLancame ASC, m.numeLancam ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findByDataLancamentoBetween(@Param("dataInicio") LocalDate dataInicio, 
                                                 @Param("dataFim") LocalDate dataFim);

    /**
     * Busca movimentações contábeis por faixa de valor de débito.
     * 
     * <p><strong>Coluna do banco:</strong> VALDBTO (DECIMAL(10,2), NOT NULL)</p>
     * 
     * @param valorMinimo Valor mínimo de débito
     * @param valorMaximo Valor máximo de débito
     * @return Lista de movimentações na faixa de débito especificada
     */
    @Query("SELECT m FROM MovContabil m WHERE m.valDbto BETWEEN :valorMinimo AND :valorMaximo ORDER BY m.valDbto DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findByValorDebitoBetween(@Param("valorMinimo") BigDecimal valorMinimo, 
                                              @Param("valorMaximo") BigDecimal valorMaximo);

    /**
     * Busca movimentações contábeis por faixa de valor de crédito.
     * 
     * <p><strong>Coluna do banco:</strong> VALCDTO (DECIMAL(10,2), NOT NULL)</p>
     * 
     * @param valorMinimo Valor mínimo de crédito
     * @param valorMaximo Valor máximo de crédito
     * @return Lista de movimentações na faixa de crédito especificada
     */
    @Query("SELECT m FROM MovContabil m WHERE m.valCdto BETWEEN :valorMinimo AND :valorMaximo ORDER BY m.valCdto DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findByValorCreditoBetween(@Param("valorMinimo") BigDecimal valorMinimo, 
                                               @Param("valorMaximo") BigDecimal valorMaximo);

    /**
     * Busca movimentações contábeis balanceadas (débito = crédito).
     * 
     * <p>Consulta otimizada para auditoria contábil.</p>
     * 
     * @return Lista de movimentações balanceadas
     */
    @Query("SELECT m FROM MovContabil m WHERE m.valDbto = m.valCdto ORDER BY m.dataLancame DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findMovimentacoesBalanceadas();

    /**
     * Busca movimentações contábeis desbalanceadas (débito ≠ crédito).
     * 
     * <p>Consulta otimizada para auditoria contábil.</p>
     * 
     * @return Lista de movimentações desbalanceadas
     */
    @Query("SELECT m FROM MovContabil m WHERE m.valDbto <> m.valCdto ORDER BY m.dataLancame DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<MovContabil> findMovimentacoesDesbalanceadas();

    /**
     * Conta o total de movimentações por ordem de compra.
     * 
     * <p>Consulta agregada otimizada para dashboards e relatórios.</p>
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Quantidade de movimentações da ordem de compra
     */
    @Query("SELECT COUNT(m) FROM MovContabil m WHERE m.idOrdComp = :idOrdComp")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long countByIdOrdComp(@Param("idOrdComp") Integer idOrdComp);

    /**
     * Soma o valor total de débitos por plano de contas em um período.
     * 
     * <p>Consulta agregada para relatórios contábeis.</p>
     * 
     * @param idPlanoConta ID do plano de contas
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Valor total de débitos do plano de contas no período
     */
    @Query("SELECT COALESCE(SUM(m.valDbto), 0) FROM MovContabil m WHERE m.idPlanoConta = :idPlanoConta AND m.dataLancame BETWEEN :dataInicio AND :dataFim")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    BigDecimal sumDebitosByPlanoContaAndPeriodo(@Param("idPlanoConta") Integer idPlanoConta,
                                               @Param("dataInicio") LocalDate dataInicio,
                                               @Param("dataFim") LocalDate dataFim);

    /**
     * Soma o valor total de créditos por plano de contas em um período.
     * 
     * <p>Consulta agregada para relatórios contábeis.</p>
     * 
     * @param idPlanoConta ID do plano de contas
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Valor total de créditos do plano de contas no período
     */
    @Query("SELECT COALESCE(SUM(m.valCdto), 0) FROM MovContabil m WHERE m.idPlanoConta = :idPlanoConta AND m.dataLancame BETWEEN :dataInicio AND :dataFim")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    BigDecimal sumCreditosByPlanoContaAndPeriodo(@Param("idPlanoConta") Integer idPlanoConta,
                                                @Param("dataInicio") LocalDate dataInicio,
                                                @Param("dataFim") LocalDate dataFim);

    /**
     * Calcula o saldo (débito - crédito) por plano de contas em um período.
     * 
     * <p>Consulta agregada para balanços contábeis.</p>
     * 
     * @param idPlanoConta ID do plano de contas
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Saldo do plano de contas no período
     */
    @Query("SELECT COALESCE(SUM(m.valDbto - m.valCdto), 0) FROM MovContabil m WHERE m.idPlanoConta = :idPlanoConta AND m.dataLancame BETWEEN :dataInicio AND :dataFim")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    BigDecimal calcularSaldoByPlanoContaAndPeriodo(@Param("idPlanoConta") Integer idPlanoConta,
                                                  @Param("dataInicio") LocalDate dataInicio,
                                                  @Param("dataFim") LocalDate dataFim);

}