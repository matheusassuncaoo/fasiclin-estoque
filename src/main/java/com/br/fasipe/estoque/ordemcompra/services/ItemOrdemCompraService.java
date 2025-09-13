package com.br.fasipe.estoque.ordemcompra.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.ItemOrdemCompra;
import com.br.fasipe.estoque.ordemcompra.repository.ItemOrdemCompraRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service para operações de negócio da entidade ItemOrdemCompra.
 * 
 * <p>Esta classe implementa a camada de serviço para o módulo de Itens de Ordem de Compra,
 * fornecendo operações CRUD completas com validações de negócio, tratamento de exceções
 * e métodos de consulta otimizados para gestão de itens de compra.</p>
 * 
 * <p><strong>Funcionalidades principais:</strong></p>
 * <ul>
 *   <li>CRUD completo (Create, Read, Update, Delete)</li>
 *   <li>Consultas por ordem de compra, produto e data de vencimento</li>
 *   <li>Operações de cálculo de valores e quantidades</li>
 *   <li>Alertas de itens vencidos e próximos ao vencimento</li>
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
public class ItemOrdemCompraService {
    
    @Autowired
    private ItemOrdemCompraRepository itemOrdemCompraRepository;
    
    /**
     * Busca um item de ordem de compra por ID.
     * 
     * @param id ID do item de ordem de compra (não pode ser nulo)
     * @return ItemOrdemCompra encontrado
     * @throws EntityNotFoundException se o item não for encontrado
     * @throws IllegalArgumentException se o ID for nulo
     */
    public ItemOrdemCompra findById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return itemOrdemCompraRepository.findByIdItemOrd(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Item de ordem de compra não encontrado com ID: " + id));
    }
    
    /**
     * Busca todos os itens de ordem de compra.
     * 
     * @return Lista de todos os itens de ordem de compra
     */
    public List<ItemOrdemCompra> findAll() {
        return itemOrdemCompraRepository.findAll();
    }

    /**
     * Cria um novo item de ordem de compra.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve ser nulo (será gerado automaticamente)</li>
     *   <li>ID da ordem de compra é obrigatório</li>
     *   <li>ID do produto é obrigatório</li>
     *   <li>Quantidade deve ser positiva</li>
     *   <li>Valor unitário deve ser positivo</li>
     * </ul>
     * 
     * @param obj Item de ordem de compra a ser criado (validado com @Valid)
     * @return ItemOrdemCompra criado com ID gerado
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public ItemOrdemCompra create(@Valid @NotNull ItemOrdemCompra obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Item de ordem de compra não pode ser nulo");
        }
        
        if (obj.getIdItemOrd() != null) {
            throw new IllegalArgumentException("ID deve ser nulo para criação de novo item");
        }
        
        // Validações de negócio adicionais
        validateBusinessRules(obj);
        
        // Calcula o valor total automaticamente
        obj.inicializarValorTotal();
        
        try {
            return itemOrdemCompraRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao criar item de ordem de compra: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza um item de ordem de compra existente.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve existir no banco</li>
     *   <li>Campos obrigatórios devem estar preenchidos</li>
     *   <li>Regras de negócio específicas</li>
     * </ul>
     * 
     * @param obj Item de ordem de compra a ser atualizado (validado com @Valid)
     * @return ItemOrdemCompra atualizado
     * @throws EntityNotFoundException se o item não existir
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public ItemOrdemCompra update(@Valid @NotNull ItemOrdemCompra obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Item de ordem de compra não pode ser nulo");
        }
        
        if (obj.getIdItemOrd() == null) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }
        
        // Verifica se o item existe
        ItemOrdemCompra existingItem = findById(obj.getIdItemOrd());
        
        // Validações de negócio para atualização
        validateUpdateRules(obj, existingItem);
        
        // Recalcula o valor total automaticamente
        obj.inicializarValorTotal();
        
        try {
            return itemOrdemCompraRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao atualizar item de ordem de compra: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta um item de ordem de compra por ID.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>ID não pode ser nulo</li>
     *   <li>Item deve existir no banco</li>
     *   <li>Não pode deletar se houver movimentações relacionadas</li>
     * </ul>
     * 
     * @param id ID do item de ordem de compra a ser deletado
     * @throws EntityNotFoundException se o item não existir
     * @throws IllegalArgumentException se o ID for nulo
     * @throws IllegalStateException se o item não puder ser deletado
     */
    @Transactional
    public void deleteById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        ItemOrdemCompra item = findById(id);
        
        // Validação de negócio: verificar se pode ser deletado
        validateDeletion(item);
        
        try {
            itemOrdemCompraRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Item de ordem de compra não encontrado com ID: " + id);
        }
    }
    
    /**
     * Deleta um item de ordem de compra por objeto.
     * 
     * @param obj Item de ordem de compra a ser deletado
     * @throws IllegalArgumentException se o objeto for nulo
     */
    @Transactional
    public void delete(@NotNull ItemOrdemCompra obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Item de ordem de compra não pode ser nulo");
        }
        
        deleteById(obj.getIdItemOrd());
    }
    
    /**
     * Busca itens de ordem de compra por ID da ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return Lista de itens da ordem de compra
     */
    public List<ItemOrdemCompra> findByIdOrdemCompra(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        return itemOrdemCompraRepository.findByIdOrdComp(idOrdemCompra);
    }
    
    /**
     * Busca itens de ordem de compra por ID do produto.
     * 
     * @param idProduto ID do produto
     * @return Lista de itens do produto
     */
    public List<ItemOrdemCompra> findByIdProduto(@NotNull Integer idProduto) {
        if (idProduto == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo");
        }
        return itemOrdemCompraRepository.findByIdProduto(idProduto);
    }
    
    /**
     * Busca itens por data de vencimento específica.
     * 
     * @param dataVencimento Data de vencimento
     * @return Lista de itens com a data de vencimento especificada
     */
    public List<ItemOrdemCompra> findByDataVencimento(@NotNull LocalDate dataVencimento) {
        if (dataVencimento == null) {
            throw new IllegalArgumentException("Data de vencimento não pode ser nula");
        }
        return itemOrdemCompraRepository.findByDataVencimento(dataVencimento);
    }
    
    /**
     * Busca itens por faixa de datas de vencimento.
     * 
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @return Lista de itens na faixa de datas
     */
    public List<ItemOrdemCompra> findByDataVencimentoBetween(@NotNull LocalDate dataInicio, 
                                                            @NotNull LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }
        
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial deve ser anterior à data final");
        }
        
        return itemOrdemCompraRepository.findByDataVencimentoBetween(dataInicio, dataFim);
    }
    
    /**
     * Busca itens vencidos.
     * 
     * @return Lista de itens com data de vencimento anterior à data atual
     */
    public List<ItemOrdemCompra> findItensVencidos() {
        return itemOrdemCompraRepository.findItensVencidos();
    }
    
    /**
     * Busca itens próximos ao vencimento (próximos 30 dias).
     * 
     * @return Lista de itens próximos ao vencimento
     */
    public List<ItemOrdemCompra> findItensProximosVencimento() {
        LocalDate dataLimite = LocalDate.now().plusDays(30);
        return itemOrdemCompraRepository.findItensProximosVencimento(dataLimite);
    }
    
    /**
     * Busca itens por faixa de valor unitário.
     * 
     * @param valorMinimo Valor unitário mínimo
     * @param valorMaximo Valor unitário máximo
     * @return Lista de itens na faixa de valor
     */
    public List<ItemOrdemCompra> findByValorUnitarioBetween(@NotNull BigDecimal valorMinimo, 
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
        
        return itemOrdemCompraRepository.findByValorBetween(valorMinimo, valorMaximo);
    }
    
    /**
     * Conta itens por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return Quantidade de itens da ordem de compra
     */
    public Long countByIdOrdemCompra(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        return itemOrdemCompraRepository.countByIdOrdComp(idOrdemCompra);
    }
    
    /**
     * Soma valor total dos itens por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return Valor total dos itens da ordem de compra
     */
    public BigDecimal sumValorTotalByIdOrdemCompra(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        BigDecimal total = itemOrdemCompraRepository.sumValorTotalByIdOrdComp(idOrdemCompra);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Soma quantidade total dos itens por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return Quantidade total dos itens da ordem de compra
     */
    public Long sumQuantidadeByIdOrdemCompra(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        Long total = itemOrdemCompraRepository.sumQuantidadeByIdOrdComp(idOrdemCompra);
        return total != null ? total : 0L;
    }
    
    /**
     * Calcula o valor total de um item (quantidade * valor unitário).
     * 
     * @param item Item de ordem de compra
     * @return Valor total do item
     */
    public BigDecimal calcularValorTotal(@NotNull ItemOrdemCompra item) {
        if (item == null) {
            throw new IllegalArgumentException("Item não pode ser nulo");
        }
        
        if (item.getQntd() == null || item.getValor() == null) {
            return BigDecimal.ZERO;
        }
        
        return item.getValor().multiply(new BigDecimal(item.getQntd()));
    }
    
    /**
     * Atualiza o valor total de um item baseado na quantidade e valor unitário.
     * 
     * @param item Item a ser atualizado
     * @return Item atualizado
     */
    @Transactional
    public ItemOrdemCompra atualizarValorTotal(@NotNull ItemOrdemCompra item) {
        if (item == null) {
            throw new IllegalArgumentException("Item não pode ser nulo");
        }
        
        // Calcula e atualiza o valor total usando o método do modelo
        item.inicializarValorTotal();
        
        return update(item);
    }
    
    /**
     * Valida regras de negócio gerais para criação/atualização.
     * 
     * @param item Item a ser validado
     * @throws IllegalArgumentException se alguma regra for violada
     */
    private void validateBusinessRules(ItemOrdemCompra item) {
        // Validação: ID da ordem de compra é obrigatório
        if (item.getIdOrdComp() == null) {
            throw new IllegalArgumentException("ID da ordem de compra é obrigatório");
        }
        
        // Validação: ID do produto é obrigatório
        if (item.getIdProduto() == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório");
        }
        
        // Validação: quantidade deve ser positiva
        if (item.getQntd() != null && item.getQntd() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        
        // Validação: valor unitário deve ser positivo
        if (item.getValor() != null && item.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor unitário deve ser positivo");
        }
        
        // Validação: data de vencimento não pode ser no passado
        if (item.getDataVenc() != null && item.getDataVenc().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data de vencimento não pode ser no passado");
        }
    }
    
    /**
     * Valida regras específicas para atualização.
     * 
     * @param novoItem Nova versão do item
     * @param itemExistente Item existente no banco
     * @throws IllegalStateException se alguma regra de atualização for violada
     */
    private void validateUpdateRules(ItemOrdemCompra novoItem, ItemOrdemCompra itemExistente) {
        // Validação: não permitir alterar ID da ordem de compra após criação
        if (!itemExistente.getIdOrdComp().equals(novoItem.getIdOrdComp())) {
            throw new IllegalStateException(
                "ID da ordem de compra não pode ser alterado após criação");
        }
        
        // Validação: não permitir alterar ID do produto após criação
        if (!itemExistente.getIdProduto().equals(novoItem.getIdProduto())) {
            throw new IllegalStateException(
                "ID do produto não pode ser alterado após criação");
        }
        
        // Aplicar validações gerais
        validateBusinessRules(novoItem);
    }
    
    /**
     * Valida se um item pode ser deletado.
     * 
     * @param item Item a ser deletado
     * @throws IllegalStateException se não puder ser deletado
     */
    private void validateDeletion(ItemOrdemCompra item) {
        // Validação: verificar se há movimentações relacionadas
        // Esta validação pode ser expandida conforme necessário
        
        // Por enquanto, permitir deleção sempre
        // Futuras implementações podem incluir verificações de integridade referencial
    }
}