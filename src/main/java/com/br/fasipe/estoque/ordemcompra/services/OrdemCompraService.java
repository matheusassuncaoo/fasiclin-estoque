package com.br.fasipe.estoque.ordemcompra.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra;
import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra.StatusOrdemCompra;
import com.br.fasipe.estoque.ordemcompra.repository.OrdemCompraRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service para operações de negócio da entidade OrdemCompra.
 * 
 * <p>Esta classe implementa a camada de serviço para o módulo de Ordem de Compra,
 * fornecendo operações CRUD completas com validações de negócio, tratamento de exceções
 * e métodos de consulta otimizados.</p>
 * 
 * <p><strong>Funcionalidades principais:</strong></p>
 * <ul>
 *   <li>CRUD completo (Create, Read, Update, Delete)</li>
 *   <li>Consultas por status, valor, datas</li>
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
public class OrdemCompraService {
    
    @Autowired
    private OrdemCompraRepository ordemCompraRepository;
    
    /**
     * Busca uma ordem de compra por ID.
     * 
     * @param id ID da ordem de compra (não pode ser nulo)
     * @return OrdemCompra encontrada
     * @throws EntityNotFoundException se a ordem não for encontrada
     * @throws IllegalArgumentException se o ID for nulo
     */
    public OrdemCompra findById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return ordemCompraRepository.findByIdOrdemCompra(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Ordem de compra não encontrada com ID: " + id));
    }
    
    /**
     * Busca todas as ordens de compra.
     * 
     * @return Lista de todas as ordens de compra
     */
    public List<OrdemCompra> findAll() {
        return ordemCompraRepository.findAll();
    }

    /**
     * Cria uma nova ordem de compra.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve ser nulo (será gerado automaticamente)</li>
     *   <li>Campos obrigatórios devem estar preenchidos</li>
     *   <li>Valores devem ser positivos</li>
     *   <li>Datas devem ser válidas</li>
     * </ul>
     * 
     * @param obj Ordem de compra a ser criada (validada com @Valid)
     * @return OrdemCompra criada com ID gerado
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public OrdemCompra create(@Valid @NotNull OrdemCompra obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Ordem de compra não pode ser nula");
        }
        
        if (obj.getId() != null) {
            throw new IllegalArgumentException("ID deve ser nulo para criação de nova ordem");
        }
        
        // Validações de negócio adicionais
        validateBusinessRules(obj);
        
        try {
            return ordemCompraRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao criar ordem de compra: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza uma ordem de compra existente.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve existir no banco</li>
     *   <li>Campos obrigatórios devem estar preenchidos</li>
     *   <li>Regras de negócio específicas</li>
     * </ul>
     * 
     * @param obj Ordem de compra a ser atualizada (validada com @Valid)
     * @return OrdemCompra atualizada
     * @throws EntityNotFoundException se a ordem não existir
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public OrdemCompra update(@Valid @NotNull OrdemCompra obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Ordem de compra não pode ser nula");
        }
        
        if (obj.getId() == null) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }
        
        // Verifica se a ordem existe
        OrdemCompra existingOrdem = findById(obj.getId());
        
        // Validações de negócio para atualização
        validateUpdateRules(obj, existingOrdem);
        
        try {
            return ordemCompraRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao atualizar ordem de compra: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta uma ordem de compra por ID.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>ID não pode ser nulo</li>
     *   <li>Ordem deve existir no banco</li>
     *   <li>Ordem não pode estar em status que impeça exclusão</li>
     * </ul>
     * 
     * @param id ID da ordem de compra a ser deletada
     * @throws EntityNotFoundException se a ordem não existir
     * @throws IllegalArgumentException se o ID for nulo
     * @throws IllegalStateException se a ordem não puder ser deletada
     */
    @Transactional
    public void deleteById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        OrdemCompra ordem = findById(id);
        
        // Validação de negócio: não permitir deletar ordens concluídas
        if (ordem.getStatusOrdemCompra() == StatusOrdemCompra.CONC) {
            throw new IllegalStateException(
                "Não é possível deletar ordem de compra concluída (ID: " + id + ")");
        }
        
        try {
            ordemCompraRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Ordem de compra não encontrada com ID: " + id);
        }
    }
    
    /**
     * Deleta uma ordem de compra por objeto.
     * 
     * @param obj Ordem de compra a ser deletada
     * @throws IllegalArgumentException se o objeto for nulo
     */
    @Transactional
    public void delete(@NotNull OrdemCompra obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Ordem de compra não pode ser nula");
        }
        
        deleteById(obj.getId());
    }
    
    /**
     * Busca ordens de compra por status.
     * 
     * @param status Status da ordem (PEND, ANDA, CONC)
     * @return Lista de ordens com o status especificado
     */
    public List<OrdemCompra> findByStatus(@NotNull StatusOrdemCompra status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        return ordemCompraRepository.findByStatus(status);
    }
    
    /**
     * Busca ordens de compra por faixa de valor.
     * 
     * @param valorMinimo Valor mínimo (deve ser positivo)
     * @param valorMaximo Valor máximo (deve ser maior que o mínimo)
     * @return Lista de ordens na faixa de valor especificada
     */
    public List<OrdemCompra> findByValorBetween(@NotNull BigDecimal valorMinimo, 
                                               @NotNull BigDecimal valorMaximo) {
        if (valorMinimo == null || valorMaximo == null) {
            throw new IllegalArgumentException("Valores não podem ser nulos");
        }
        
        if (valorMinimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor mínimo deve ser positivo");
        }
        
        if (valorMinimo.compareTo(valorMaximo) > 0) {
            throw new IllegalArgumentException("Valor mínimo deve ser menor que o máximo");
        }
        
        return ordemCompraRepository.findByValorBetween(valorMinimo, valorMaximo);
    }
    
    /**
     * Busca ordens de compra por data prevista.
     * 
     * @param dataPrevista Data prevista da ordem
     * @return Lista de ordens com a data prevista especificada
     */
    public List<OrdemCompra> findByDataPrevista(@NotNull LocalDate dataPrevista) {
        if (dataPrevista == null) {
            throw new IllegalArgumentException("Data prevista não pode ser nula");
        }
        return ordemCompraRepository.findByDataPrevista(dataPrevista);
    }
    
    /**
     * Busca ordens de compra por período de criação.
     * 
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @return Lista de ordens criadas no período
     */
    public List<OrdemCompra> findByPeriodoCriacao(@NotNull LocalDate dataInicio, 
                                                 @NotNull LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial deve ser anterior à data final");
        }
        
        return ordemCompraRepository.findByPeriodoCriacao(dataInicio, dataFim);
    }
    
    /**
     * Busca ordens de compra em atraso.
     * 
     * @return Lista de ordens com entrega em atraso
     */
    public List<OrdemCompra> findOrdensEmAtraso() {
        return ordemCompraRepository.findOrdensEmAtraso(LocalDate.now());
    }
    
    /**
     * Conta ordens de compra por status.
     * 
     * @param status Status para contagem
     * @return Quantidade de ordens com o status especificado
     */
    public Long countByStatus(@NotNull StatusOrdemCompra status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        return ordemCompraRepository.countByStatus(status);
    }
    
    /**
     * Valida regras de negócio gerais para criação/atualização.
     * 
     * @param ordem Ordem a ser validada
     * @throws IllegalArgumentException se alguma regra for violada
     */
    private void validateBusinessRules(OrdemCompra ordem) {
        // Validação: data de ordem não pode ser futura
        if (ordem.getDataOrdem() != null && ordem.getDataOrdem().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da ordem não pode ser futura");
        }
        
        // Validação: data prevista deve ser posterior à data da ordem
        if (ordem.getDataOrdem() != null && ordem.getDataPrev() != null) {
            if (ordem.getDataPrev().isBefore(ordem.getDataOrdem())) {
                throw new IllegalArgumentException(
                    "Data prevista deve ser posterior à data da ordem");
            }
        }
        
        // Validação: valor deve ser positivo
        if (ordem.getValor() != null && ordem.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
    }
    
    /**
     * Valida regras específicas para atualização.
     * 
     * @param novaOrdem Nova versão da ordem
     * @param ordemExistente Ordem existente no banco
     * @throws IllegalStateException se alguma regra de atualização for violada
     */
    private void validateUpdateRules(OrdemCompra novaOrdem, OrdemCompra ordemExistente) {
        // Validação: não permitir alterar status de CONC para outros
        if (ordemExistente.getStatusOrdemCompra() == StatusOrdemCompra.CONC && 
            novaOrdem.getStatusOrdemCompra() != StatusOrdemCompra.CONC) {
            throw new IllegalStateException(
                "Não é possível alterar status de ordem concluída");
        }
        
        // Validação: não permitir alterar data da ordem após criação
        if (ordemExistente.getDataOrdem() != null && 
            !ordemExistente.getDataOrdem().equals(novaOrdem.getDataOrdem())) {
            throw new IllegalStateException(
                "Data da ordem não pode ser alterada após criação");
        }
        
        // Aplicar validações gerais
        validateBusinessRules(novaOrdem);
    }
}
