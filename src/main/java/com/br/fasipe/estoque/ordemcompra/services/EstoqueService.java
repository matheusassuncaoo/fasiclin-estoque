package com.br.fasipe.estoque.ordemcompra.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.Estoque;
import com.br.fasipe.estoque.ordemcompra.repository.EstoqueRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service para operações de negócio da entidade Estoque.
 * 
 * <p>Esta classe implementa a camada de serviço para o módulo de Estoque,
 * fornecendo operações CRUD completas com validações de negócio, tratamento de exceções
 * e métodos de consulta otimizados para gestão de inventário.</p>
 * 
 * <p><strong>Funcionalidades principais:</strong></p>
 * <ul>
 *   <li>CRUD completo (Create, Read, Update, Delete)</li>
 *   <li>Consultas por produto, lote e quantidade</li>
 *   <li>Operações de movimentação de estoque</li>
 *   <li>Alertas de estoque baixo e zerado</li>
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
public class EstoqueService {
    
    @Autowired
    private EstoqueRepository estoqueRepository;
    
    /**
     * Busca um registro de estoque por ID.
     * 
     * @param id ID do registro de estoque (não pode ser nulo)
     * @return Estoque encontrado
     * @throws EntityNotFoundException se o registro não for encontrado
     * @throws IllegalArgumentException se o ID for nulo
     */
    public Estoque findById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return estoqueRepository.findByIdEstoque(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Registro de estoque não encontrado com ID: " + id));
    }
    
    /**
     * Busca todos os registros de estoque.
     * 
     * @return Lista de todos os registros de estoque
     */
    public List<Estoque> findAll() {
        return estoqueRepository.findAll();
    }

    /**
     * Cria um novo registro de estoque.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve ser nulo (será gerado automaticamente)</li>
     *   <li>ID do produto é obrigatório</li>
     *   <li>Quantidade deve ser positiva ou zero</li>
     *   <li>Não pode haver duplicação de produto+lote</li>
     * </ul>
     * 
     * @param obj Registro de estoque a ser criado (validado com @Valid)
     * @return Estoque criado com ID gerado
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public Estoque create(@Valid @NotNull Estoque obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Registro de estoque não pode ser nulo");
        }
        
        if (obj.getId() != null) {
            throw new IllegalArgumentException("ID deve ser nulo para criação de novo registro");
        }
        
        // Validações de negócio adicionais
        validateBusinessRules(obj);
        
        // Verificar se já existe estoque para o mesmo produto+lote
        validateUniqueProductLot(obj);
        
        try {
            return estoqueRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao criar registro de estoque: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza um registro de estoque existente.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve existir no banco</li>
     *   <li>Campos obrigatórios devem estar preenchidos</li>
     *   <li>Regras de negócio específicas</li>
     * </ul>
     * 
     * @param obj Registro de estoque a ser atualizado (validado com @Valid)
     * @return Estoque atualizado
     * @throws EntityNotFoundException se o registro não existir
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public Estoque update(@Valid @NotNull Estoque obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Registro de estoque não pode ser nulo");
        }
        
        if (obj.getId() == null) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }
        
        // Verifica se o registro existe
        Estoque existingEstoque = findById(obj.getId());
        
        // Validações de negócio para atualização
        validateUpdateRules(obj, existingEstoque);
        
        try {
            return estoqueRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao atualizar registro de estoque: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta um registro de estoque por ID.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>ID não pode ser nulo</li>
     *   <li>Registro deve existir no banco</li>
     *   <li>Não pode deletar se houver movimentações pendentes</li>
     * </ul>
     * 
     * @param id ID do registro de estoque a ser deletado
     * @throws EntityNotFoundException se o registro não existir
     * @throws IllegalArgumentException se o ID for nulo
     * @throws IllegalStateException se o registro não puder ser deletado
     */
    @Transactional
    public void deleteById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        Estoque estoque = findById(id);
        
        // Validação de negócio: verificar se pode ser deletado
        validateDeletion(estoque);
        
        try {
            estoqueRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Registro de estoque não encontrado com ID: " + id);
        }
    }
    
    /**
     * Deleta um registro de estoque por objeto.
     * 
     * @param obj Registro de estoque a ser deletado
     * @throws IllegalArgumentException se o objeto for nulo
     */
    @Transactional
    public void delete(@NotNull Estoque obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Registro de estoque não pode ser nulo");
        }
        
        deleteById(obj.getId());
    }
    
    /**
     * Busca registros de estoque por ID do produto.
     * 
     * @param idProduto ID do produto
     * @return Lista de registros de estoque do produto
     */
    public List<Estoque> findByIdProduto(@NotNull Integer idProduto) {
        if (idProduto == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo");
        }
        return estoqueRepository.findByIdProduto(idProduto);
    }
    
    /**
     * Busca registros de estoque por ID do lote.
     * 
     * @param idLote ID do lote
     * @return Lista de registros de estoque do lote
     */
    public List<Estoque> findByIdLote(@NotNull Integer idLote) {
        if (idLote == null) {
            throw new IllegalArgumentException("ID do lote não pode ser nulo");
        }
        return estoqueRepository.findByIdLote(idLote);
    }
    
    /**
     * Busca registros de estoque por quantidade específica.
     * 
     * @param quantidade Quantidade em estoque
     * @return Lista de registros com a quantidade especificada
     */
    public List<Estoque> findByQuantidade(@NotNull Integer quantidade) {
        if (quantidade == null) {
            throw new IllegalArgumentException("Quantidade não pode ser nula");
        }
        
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva ou zero");
        }
        
        return estoqueRepository.findByQuantidade(quantidade);
    }
    
    /**
     * Busca registros de estoque por faixa de quantidade.
     * 
     * @param quantidadeMinima Quantidade mínima
     * @param quantidadeMaxima Quantidade máxima
     * @return Lista de registros na faixa especificada
     */
    public List<Estoque> findByQuantidadeBetween(@NotNull Integer quantidadeMinima, 
                                                @NotNull Integer quantidadeMaxima) {
        if (quantidadeMinima == null || quantidadeMaxima == null) {
            throw new IllegalArgumentException("Quantidades não podem ser nulas");
        }
        
        if (quantidadeMinima < 0 || quantidadeMaxima < 0) {
            throw new IllegalArgumentException("Quantidades devem ser positivas ou zero");
        }
        
        if (quantidadeMinima > quantidadeMaxima) {
            throw new IllegalArgumentException("Quantidade mínima deve ser menor que a máxima");
        }
        
        return estoqueRepository.findByQuantidadeBetween(quantidadeMinima, quantidadeMaxima);
    }
    
    /**
     * Busca registros com estoque baixo.
     * 
     * @param quantidadeMinima Quantidade considerada como estoque baixo
     * @return Lista de registros com estoque baixo
     */
    public List<Estoque> findEstoqueBaixo(@NotNull Integer quantidadeMinima) {
        if (quantidadeMinima == null) {
            throw new IllegalArgumentException("Quantidade mínima não pode ser nula");
        }
        
        if (quantidadeMinima <= 0) {
            throw new IllegalArgumentException("Quantidade mínima deve ser positiva");
        }
        
        return estoqueRepository.findEstoqueBaixo(quantidadeMinima);
    }
    
    /**
     * Busca registros com estoque zerado.
     * 
     * @return Lista de registros com quantidade zero
     */
    public List<Estoque> findEstoqueZerado() {
        return estoqueRepository.findEstoqueZerado();
    }
    
    /**
     * Conta registros de estoque por produto.
     * 
     * @param idProduto ID do produto
     * @return Quantidade de registros do produto
     */
    public Long countByIdProduto(@NotNull Integer idProduto) {
        if (idProduto == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo");
        }
        return estoqueRepository.countByIdProduto(idProduto);
    }
    
    /**
     * Soma quantidade total em estoque por produto.
     * 
     * @param idProduto ID do produto
     * @return Quantidade total em estoque do produto
     */
    public Long sumQuantidadeByIdProduto(@NotNull Integer idProduto) {
        if (idProduto == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo");
        }
        return estoqueRepository.sumQuantidadeByIdProduto(idProduto);
    }
    
    /**
     * Adiciona quantidade ao estoque de um produto.
     * 
     * @param idProduto ID do produto
     * @param idLote ID do lote (pode ser nulo)
     * @param quantidade Quantidade a ser adicionada
     * @return Registro de estoque atualizado ou criado
     */
    @Transactional
    public Estoque adicionarEstoque(@NotNull Integer idProduto, Integer idLote, 
                                   @NotNull Integer quantidade) {
        if (idProduto == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo");
        }
        
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        
        // Buscar estoque existente para o produto+lote
        List<Estoque> estoquesExistentes = estoqueRepository.findByIdProduto(idProduto);
        Estoque estoqueExistente = estoquesExistentes.stream()
            .filter(e -> (idLote == null && e.getIdLote() == null) || 
                        (idLote != null && idLote.equals(e.getIdLote())))
            .findFirst()
            .orElse(null);
        
        if (estoqueExistente != null) {
            // Atualizar estoque existente
            estoqueExistente.setQtdEstoque(estoqueExistente.getQtdEstoque() + quantidade);
            return update(estoqueExistente);
        } else {
            // Criar novo registro de estoque
            Estoque novoEstoque = new Estoque();
            novoEstoque.setIdProduto(idProduto);
            novoEstoque.setIdLote(idLote);
            novoEstoque.setQtdEstoque(quantidade);
            return create(novoEstoque);
        }
    }
    
    /**
     * Remove quantidade do estoque de um produto.
     * 
     * @param idProduto ID do produto
     * @param idLote ID do lote (pode ser nulo)
     * @param quantidade Quantidade a ser removida
     * @return Registro de estoque atualizado
     * @throws IllegalStateException se não houver estoque suficiente
     */
    @Transactional
    public Estoque removerEstoque(@NotNull Integer idProduto, Integer idLote, 
                                 @NotNull Integer quantidade) {
        if (idProduto == null) {
            throw new IllegalArgumentException("ID do produto não pode ser nulo");
        }
        
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }
        
        // Buscar estoque existente para o produto+lote
        List<Estoque> estoquesExistentes = estoqueRepository.findByIdProduto(idProduto);
        Estoque estoqueExistente = estoquesExistentes.stream()
            .filter(e -> (idLote == null && e.getIdLote() == null) || 
                        (idLote != null && idLote.equals(e.getIdLote())))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Estoque não encontrado para produto: " + idProduto + 
                (idLote != null ? ", lote: " + idLote : "")));
        
        // Verificar se há estoque suficiente
        if (estoqueExistente.getQtdEstoque() < quantidade) {
            throw new IllegalStateException(
                "Estoque insuficiente. Disponível: " + estoqueExistente.getQtdEstoque() + 
                ", Solicitado: " + quantidade);
        }
        
        // Atualizar quantidade
        estoqueExistente.setQtdEstoque(estoqueExistente.getQtdEstoque() - quantidade);
        return update(estoqueExistente);
    }
    
    /**
     * Valida regras de negócio gerais para criação/atualização.
     * 
     * @param estoque Registro a ser validado
     * @throws IllegalArgumentException se alguma regra for violada
     */
    private void validateBusinessRules(Estoque estoque) {
        // Validação: ID do produto é obrigatório
        if (estoque.getIdProduto() == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório");
        }
        
        // Validação: quantidade deve ser positiva ou zero
        if (estoque.getQtdEstoque() != null && estoque.getQtdEstoque() < 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva ou zero");
        }
    }
    
    /**
     * Valida se não existe duplicação de produto+lote.
     * 
     * @param estoque Registro a ser validado
     * @throws IllegalArgumentException se já existir registro para o mesmo produto+lote
     */
    private void validateUniqueProductLot(Estoque estoque) {
        List<Estoque> existentes = estoqueRepository.findByIdProduto(estoque.getIdProduto());
        
        boolean jaExiste = existentes.stream()
            .anyMatch(e -> (estoque.getIdLote() == null && e.getIdLote() == null) ||
                          (estoque.getIdLote() != null && estoque.getIdLote().equals(e.getIdLote())));
        
        if (jaExiste) {
            throw new IllegalArgumentException(
                "Já existe registro de estoque para produto: " + estoque.getIdProduto() + 
                (estoque.getIdLote() != null ? ", lote: " + estoque.getIdLote() : ""));
        }
    }
    
    /**
     * Valida regras específicas para atualização.
     * 
     * @param novoEstoque Nova versão do registro
     * @param estoqueExistente Registro existente no banco
     * @throws IllegalStateException se alguma regra de atualização for violada
     */
    private void validateUpdateRules(Estoque novoEstoque, Estoque estoqueExistente) {
        // Validação: não permitir alterar ID do produto após criação
        if (!estoqueExistente.getIdProduto().equals(novoEstoque.getIdProduto())) {
            throw new IllegalStateException(
                "ID do produto não pode ser alterado após criação");
        }
        
        // Validação: não permitir alterar ID do lote após criação
        if ((estoqueExistente.getIdLote() == null && novoEstoque.getIdLote() != null) ||
            (estoqueExistente.getIdLote() != null && !estoqueExistente.getIdLote().equals(novoEstoque.getIdLote()))) {
            throw new IllegalStateException(
                "ID do lote não pode ser alterado após criação");
        }
        
        // Aplicar validações gerais
        validateBusinessRules(novoEstoque);
    }
    
    /**
     * Valida se um registro pode ser deletado.
     * 
     * @param estoque Registro a ser deletado
     * @throws IllegalStateException se não puder ser deletado
     */
    private void validateDeletion(Estoque estoque) {
        // Validação: não permitir deletar se houver quantidade em estoque
        if (estoque.getQtdEstoque() != null && estoque.getQtdEstoque() > 0) {
            throw new IllegalStateException(
                "Não é possível deletar registro com estoque disponível (Quantidade: " + 
                estoque.getQtdEstoque() + ")");
        }
    }
}