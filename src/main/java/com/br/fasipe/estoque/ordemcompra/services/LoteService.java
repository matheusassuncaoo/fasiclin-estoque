package com.br.fasipe.estoque.ordemcompra.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.Lote;
import com.br.fasipe.estoque.ordemcompra.repository.LoteRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service para operações de negócio da entidade Lote.
 * 
 * <p>
 * Esta classe implementa a camada de serviço para o módulo de Lotes,
 * fornecendo operações CRUD completas com validações de negócio, tratamento de
 * exceções
 * e métodos de consulta otimizados para gestão de lotes de produtos.
 * </p>
 * 
 * <p>
 * <strong>Funcionalidades principais:</strong>
 * </p>
 * <ul>
 * <li>CRUD completo (Create, Read, Update, Delete)</li>
 * <li>Consultas por ordem de compra, data de vencimento e quantidade</li>
 * <li>Operações de controle de estoque por lote</li>
 * <li>Alertas de lotes vencidos e próximos ao vencimento</li>
 * <li>Gestão de lotes com quantidade baixa ou zerada</li>
 * <li>Validações de integridade de dados</li>
 * <li>Tratamento robusto de exceções</li>
 * <li>Transações controladas</li>
 * </ul>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private EstoqueService estoqueService;

    /**
     * Busca um lote por ID.
     * 
     * @param id ID do lote (não pode ser nulo)
     * @return Lote encontrado
     * @throws EntityNotFoundException  se o lote não for encontrado
     * @throws IllegalArgumentException se o ID for nulo
     */
    public Lote findById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return loteRepository.findByIdLote(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Lote não encontrado com ID: " + id));
    }

    /**
     * Busca todos os lotes.
     * 
     * @return Lista de todos os lotes
     */
    public List<Lote> findAll() {
        return loteRepository.findAll();
    }

    /**
     * Cria um novo lote.
     * 
     * <p>
     * <strong>Validações aplicadas:</strong>
     * </p>
     * <ul>
     * <li>Objeto não pode ser nulo</li>
     * <li>ID deve ser nulo (será gerado automaticamente)</li>
     * <li>ID da ordem de compra é obrigatório</li>
     * <li>Quantidade deve ser positiva ou zero</li>
     * <li>Data de vencimento deve ser futura</li>
     * </ul>
     * 
     * @param obj Lote a ser criado (validado com @Valid)
     * @return Lote criado com ID gerado
     * @throws IllegalArgumentException        se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public Lote create(@Valid @NotNull Lote obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Lote não pode ser nulo");
        }

        if (obj.getIdLote() != null) {
            throw new IllegalArgumentException("ID deve ser nulo para criação de novo lote");
        }

        // Validações de negócio adicionais
        validateBusinessRules(obj);

        try {
            return loteRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "Erro de integridade ao criar lote: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza um lote existente.
     * 
     * <p>
     * <strong>Validações aplicadas:</strong>
     * </p>
     * <ul>
     * <li>Objeto não pode ser nulo</li>
     * <li>ID deve existir no banco</li>
     * <li>Campos obrigatórios devem estar preenchidos</li>
     * <li>Regras de negócio específicas</li>
     * </ul>
     * 
     * @param obj Lote a ser atualizado (validado com @Valid)
     * @return Lote atualizado
     * @throws EntityNotFoundException         se o lote não existir
     * @throws IllegalArgumentException        se validações falharem
     * @throws DataIntegrityViolationException se houver violação de integridade
     */
    @Transactional
    public Lote update(@Valid @NotNull Lote obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Lote não pode ser nulo");
        }

        if (obj.getIdLote() == null) {
            throw new IllegalArgumentException("ID é obrigatório para atualização");
        }

        // Verifica se o lote existe
        Lote existingLote = findById(obj.getIdLote());

        // Validações de negócio para atualização
        validateUpdateRules(obj, existingLote);

        try {
            return loteRepository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "Erro de integridade ao atualizar lote: " + e.getMessage(), e);
        }
    }

    /**
     * Deleta um lote por ID.
     * 
     * <p>
     * <strong>Validações aplicadas:</strong>
     * </p>
     * <ul>
     * <li>ID não pode ser nulo</li>
     * <li>Lote deve existir no banco</li>
     * <li>Não pode deletar se houver estoque relacionado</li>
     * </ul>
     * 
     * @param id ID do lote a ser deletado
     * @throws EntityNotFoundException  se o lote não existir
     * @throws IllegalArgumentException se o ID for nulo
     * @throws IllegalStateException    se o lote não puder ser deletado
     */
    @Transactional
    public void deleteById(@NotNull Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }

        Lote lote = findById(id);

        // Validação de negócio: verificar se pode ser deletado
        validateDeletion(lote);

        try {
            loteRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("Lote não encontrado com ID: " + id);
        }
    }

    /**
     * Deleta um lote por objeto.
     * 
     * @param obj Lote a ser deletado
     * @throws IllegalArgumentException se o objeto for nulo
     */
    @Transactional
    public void delete(@NotNull Lote obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Lote não pode ser nulo");
        }

        deleteById(obj.getIdLote());
    }

    /**
     * Busca lotes por data de validade.
     * 
     * @param dataValidade Data de validade (não pode ser nula)
     * @return Lista de lotes com a data de validade especificada
     * @throws IllegalArgumentException se a data for nula
     */
    public List<Lote> findByDataValidade(@NotNull LocalDate dataValidade) {
        if (dataValidade == null) {
            throw new IllegalArgumentException("Data de validade não pode ser nula");
        }
        return loteRepository.findByDataVencimento(dataValidade);
    }

    /**
     * Busca lotes por ID da ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra (não pode ser nulo)
     * @return Lista de lotes da ordem de compra especificada
     * @throws IllegalArgumentException se o ID for nulo
     */
    public List<Lote> findByIdOrdemCompra(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        return loteRepository.findByIdOrdComp(idOrdemCompra);
    }

    /**
     * Busca lotes válidos (não vencidos).
     * 
     * @return Lista de lotes válidos (com data de validade posterior à data atual)
     */
    public List<Lote> findLotesValidos() {
        LocalDate hoje = LocalDate.now();
        return loteRepository.findByDataVencimentoBetween(hoje, hoje.plusYears(100));
    }

    /**
     * Busca lotes por faixa de datas de validade.
     * 
     * @param dataInicio Data inicial (não pode ser nula)
     * @param dataFim    Data final (não pode ser nula)
     * @return Lista de lotes com validade dentro do período especificado
     * @throws IllegalArgumentException se alguma data for nula ou se a data inicial
     *                                  for posterior à final
     */
    public List<Lote> findByDataValidadeBetween(@NotNull LocalDate dataInicio, @NotNull LocalDate dataFim) {
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data inicial não pode ser nula");
        }
        if (dataFim == null) {
            throw new IllegalArgumentException("Data final não pode ser nula");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
        return loteRepository.findByDataVencimentoBetween(dataInicio, dataFim);
    }

    /**
     * Busca lotes por data de vencimento específica.
     * 
     * @param dataVencimento Data de vencimento
     * @return Lista de lotes com a data de vencimento especificada
     */
    public List<Lote> findByDataVencimento(@NotNull LocalDate dataVencimento) {
        if (dataVencimento == null) {
            throw new IllegalArgumentException("Data de vencimento não pode ser nula");
        }
        return loteRepository.findByDataVencimento(dataVencimento);
    }

    /**
     * Busca lotes por faixa de datas de vencimento.
     * 
     * @param dataInicio Data inicial
     * @param dataFim    Data final
     * @return Lista de lotes na faixa de datas
     */
    public List<Lote> findByDataVencimentoBetween(@NotNull LocalDate dataInicio,
            @NotNull LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas");
        }

        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data inicial deve ser anterior à data final");
        }

        return loteRepository.findByDataVencimentoBetween(dataInicio, dataFim);
    }

    /**
     * Busca lotes vencidos.
     * 
     * @return Lista de lotes com data de vencimento anterior à data atual
     */
    public List<Lote> findLotesVencidos() {
        return loteRepository.findLotesVencidos();
    }

    /**
     * Busca lotes próximos ao vencimento (próximos 30 dias).
     * 
     * @return Lista de lotes próximos ao vencimento
     */
    public List<Lote> findLotesProximosVencimento() {
        LocalDate dataLimite = LocalDate.now().plusDays(30);
        return loteRepository.findLotesProximosVencimento(dataLimite);
    }

    /**
     * Busca lotes por quantidade específica.
     * 
     * @param quantidade Quantidade do lote
     * @return Lista de lotes com a quantidade especificada
     */
    public List<Lote> findByQuantidade(@NotNull Integer quantidade) {
        if (quantidade == null) {
            throw new IllegalArgumentException("Quantidade não pode ser nula");
        }

        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva ou zero");
        }

        return loteRepository.findByQuantidade(quantidade);
    }

    /**
     * Busca lotes por faixa de quantidade.
     * 
     * @param quantidadeMinima Quantidade mínima
     * @param quantidadeMaxima Quantidade máxima
     * @return Lista de lotes na faixa de quantidade
     */
    public List<Lote> findByQuantidadeBetween(@NotNull Integer quantidadeMinima,
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

        return loteRepository.findByQuantidadeBetween(quantidadeMinima, quantidadeMaxima);
    }

    /**
     * Busca lotes com quantidade baixa.
     * 
     * @param quantidadeMinima Quantidade considerada como baixa
     * @return Lista de lotes com quantidade baixa
     */
    public List<Lote> findLotesQuantidadeBaixa(@NotNull Integer quantidadeMinima) {
        if (quantidadeMinima == null) {
            throw new IllegalArgumentException("Quantidade mínima não pode ser nula");
        }

        if (quantidadeMinima <= 0) {
            throw new IllegalArgumentException("Quantidade mínima deve ser positiva");
        }

        return loteRepository.findLotesQuantidadeBaixa(quantidadeMinima);
    }

    /**
     * Busca lotes com quantidade zerada.
     * 
     * @return Lista de lotes com quantidade zero
     */
    public List<Lote> findLotesZerados() {
        return loteRepository.findLotesZerados();
    }

    /**
     * Conta lotes por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return Quantidade de lotes da ordem de compra
     */
    public Long countByIdOrdemCompra(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        return loteRepository.countByIdOrdComp(idOrdemCompra);
    }

    /**
     * Soma quantidade total dos lotes por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return Quantidade total dos lotes da ordem de compra
     */
    public Long sumQuantidadeByIdOrdemCompra(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }
        Long total = loteRepository.sumQuantidadeByIdOrdComp(idOrdemCompra);
        return total != null ? total : 0L;
    }

    /**
     * Adiciona quantidade a um lote.
     * 
     * @param idLote     ID do lote
     * @param quantidade Quantidade a ser adicionada
     * @return Lote atualizado
     */
    @Transactional
    public Lote adicionarQuantidade(@NotNull Integer idLote, @NotNull Integer quantidade) {
        if (idLote == null) {
            throw new IllegalArgumentException("ID do lote não pode ser nulo");
        }

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }

        Lote lote = findById(idLote);
        Integer quantidadeAtual = lote.getQntd() != null ? lote.getQntd() : 0;
        lote.setQntd(quantidadeAtual + quantidade);

        return update(lote);
    }

    /**
     * Remove quantidade de um lote.
     * 
     * @param idLote     ID do lote
     * @param quantidade Quantidade a ser removida
     * @return Lote atualizado
     * @throws IllegalStateException se não houver quantidade suficiente
     */
    @Transactional
    public Lote removerQuantidade(@NotNull Integer idLote, @NotNull Integer quantidade) {
        if (idLote == null) {
            throw new IllegalArgumentException("ID do lote não pode ser nulo");
        }

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva");
        }

        Lote lote = findById(idLote);
        Integer quantidadeAtual = lote.getQntd() != null ? lote.getQntd() : 0;

        if (quantidadeAtual < quantidade) {
            throw new IllegalStateException(
                    "Quantidade insuficiente no lote. Disponível: " + quantidadeAtual +
                            ", Solicitado: " + quantidade);
        }

        lote.setQntd(quantidadeAtual - quantidade);
        return update(lote);
    }

    /**
     * Verifica se um lote está vencido.
     * 
     * @param lote Lote a ser verificado
     * @return true se o lote estiver vencido
     */
    public boolean isLoteVencido(@NotNull Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("Lote não pode ser nulo");
        }

        return lote.getDataVenc() != null && lote.getDataVenc().isBefore(LocalDate.now());
    }

    /**
     * Verifica se um lote está próximo ao vencimento (próximos 30 dias).
     * 
     * @param lote Lote a ser verificado
     * @return true se o lote estiver próximo ao vencimento
     */
    public boolean isLoteProximoVencimento(@NotNull Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("Lote não pode ser nulo");
        }

        if (lote.getDataVenc() == null) {
            return false;
        }

        LocalDate dataLimite = LocalDate.now().plusDays(30);
        return lote.getDataVenc().isBefore(dataLimite) && !lote.getDataVenc().isBefore(LocalDate.now());
    }

    /**
     * Valida regras de negócio gerais para criação/atualização.
     * 
     * @param lote Lote a ser validado
     * @throws IllegalArgumentException se alguma regra for violada
     */
    private void validateBusinessRules(Lote lote) {
        // Validação: ID da ordem de compra é obrigatório
        if (lote.getIdOrdComp() == null) {
            throw new IllegalArgumentException("ID da ordem de compra é obrigatório");
        }

        // Validação: quantidade deve ser positiva ou zero
        if (lote.getQntd() != null && lote.getQntd() < 0) {
            throw new IllegalArgumentException("Quantidade deve ser positiva ou zero");
        }

        // Validação: data de vencimento deve ser futura (se informada)
        if (lote.getDataVenc() != null && lote.getDataVenc().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data de vencimento deve ser futura");
        }
    }

    /**
     * Valida regras específicas para atualização.
     * 
     * @param novoLote      Nova versão do lote
     * @param loteExistente Lote existente no banco
     * @throws IllegalStateException se alguma regra de atualização for violada
     */
    private void validateUpdateRules(Lote novoLote, Lote loteExistente) {
        // Validação: não permitir alterar ID da ordem de compra após criação
        if (!loteExistente.getIdOrdComp().equals(novoLote.getIdOrdComp())) {
            throw new IllegalStateException(
                    "ID da ordem de compra não pode ser alterado após criação");
        }

        // Aplicar validações gerais
        validateBusinessRules(novoLote);
    }

    /**
     * Valida se um lote pode ser deletado.
     * 
     * @param lote Lote a ser deletado
     * @throws IllegalStateException se não puder ser deletado
     */
    private void validateDeletion(Lote lote) {
        // Validação: não permitir deletar se houver quantidade no lote
        if (lote.getQntd() != null && lote.getQntd() > 0) {
            throw new IllegalStateException(
                    "Não é possível deletar lote com quantidade disponível (Quantidade: " +
                            lote.getQntd() + ")");
        }
    }

    /**
     * Remove todos os lotes de uma ordem de compra específica, garantindo a
     * remoção prévia de registros de estoque vinculados a cada lote.
     *
     * Regras de negócio:
     * - Se qualquer lote possuir quantidade > 0, a operação será bloqueada com
     *   IllegalStateException (mapeada para HTTP 422 na camada de controller).
     * - Se existir registro de estoque com quantidade > 0 para o lote, a operação
     *   também será bloqueada (IllegalStateException).
     * - Quando permitido, remove primeiro os registros de estoque do lote e em
     *   seguida o lote.
     *
     * @param idOrdemCompra ID da ordem de compra cujos lotes serão removidos
     */
    @Transactional
    public void deleteAllByOrdemId(@NotNull Integer idOrdemCompra) {
        if (idOrdemCompra == null) {
            throw new IllegalArgumentException("ID da ordem de compra não pode ser nulo");
        }

        System.out.println("[LoteService] DEBUG: Iniciando deleteAllByOrdemId para ordem: " + idOrdemCompra);
        
        List<Lote> lotes = findByIdOrdemCompra(idOrdemCompra);
        System.out.println("[LoteService] DEBUG: Encontrados " + lotes.size() + " lotes para a ordem " + idOrdemCompra);
        
        if (lotes.isEmpty()) {
            System.out.println("[LoteService] DEBUG: Nenhum lote encontrado, saindo");
            return; // nada a fazer
        }

        for (Lote lote : lotes) {
            System.out.println("[LoteService] DEBUG: Processando lote ID: " + lote.getIdLote() + ", quantidade: " + lote.getQntd());
            
            // Regra de negócio: não permitir excluir lotes com quantidade > 0
            if (lote.getQntd() != null && lote.getQntd() > 0) {
                System.out.println("[LoteService] ERRO: Lote " + lote.getIdLote() + " possui quantidade > 0");
                throw new IllegalStateException(
                        "Não é possível remover a ordem: lote #" + lote.getIdLote() +
                                " possui quantidade disponível (" + lote.getQntd() + ")");
            }

            // Verificar e remover estoques vinculados ao lote
            try {
                System.out.println("[LoteService] DEBUG: Buscando estoques para lote: " + lote.getIdLote());
                List<com.br.fasipe.estoque.ordemcompra.models.Estoque> estoques = estoqueService
                        .findByIdLote(lote.getIdLote());
                System.out.println("[LoteService] DEBUG: Encontrados " + estoques.size() + " estoques para lote " + lote.getIdLote());
                
                for (com.br.fasipe.estoque.ordemcompra.models.Estoque est : estoques) {
                    System.out.println("[LoteService] DEBUG: Processando estoque ID: " + est.getId() + ", quantidade: " + est.getQtdEstoque());
                    
                    // Bloquear se houver quantidade em estoque
                    if (est.getQtdEstoque() != null && est.getQtdEstoque() > 0) {
                        System.out.println("[LoteService] ERRO: Estoque " + est.getId() + " possui quantidade > 0");
                        throw new IllegalStateException(
                                "Não é possível remover a ordem: estoque do lote #" + lote.getIdLote()
                                        + " possui quantidade (" + est.getQtdEstoque() + ")");
                    }
                    // Se quantidade for zero, remover o registro de estoque
                    System.out.println("[LoteService] DEBUG: Removendo estoque ID: " + est.getId());
                    estoqueService.deleteById(est.getId());
                }

                // Após remover estoques vinculados, remover o lote
                System.out.println("[LoteService] DEBUG: Removendo lote ID: " + lote.getIdLote());
                deleteById(lote.getIdLote());
                System.out.println("[LoteService] DEBUG: Lote " + lote.getIdLote() + " removido com sucesso");
                
            } catch (Exception e) {
                System.out.println("[LoteService] ERRO ao processar lote " + lote.getIdLote() + ": " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        
        System.out.println("[LoteService] DEBUG: Finalizada remoção de todos os lotes para ordem " + idOrdemCompra);
    }
}