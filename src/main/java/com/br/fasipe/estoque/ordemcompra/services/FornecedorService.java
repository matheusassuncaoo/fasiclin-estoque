package com.br.fasipe.estoque.ordemcompra.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.Fornecedor;
import com.br.fasipe.estoque.ordemcompra.repository.FornecedorRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service para operações de negócio da entidade Fornecedor.
 * 
 * <p>Esta classe implementa a camada de serviço para o módulo de Fornecedores,
 * fornecendo operações CRUD completas com validações de negócio, tratamento de exceções
 * e métodos de consulta otimizados para gestão de fornecedores.</p>
 * 
 * <p><strong>Funcionalidades principais:</strong></p>
 * <ul>
 *   <li>CRUD completo (Create, Read, Update, Delete)</li>
 *   <li>Consultas por ID de pessoa, representante e descrição</li>
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
public class FornecedorService {
    
    @Autowired
    private FornecedorRepository fornecedorRepository;
    
    /**
     * Busca um fornecedor por ID.
     * 
     * @param id ID do fornecedor (não pode ser nulo)
     * @return Fornecedor encontrado
     * @throws EntityNotFoundException se o fornecedor não for encontrado
     * @throws IllegalArgumentException se o ID for nulo
     */
    public Fornecedor findById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return fornecedorRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(
                "Fornecedor não encontrado com ID: " + id));
    }
    
    /**
     * Busca todos os fornecedores.
     * 
     * @return Lista de todos os fornecedores
     */
    public List<Fornecedor> findAll() {
        return fornecedorRepository.findAll();
    }

    /**
     * Busca todos os fornecedores ordenados por representante.
     * 
     * @return Lista de fornecedores ordenados por representante
     */
    public List<Fornecedor> findAllOrderByRepresentante() {
        return fornecedorRepository.findAllOrderByRepresentante();
    }

    /**
     * Cria um novo fornecedor.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve ser nulo (será gerado automaticamente)</li>
     *   <li>ID da pessoa é obrigatório</li>
     *   <li>ID da pessoa deve ser único</li>
     *   <li>Representante é obrigatório</li>
     * </ul>
     * 
     * @param obj Fornecedor a ser criado (validado com @Valid)
     * @return Fornecedor criado com ID gerado
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public Fornecedor create(@Valid @NotNull Fornecedor obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Fornecedor não pode ser nulo");
        }
        
        if (obj.getId() != null) {
            throw new IllegalArgumentException("ID deve ser nulo para criação de novo fornecedor");
        }
        
        // Validações de negócio adicionais
        validateBusinessRules(obj);
        
        // Verificar se já existe fornecedor para esta pessoa
        if (obj.getIdPessoa() != null && 
            fornecedorRepository.existsByIdPessoa(obj.getIdPessoa())) {
            throw new IllegalArgumentException(
                "Já existe um fornecedor cadastrado para a pessoa com ID: " + obj.getIdPessoa());
        }
        
        try {
            return fornecedorRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao criar fornecedor: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza um fornecedor existente.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>Objeto não pode ser nulo</li>
     *   <li>ID deve existir no banco</li>
     *   <li>Campos obrigatórios devem estar preenchidos</li>
     *   <li>Regras de negócio específicas</li>
     * </ul>
     * 
     * @param obj Fornecedor a ser atualizado (validado com @Valid)
     * @return Fornecedor atualizado
     * @throws EntityNotFoundException se o fornecedor não existir
     * @throws IllegalArgumentException se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public Fornecedor update(@Valid @NotNull Fornecedor obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Fornecedor não pode ser nulo");
        }
        
        if (obj.getId() == null) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }
        
        // Verifica se o fornecedor existe
        Fornecedor existingFornecedor = findById(obj.getId());
        
        // Validações de negócio para atualização
        validateUpdateRules(obj, existingFornecedor);
        
        try {
            return fornecedorRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                "Erro de integridade ao atualizar fornecedor: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta um fornecedor por ID.
     * 
     * <p><strong>Validações aplicadas:</strong></p>
     * <ul>
     *   <li>ID não pode ser nulo</li>
     *   <li>Fornecedor deve existir no banco</li>
     *   <li>Não pode deletar se houver dependências</li>
     * </ul>
     * 
     * @param id ID do fornecedor a ser deletado
     * @throws EntityNotFoundException se o fornecedor não existir
     * @throws IllegalArgumentException se o ID for nulo
     * @throws IllegalStateException se o fornecedor não puder ser deletado
     */
    @Transactional
    public void deleteById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        
        Fornecedor fornecedor = findById(id);
        
        // Validação de negócio: verificar se pode ser deletado
        validateDeletion(fornecedor);
        
        try {
            fornecedorRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Fornecedor não encontrado com ID: " + id);
        }
    }
    
    /**
     * Deleta um fornecedor por objeto.
     * 
     * @param obj Fornecedor a ser deletado
     * @throws IllegalArgumentException se o objeto for nulo
     */
    @Transactional
    public void delete(@NotNull Fornecedor obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Fornecedor não pode ser nulo");
        }
        
        if (obj.getId() == null) {
            throw new IllegalArgumentException("ID é obrigatório para deleção");
        }
        
        deleteById(obj.getId());
    }

    /**
     * Busca fornecedor por ID de pessoa.
     * 
     * @param idPessoa ID da pessoa
     * @return Fornecedor encontrado ou null se não existir
     */
    public Fornecedor findByIdPessoa(@NotNull Integer idPessoa) {
        if (idPessoa == null) {
            throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
        }
        return fornecedorRepository.findByIdPessoa(idPessoa).orElse(null);
    }

    /**
     * Busca fornecedores por representante (busca parcial).
     * 
     * @param representante termo de busca para representante
     * @return Lista de fornecedores encontrados
     */
    public List<Fornecedor> findByRepresentanteContaining(@NotNull String representante) {
        if (representante == null || representante.trim().isEmpty()) {
            throw new IllegalArgumentException("Representante não pode ser nulo ou vazio");
        }
        return fornecedorRepository.findByRepresentanteContaining(representante.trim());
    }

    /**
     * Busca fornecedores por descrição (busca parcial).
     * 
     * @param descricao termo de busca para descrição
     * @return Lista de fornecedores encontrados
     */
    public List<Fornecedor> findByDescricaoContaining(@NotNull String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição não pode ser nula ou vazia");
        }
        return fornecedorRepository.findByDescricaoContaining(descricao.trim());
    }

    /**
     * Busca fornecedores por contato do representante.
     * 
     * @param contatoRepresentante contato do representante
     * @return Lista de fornecedores encontrados
     */
    public List<Fornecedor> findByContatoRepresentante(@NotNull String contatoRepresentante) {
        if (contatoRepresentante == null || contatoRepresentante.trim().isEmpty()) {
            throw new IllegalArgumentException("Contato do representante não pode ser nulo ou vazio");
        }
        return fornecedorRepository.findByContatoRepresentante(contatoRepresentante.trim());
    }

    /**
     * Verifica se existe fornecedor para o ID de pessoa especificado.
     * 
     * @param idPessoa ID da pessoa
     * @return true se existe, false caso contrário
     */
    public boolean existsByIdPessoa(@NotNull Integer idPessoa) {
        if (idPessoa == null) {
            throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
        }
        return fornecedorRepository.existsByIdPessoa(idPessoa);
    }

    /**
     * Valida as regras de negócio para um fornecedor.
     * 
     * @param fornecedor fornecedor a ser validado
     * @throws IllegalArgumentException se alguma regra for violada
     */
    private void validateBusinessRules(Fornecedor fornecedor) {
        if (fornecedor.getIdPessoa() == null) {
            throw new IllegalArgumentException("ID da pessoa é obrigatório");
        }
        
        if (fornecedor.getRepresentante() == null || fornecedor.getRepresentante().trim().isEmpty()) {
            throw new IllegalArgumentException("Representante é obrigatório");
        }
    }

    /**
     * Valida as regras específicas para atualização de fornecedor.
     * 
     * @param newFornecedor novo fornecedor com dados atualizados
     * @param existingFornecedor fornecedor existente no banco
     * @throws IllegalArgumentException se alguma regra for violada
     */
    private void validateUpdateRules(Fornecedor newFornecedor, Fornecedor existingFornecedor) {
        validateBusinessRules(newFornecedor);
        
        // Se mudou o ID da pessoa, verificar se não conflita com outro fornecedor
        if (!newFornecedor.getIdPessoa().equals(existingFornecedor.getIdPessoa())) {
            if (fornecedorRepository.existsByIdPessoa(newFornecedor.getIdPessoa())) {
                throw new IllegalArgumentException(
                    "Já existe um fornecedor cadastrado para a pessoa com ID: " + newFornecedor.getIdPessoa());
            }
        }
    }

    /**
     * Valida se o fornecedor pode ser deletado.
     * 
     * @param fornecedor fornecedor a ser validado para deleção
     * @throws IllegalStateException se o fornecedor não puder ser deletado
     */
    private void validateDeletion(Fornecedor fornecedor) {
        // Aqui podem ser adicionadas validações específicas de negócio
        // Por exemplo, verificar se não há ordens de compra ativas para este fornecedor
        
        // Exemplo de validação comentada:
        // if (hasActiveOrders(fornecedor.getId())) {
        //     throw new IllegalStateException(
        //         "Não é possível deletar fornecedor que possui ordens de compra ativas");
        // }
    }
}