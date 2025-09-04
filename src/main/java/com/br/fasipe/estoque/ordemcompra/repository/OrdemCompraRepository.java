package com.br.fasipe.estoque.ordemcompra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra;
import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra.StatusOrdemCompra;

import jakarta.persistence.QueryHint;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados da entidade OrdemCompra.
 * 
 * <p>Este repository fornece métodos otimizados para consultas na tabela ORDEMCOMPRA
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
public interface OrdemCompraRepository extends JpaRepository<OrdemCompra, Integer> {
    
    /**
     * Busca uma ordem de compra pelo ID.
     * 
     * <p><strong>Coluna do banco:</strong> IDORDCOMP (INT, PRIMARY KEY, AUTO_INCREMENT)</p>
     * 
     * @param id ID da ordem de compra
     * @return Optional contendo a ordem de compra encontrada ou vazio se não existir
     */
    @Query("SELECT o FROM OrdemCompra o WHERE o.id = :id")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    Optional<OrdemCompra> findByIdOrdemCompra(@Param("id") Integer id);

    /**
     * Busca ordens de compra por status.
     * 
     * <p><strong>Coluna do banco:</strong> STATUSORD (ENUM: 'PEND', 'ANDA', 'CONC')</p>
     * <ul>
     *   <li><strong>PEND:</strong> Pendente</li>
     *   <li><strong>ANDA:</strong> Em andamento</li>
     *   <li><strong>CONC:</strong> Concluída</li>
     * </ul>
     * 
     * @param status Status da ordem de compra
     * @return Lista de ordens de compra com o status especificado
     */
    @Query("SELECT o FROM OrdemCompra o WHERE o.statusOrdemCompra = :status ORDER BY o.dataOrdem DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<OrdemCompra> findByStatus(@Param("status") StatusOrdemCompra status);

    /**
     * Busca ordens de compra por faixa de valor.
     * 
     * <p><strong>Coluna do banco:</strong> VALOR (DECIMAL(10,2), NOT NULL)</p>
     * 
     * @param valorMinimo Valor mínimo da ordem
     * @param valorMaximo Valor máximo da ordem
     * @return Lista de ordens de compra dentro da faixa de valor especificada
     */
    @Query("SELECT o FROM OrdemCompra o WHERE o.valor BETWEEN :valorMinimo AND :valorMaximo ORDER BY o.valor DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<OrdemCompra> findByValorBetween(@Param("valorMinimo") BigDecimal valorMinimo, 
                                        @Param("valorMaximo") BigDecimal valorMaximo);

    /**
     * Busca ordens de compra por data prevista.
     * 
     * <p><strong>Coluna do banco:</strong> DATAPREV (DATE, NOT NULL)</p>
     * 
     * @param dataPrevista Data prevista da ordem
     * @return Lista de ordens de compra com a data prevista especificada
     */
    @Query("SELECT o FROM OrdemCompra o WHERE o.dataPrev = :dataPrevista ORDER BY o.dataOrdem DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<OrdemCompra> findByDataPrevista(@Param("dataPrevista") LocalDate dataPrevista);

    /**
     * Busca ordens de compra por período de criação.
     * 
     * <p><strong>Coluna do banco:</strong> DATAORDEM (DATE, NOT NULL)</p>
     * 
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @return Lista de ordens de compra criadas no período especificado
     */
    @Query("SELECT o FROM OrdemCompra o WHERE o.dataOrdem BETWEEN :dataInicio AND :dataFim ORDER BY o.dataOrdem DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<OrdemCompra> findByPeriodoCriacao(@Param("dataInicio") LocalDate dataInicio, 
                                          @Param("dataFim") LocalDate dataFim);

    /**
     * Busca ordens de compra por data de entrega.
     * 
     * <p><strong>Coluna do banco:</strong> DATAENTRE (DATE, NOT NULL)</p>
     * 
     * @param dataEntrega Data de entrega da ordem
     * @return Lista de ordens de compra com a data de entrega especificada
     */
    @Query("SELECT o FROM OrdemCompra o WHERE o.dataEntre = :dataEntrega ORDER BY o.dataOrdem DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<OrdemCompra> findByDataEntrega(@Param("dataEntrega") LocalDate dataEntrega);

    /**
     * Busca ordens de compra com entrega em atraso.
     * 
     * <p>Consulta otimizada que retorna ordens onde a data de entrega é anterior à data atual
     * e o status não é 'CONC' (concluída).</p>
     * 
     * @param dataAtual Data atual para comparação
     * @return Lista de ordens de compra em atraso
     */
    @Query("SELECT o FROM OrdemCompra o WHERE o.dataEntre < :dataAtual AND o.statusOrdemCompra != 'CONC' ORDER BY o.dataEntre ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Não cachear consultas dinâmicas
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "2000")
    })
    List<OrdemCompra> findOrdensEmAtraso(@Param("dataAtual") LocalDate dataAtual);

    /**
     * Conta o total de ordens de compra por status.
     * 
     * <p>Consulta agregada otimizada para dashboards e relatórios.</p>
     * 
     * @param status Status da ordem de compra
     * @return Quantidade de ordens com o status especificado
     */
    @Query("SELECT COUNT(o) FROM OrdemCompra o WHERE o.statusOrdemCompra = :status")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "jakarta.persistence.cache.storeMode", value = "USE"),
        @QueryHint(name = "jakarta.persistence.cache.retrieveMode", value = "USE"), 
        @QueryHint(name = "jakarta.persistence.query.timeout", value = "1000")
    })
    Long countByStatus(@Param("status") StatusOrdemCompra status);

}
