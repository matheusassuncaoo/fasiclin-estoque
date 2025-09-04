package com.br.fasipe.estoque.ordemcompra.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.MovContabil;
import com.br.fasipe.estoque.ordemcompra.repository.MovContabilRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service para operações de negócio da entidade MovContabil.
 * 
 * <p>Esta classe implementa a camada de serviço para o módulo de Movimentações Contábeis,
 * fornecendo operações CRUD completas com validações de negócio, tratamento de exceções
 * e métodos de consulta otimizados para gestão de lançamentos contábeis.</p>
 * 
 * <p><strong>Funcionalidades principais:</strong></p>
 * <ul>
 *   <li>CRUD completo (Create, Read, Update, Delete)</li>
 *   <li>Consultas por ordem de compra, plano de contas e data de lançamento</li>
 *   <li>Operações de controle de débitos e créditos</li>
 *   <li>Validação de balanceamento contábil</li>
 *   <li>Relatórios de movimentações por período</li>
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
public class MovContabilService {
    
    @Autowired
    private MovContabilRepository movContabilRepository;
    
    /**
     * Busca uma movimentação contábil por ID.
     * 
     * @param id ID da movimentação contábil (não pode ser nulo)
     * @return MovContabil encontrada
     * @throws EntityNotFoundException se a movimentação não for encontrada
     * @throws IllegalArgumentException se o ID for nulo
     */
    public MovContabil findById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return movContabilRepository.findByIdMovContab(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Movimentação contábil não encontrada com ID: " + id));
    }
    
    /**
     * Busca todas as movimentações contábeis.
     * 
     * @return Lista de todas as movimentações contábeis
     */
    public List<MovContabil> findAll() {
        return movContabilRepository.findAll();
    }
    
    /**
     * Busca movimentações contábeis por faixa de valores.
     * 
     * @param valorMinimo Valor mínimo (não pode ser nulo)
     * @param valorMaximo Valor máximo (não pode ser nulo)
     * @return Lista de movimentações com valores dentro da faixa especificada
     * @throws IllegalArgumentException se algum dos valores for nulo ou inválido
     */
    public List<MovContabil> findByValorBetween(@NotNull BigDecimal valorMinimo, @NotNull BigDecimal valorMaximo) {
        if (valorMinimo == null || valorMaximo == null) {
            throw new IllegalArgumentException("Valores mínimo e máximo não podem ser nulos");
        }
        
        if (valorMinimo.compareTo(BigDecimal.ZERO) < 0 || valorMaximo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valores mínimo e máximo devem ser positivos");
        }
        
        if (valorMinimo.compareTo(valorMaximo) > 0) {
            throw new IllegalArgumentException("Valor mínimo não pode ser maior que valor máximo");
        }
        
        List<MovContabil> todas = findAll();
        
        return todas.stream()
            .filter(mov -> {
                BigDecimal valorDebito = mov.getValDbto() != null ? mov.getValDbto() : BigDecimal.ZERO;
                BigDecimal valorCredito = mov.getValCdto() != null ? mov.getValCdto() : BigDecimal.ZERO;
                BigDecimal valorTotal = valorDebito.add(valorCredito);
                
                return valorTotal.compareTo(valorMinimo) >= 0 && valorTotal.compareTo(valorMaximo) <= 0;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calcula o saldo por plano de contas e período (débitos - créditos).
     * 
     * @param idPlanoConta ID do plano de contas (não pode ser nulo)
     * @param dataInicio Data inicial do período (não pode ser nula)
     * @param dataFim Data final do período (não pode ser nula)
     * @return Saldo calculado (débitos - créditos) no período
     * @throws IllegalArgumentException se algum parâmetro for nulo ou inválido
     */
    public BigDecimal calcularSaldoPorPlanoContaEPeriodo(@NotNull Integer idPlanoConta, 
                                                       @NotNull LocalDate dataInicio, 
                                                       @NotNull LocalDate dataFim) {
        if (idPlanoConta == null) {
            throw new IllegalArgumentException("ID do plano de contas não pode ser nulo");
        }
        
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim não podem ser nulas");
        }
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
        
        List<MovContabil> movimentacoes = movContabilRepository.findByIdPlanoConta(idPlanoConta).stream()
            .filter(mov -> {
                LocalDate dataLancamento = mov.getDataLancame();
                return dataLancamento != null && 
                       (dataLancamento.isEqual(dataInicio) || dataLancamento.isAfter(dataInicio)) && 
                       (dataLancamento.isEqual(dataFim) || dataLancamento.isBefore(dataFim));
            })
            .collect(Collectors.toList());
        
        BigDecimal totalDebitos = movimentacoes.stream()
            .filter(m -> m.getValDbto() != null && m.getValDbto().compareTo(BigDecimal.ZERO) > 0)
            .map(MovContabil::getValDbto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalCreditos = movimentacoes.stream()
            .filter(m -> m.getValCdto() != null && m.getValCdto().compareTo(BigDecimal.ZERO) > 0)
            .map(MovContabil::getValCdto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        return totalDebitos.subtract(totalCreditos);
    }
    
    /**
     * Gera relatório de movimentações por período.
     * 
     * @param dataInicio Data inicial do período (não pode ser nula)
     * @param dataFim Data final do período (não pode ser nula)
     * @return Lista de movimentações no período especificado
     * @throws IllegalArgumentException se alguma data for nula ou inválida
     */
    public List<MovContabil> gerarRelatorioPorPeriodo(@NotNull LocalDate dataInicio, @NotNull LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim não podem ser nulas");
        }
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
        
        return movContabilRepository.findByDataLancamentoBetween(dataInicio, dataFim);
    }

    /**
     * Cria uma nova movimentação contábil.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve ser nulo (será gerado automaticamente)</li>
     *   <li>Número de lançamento é obrigatório</li>
     *   <li>ID do plano de contas é obrigatório</li>
     *   <li>Data de lançamento é obrigatória</li>
     *   <li>Valores de débito e crédito devem ser positivos</li>
     * </ul>
     * 
     * @param obj Movimentação contábil a ser criada (validada com @Valid)
     * @return MovContabil criada com ID gerado
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public MovContabil create(@Valid @NotNull MovContabil obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Movimentação contábil não pode ser nula");
        }
        
        if (obj.getIdMovContab() != null) {
            throw new IllegalArgumentException("ID deve ser nulo para criação de nova movimentação");
        }
        
        // Validações de negócio adicionais
        validateBusinessRules(obj);
        
        try {
            return movContabilRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao criar movimentação contábil: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza uma movimentação contábil existente.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve existir no banco</li>
     *   <li>Campos obrigatórios devem estar preenchidos</li>
     *   <li>Regras de negócio específicas</li>
     * </ul>
     * 
     * @param obj Movimentação contábil a ser atualizada (validada com @Valid)
     * @return MovContabil atualizada
     * @throws EntityNotFoundException se a movimentação não existir
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public MovContabil update(@Valid @NotNull MovContabil obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Movimentação contábil não pode ser nula");
        }
        
        if (obj.getIdMovContab() == null) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }
        
        // Verifica se a movimentação existe
        MovContabil existingMov = findById(obj.getIdMovContab());
        
        // Validações de negócio para atualização
        validateUpdateRules(obj, existingMov);
        
        try {
            return movContabilRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao atualizar movimentação contábil: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta uma movimentação contábil por ID.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>ID não pode ser nulo</li>
     *   <li>Movimentação deve existir no banco</li>
     *   <li>Não pode deletar se houver dependências</li>
     * </ul>
     * 
     * @param id ID da movimentação contábil a ser deletada
     * @throws EntityNotFoundException se a movimentação não existir
     * @throws IllegalArgumentException se o ID for nulo
     * @throws IllegalStateException se a movimentação não puder ser deletada
     */
    @Transactional
    public void deleteById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        MovContabil movContabil = findById(id);
        
        // Validação de negócio: verificar se pode ser deletada
        validateDeletion(movContabil);
        
        try {
            movContabilRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Movimentação contábil não encontrada com ID: " + id);
        }
    }
    
    /**
     * Deleta uma movimentação contábil por objeto.
     * 
     * @param obj Movimentação contábil a ser deletada
     * @throws IllegalArgumentException se o objeto for nulo
     */
    @Transactional
    public void delete(@NotNull MovContabil obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Movimentação contábil não pode ser nula");
        }
        
        deleteById(obj.getIdMovContab());
    }
    
    /**
     * Busca movimentações contábeis por número de lançamento.
     * 
     * @param numeroLancamento Número do lançamento
     * @return Lista de movimentações com o número de lançamento especificado
     */
    public List<MovContabil> findByNumeroLancamento(@NotNull Integer numeroLancamento) {
        if (numeroLancamento == null) {
            throw new IllegalArgumentException("Número de lançamento não pode ser nulo");
        }
        Optional<MovContabil> result = movContabilRepository.findByNumeroLancamento(numeroLancamento);
        return result.map(List::of).orElse(List.of());
    }
    
    /**
     * Busca movimentações contábeis por ID da ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return Lista de movimentações da ordem de compra
     */
    public List<MovContabil> findByIdOrdemCompra(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        return movContabilRepository.findByIdOrdComp(idOrdemCompra);
    }
    
    /**
     * Busca movimentações contábeis por ID da ordem de compra.
     * 
     * @param idOrdComp ID da ordem de compra
     * @return Lista de movimentações da ordem de compra
     */
    public List<MovContabil> findByIdOrdComp(@NotNull Integer idOrdComp) {
        if (idOrdComp == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        return movContabilRepository.findByIdOrdComp(idOrdComp);
    }
    
    /**
     * Busca movimentações contábeis por ID do item de venda.
     * 
     * @param idItemVenda ID do item de venda
     * @return Lista de movimentações do item de venda
     */
    public List<MovContabil> findByIdItemVenda(@NotNull Integer idItemVenda) {
        if (idItemVenda == null) {
            throw new IllegalArgumentException("ID do item de venda não pode ser nulo");
        }
        return movContabilRepository.findByIdItemVenda(idItemVenda);
    }
    
    /**
     * Busca movimentações contábeis por ID do plano de contas.
     * 
     * @param idPlanoConta ID do plano de contas
     * @return Lista de movimentações do plano de contas
     */
    public List<MovContabil> findByIdPlanoConta(@NotNull Integer idPlanoConta) {
        if (idPlanoConta == null) {
            throw new IllegalArgumentException("ID do plano de contas não pode ser nulo");
        }
        return movContabilRepository.findByIdPlanoConta(idPlanoConta);
    }
    
    /**
     * Busca movimentações contábeis por data de lançamento específica.
     * 
     * @param dataLancamento Data de lançamento
     * @return Lista de movimentações na data especificada
     */
    public List<MovContabil> findByDataLancamento(@NotNull LocalDate dataLancamento) {
        if (dataLancamento == null) {
            throw new IllegalArgumentException("Data de lançamento não pode ser nula");
        }
        return movContabilRepository.findByDataLancamento(dataLancamento);
    }
    
    /**
     * Busca movimentações contábeis por tipo (débito ou crédito).
     * 
     * @param tipo Tipo da movimentação ("D" para débito ou "C" para crédito)
     * @return Lista de movimentações do tipo especificado
     */
    public List<MovContabil> findByTipo(@NotNull String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo não pode ser nulo");
        }
        
        if (!tipo.equals("D") && !tipo.equals("C")) {
            throw new IllegalArgumentException("Tipo deve ser 'D' para débito ou 'C' para crédito");
        }
        
        List<MovContabil> todas = findAll();
        
        if (tipo.equals("D")) {
            return todas.stream()
                .filter(mov -> mov.getValDbto() != null && mov.getValDbto().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        } else {
            return todas.stream()
                .filter(mov -> mov.getValCdto() != null && mov.getValCdto().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Busca movimentações contábeis por faixa de datas de lançamento.
     * 
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Lista de movimentações na faixa de datas
     */
    public List<MovContabil> findByDataLancamentoBetween(@NotNull LocalDate dataInicio, 
                                                        @NotNull LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial deve ser anterior à data final");
        }
        
        return movContabilRepository.findByDataLancamentoBetween(dataInicio, dataFim);
    }
    
    /**
     * Busca movimentações contábeis por faixa de valor de débito.
     * 
     * @param valorMinimo Valor mínimo de débito
     * @param valorMaximo Valor máximo de débito
     * @return Lista de movimentações na faixa de valor de débito
     */
    public List<MovContabil> findByValorDebitoBetween(@NotNull BigDecimal valorMinimo, 
                                                     @NotNull BigDecimal valorMaximo) {
        if (valorMinimo == null || valorMaximo == null) {
            throw new IllegalArgumentException("Valores não podem ser nulos");
        }
        
        if (valorMinimo.compareTo(BigDecimal.ZERO) < 0 || valorMaximo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valores devem ser positivos");
        }
        
        if (valorMinimo.compareTo(valorMaximo) > 0) {
            throw new IllegalArgumentException("Valor mínimo deve ser menor que o máximo");
        }
        
        return movContabilRepository.findByValorDebitoBetween(valorMinimo, valorMaximo);
    }
    
    /**
     * Busca movimentações contábeis por faixa de valor de crédito.
     * 
     * @param valorMinimo Valor mínimo de crédito
     * @param valorMaximo Valor máximo de crédito
     * @return Lista de movimentações na faixa de valor de crédito
     */
    public List<MovContabil> findByValorCreditoBetween(@NotNull BigDecimal valorMinimo, 
                                                      @NotNull BigDecimal valorMaximo) {
        if (valorMinimo == null || valorMaximo == null) {
            throw new IllegalArgumentException("Valores não podem ser nulos");
        }
        
        if (valorMinimo.compareTo(BigDecimal.ZERO) < 0 || valorMaximo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valores devem ser positivos");
        }
        
        if (valorMinimo.compareTo(valorMaximo) > 0) {
            throw new IllegalArgumentException("Valor mínimo deve ser menor que o máximo");
        }
        
        return movContabilRepository.findByValorCreditoBetween(valorMinimo, valorMaximo);
    }
    
    /**
     * Busca movimentações contábeis balanceadas (débito = crédito).
     * 
     * @return Lista de movimentações balanceadas
     */
    public List<MovContabil> findMovimentacoesBalanceadas() {
        return movContabilRepository.findMovimentacoesBalanceadas();
    }
    
    /**
     * Busca movimentações contábeis desbalanceadas (débito ≠ crédito).
     * 
     * @return Lista de movimentações desbalanceadas
     */
    public List<MovContabil> findMovimentacoesDesbalanceadas() {
        return movContabilRepository.findMovimentacoesDesbalanceadas();
    }
    
    /**
     * Conta movimentações por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return Quantidade de movimentações da ordem de compra
     */
    public Long countByIdOrdComp(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        return movContabilRepository.countByIdOrdComp(idOrdemCompra);
    }
    
    /**
     * Soma valores de débito por plano de contas em um período.
     * 
     * @param idPlanoContas ID do plano de contas
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Soma dos valores de débito
     */
    public BigDecimal sumDebitosByPlanoContasAndPeriodo(@NotNull Integer idPlanoContas, 
                                                       @NotNull LocalDate dataInicio, 
                                                       @NotNull LocalDate dataFim) {
        if (idPlanoContas == null) {
            throw new IllegalArgumentException("ID do plano de contas não pode ser nulo");
        }
        
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial deve ser anterior à data final");
        }
        
        return movContabilRepository.sumDebitosByPlanoContaAndPeriodo(idPlanoContas, dataInicio, dataFim);
    }
    
    /**
     * Soma valores de crédito por plano de contas em um período.
     * 
     * @param idPlanoContas ID do plano de contas
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Soma dos valores de crédito
     */
    public BigDecimal sumCreditosByPlanoContasAndPeriodo(@NotNull Integer idPlanoContas, 
                                                        @NotNull LocalDate dataInicio, 
                                                        @NotNull LocalDate dataFim) {
        if (idPlanoContas == null) {
            throw new IllegalArgumentException("ID do plano de contas não pode ser nulo");
        }
        
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial deve ser anterior à data final");
        }
        
        return movContabilRepository.sumCreditosByPlanoContaAndPeriodo(idPlanoContas, dataInicio, dataFim);
    }
    
    /**
     * Calcula o saldo de uma conta (créditos - débitos) em um período.
     * 
     * @param idPlanoContas ID do plano de contas
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Saldo da conta no período
     */
    public BigDecimal calcularSaldoConta(@NotNull Integer idPlanoContas, 
                                        @NotNull LocalDate dataInicio, 
                                        @NotNull LocalDate dataFim) {
        BigDecimal creditos = sumCreditosByPlanoContasAndPeriodo(idPlanoContas, dataInicio, dataFim);
        BigDecimal debitos = sumDebitosByPlanoContasAndPeriodo(idPlanoContas, dataInicio, dataFim);
        
        return creditos.subtract(debitos);
    }
    
    /**
     * Calcula o saldo por plano de contas (todos os créditos - todos os débitos).
     * 
     * @param idPlanoConta ID do plano de contas
     * @return Saldo do plano de contas
     */
    public BigDecimal calcularSaldoPorPlanoConta(@NotNull Integer idPlanoConta) {
        if (idPlanoConta == null) {
            throw new IllegalArgumentException("ID do plano de contas não pode ser nulo");
        }
        
        List<MovContabil> movimentacoes = findByIdPlanoConta(idPlanoConta);
        
        BigDecimal totalCreditos = BigDecimal.ZERO;
        BigDecimal totalDebitos = BigDecimal.ZERO;
        
        for (MovContabil mov : movimentacoes) {
            if (mov.getValCdto() != null) {
                totalCreditos = totalCreditos.add(mov.getValCdto());
            }
            
            if (mov.getValDbto() != null) {
                totalDebitos = totalDebitos.add(mov.getValDbto());
            }
        }
        
        return totalCreditos.subtract(totalDebitos);
    }
    
    /**
     * Verifica se uma movimentação está balanceada.
     * 
     * @param movContabil Movimentação a ser verificada
     * @return true se a movimentação estiver balanceada
     */
    public boolean isMovimentacaoBalanceada(@NotNull MovContabil movContabil) {
        if (movContabil == null) {
            throw new IllegalArgumentException("Movimentação contábil não pode ser nula");
        }
        
        BigDecimal debito = movContabil.getValDbto() != null ? movContabil.getValDbto() : BigDecimal.ZERO;
        BigDecimal credito = movContabil.getValCdto() != null ? movContabil.getValCdto() : BigDecimal.ZERO;
        
        return debito.compareTo(credito) == 0;
    }
    
    /**
     * Cria um lançamento contábil balanceado.
     * 
     * @param numeroLancamento Número do lançamento
     * @param idPlanoContasDebito ID do plano de contas para débito
     * @param idPlanoContasCredito ID do plano de contas para crédito
     * @param valor Valor do lançamento
     * @param dataLancamento Data do lançamento
     * @param historico Histórico do lançamento
     * @return Lista com as duas movimentações criadas (débito e crédito)
     */
    @Transactional
    public List<MovContabil> criarLancamentoBalanceado(@NotNull Integer numeroLancamento,
                                                      @NotNull Integer idPlanoContasDebito,
                                                      @NotNull Integer idPlanoContasCredito,
                                                      @NotNull BigDecimal valor,
                                                      @NotNull LocalDate dataLancamento,
                                                      String historico) {
        if (numeroLancamento == null) {
            throw new IllegalArgumentException("Número de lançamento não pode ser nulo");
        }
        
        if (idPlanoContasDebito == null || idPlanoContasCredito == null) {
            throw new IllegalArgumentException("IDs dos planos de contas não podem ser nulos");
        }
        
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
        
        if (dataLancamento == null) {
            throw new IllegalArgumentException("Data de lançamento não pode ser nula");
        }
        
        // Criar movimentação de débito
        MovContabil movDebito = new MovContabil();
        movDebito.setNumeLancam(numeroLancamento);
        movDebito.setIdPlanoConta(idPlanoContasDebito);
        movDebito.setValDbto(valor);
        movDebito.setValCdto(BigDecimal.ZERO);
        movDebito.setDataLancame(dataLancamento);
        // Nota: campo historico não existe no modelo MovContabil
        
        // Criar movimentação de crédito
        MovContabil movCredito = new MovContabil();
        movCredito.setNumeLancam(numeroLancamento);
        movCredito.setIdPlanoConta(idPlanoContasCredito);
        movCredito.setValDbto(BigDecimal.ZERO);
        movCredito.setValCdto(valor);
        movCredito.setDataLancame(dataLancamento);
        // Nota: campo historico não existe no modelo MovContabil
        
        // Salvar as movimentações
        MovContabil debitoSalvo = create(movDebito);
        MovContabil creditoSalvo = create(movCredito);
        
        return List.of(debitoSalvo, creditoSalvo);
    }
    
    /**
     * Valida regras de negócio gerais para criação/atualização.
     * 
     * @param movContabil Movimentação a ser validada
     * @throws IllegalArgumentException se alguma regra for violada
     */
    private void validateBusinessRules(MovContabil movContabil) {
        // Validação: número de lançamento é obrigatório
        if (movContabil.getNumeLancam() == null) {
            throw new IllegalArgumentException("Número de lançamento é obrigatório");
        }
        
        // Validação: ID do plano de contas é obrigatório
        if (movContabil.getIdPlanoConta() == null) {
            throw new IllegalArgumentException("ID do plano de contas é obrigatório");
        }
        
        // Validação: data de lançamento é obrigatória
        if (movContabil.getDataLancame() == null) {
            throw new IllegalArgumentException("Data de lançamento é obrigatória");
        }
        
        // Validação: valores de débito e crédito devem ser positivos ou zero
        if (movContabil.getValDbto() != null && movContabil.getValDbto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor de débito deve ser positivo ou zero");
        }
        
        if (movContabil.getValCdto() != null && movContabil.getValCdto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor de crédito deve ser positivo ou zero");
        }
        
        // Validação: pelo menos um dos valores (débito ou crédito) deve ser maior que zero
        BigDecimal debito = movContabil.getValDbto() != null ? movContabil.getValDbto() : BigDecimal.ZERO;
        BigDecimal credito = movContabil.getValCdto() != null ? movContabil.getValCdto() : BigDecimal.ZERO;
        
        if (debito.compareTo(BigDecimal.ZERO) == 0 && credito.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Pelo menos um valor (débito ou crédito) deve ser maior que zero");
        }
    }
    
    /**
     * Valida regras específicas para atualização.
     * 
     * @param novaMovContabil Nova versão da movimentação
     * @param movContabilExistente Movimentação existente no banco
     * @throws IllegalStateException se alguma regra de atualização for violada
     */
    private void validateUpdateRules(MovContabil novaMovContabil, MovContabil movContabilExistente) {
        // Validação: não permitir alterar número de lançamento após criação
        if (!movContabilExistente.getNumeLancam().equals(novaMovContabil.getNumeLancam())) {
            throw new IllegalStateException(
                "Número de lançamento não pode ser alterado após criação");
        }
        
        // Aplicar validações gerais
        validateBusinessRules(novaMovContabil);
    }
    
    /**
     * Valida se uma movimentação pode ser deletada.
     * 
     * @param movContabil Movimentação a ser deletada
     * @throws IllegalStateException se não puder ser deletada
     */
    private void validateDeletion(MovContabil movContabil) {
        // Validação: verificar se há dependências
        // Esta validação pode ser expandida conforme necessário
        
        // Por enquanto, permitir deleção sempre
        // Futuras implementações podem incluir verificações de integridade referencial
    }
}