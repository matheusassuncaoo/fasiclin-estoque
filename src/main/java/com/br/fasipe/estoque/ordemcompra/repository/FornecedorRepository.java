package com.br.fasipe.estoque.ordemcompra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.br.fasipe.estoque.ordemcompra.models.Fornecedor;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface para operações de acesso aos dados da entidade Fornecedor.
 * 
 * <p>Esta interface estende {@link JpaRepository} para fornecer operações CRUD básicas
 * e define métodos de consulta personalizados para atender às necessidades específicas
 * do sistema de estoque.
 * 
 * <p>Utiliza QueryHints para otimização de performance nas consultas de leitura.
 * 
 * @author Sistema de Estoque
 * @since 1.0
 * @see Fornecedor
 */
@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {

    /**
     * Busca fornecedores por ID de pessoa.
     * 
     * @param idPessoa ID da pessoa
     * @return Optional contendo o fornecedor se encontrado
     */
    @Query("SELECT f FROM Fornecedor f WHERE f.idPessoa = :idPessoa")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    Optional<Fornecedor> findByIdPessoa(@Param("idPessoa") Integer idPessoa);

    /**
     * Busca fornecedores que contenham o texto especificado no campo representante.
     * 
     * @param representante termo de busca para o representante (case insensitive)
     * @return Lista de fornecedores encontrados
     */
    @Query("SELECT f FROM Fornecedor f WHERE LOWER(f.representante) LIKE LOWER(CONCAT('%', :representante, '%'))")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Fornecedor> findByRepresentanteContaining(@Param("representante") String representante);

    /**
     * Busca fornecedores que contenham o texto especificado na descrição.
     * 
     * @param descricao termo de busca para a descrição (case insensitive)
     * @return Lista de fornecedores encontrados
     */
    @Query("SELECT f FROM Fornecedor f WHERE LOWER(f.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Fornecedor> findByDescricaoContaining(@Param("descricao") String descricao);

    /**
     * Busca fornecedores pelo contato do representante.
     * 
     * @param contatoRepresentante contato do representante
     * @return Lista de fornecedores encontrados
     */
    @Query("SELECT f FROM Fornecedor f WHERE f.contatoRepresentante = :contatoRepresentante")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Fornecedor> findByContatoRepresentante(@Param("contatoRepresentante") String contatoRepresentante);

    /**
     * Busca fornecedores ordenados por representante.
     * 
     * @return Lista de todos os fornecedores ordenados por representante
     */
    @Query("SELECT f FROM Fornecedor f ORDER BY f.representante")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Fornecedor> findAllOrderByRepresentante();

    /**
     * Verifica se existe fornecedor para o ID de pessoa especificado.
     * 
     * @param idPessoa ID da pessoa
     * @return true se existe, false caso contrário
     */
    @Query("SELECT COUNT(f) > 0 FROM Fornecedor f WHERE f.idPessoa = :idPessoa")
    @QueryHints({
        @QueryHint(name = "org.hibernate.readOnly", value = "true")
    })
    boolean existsByIdPessoa(@Param("idPessoa") Integer idPessoa);
}