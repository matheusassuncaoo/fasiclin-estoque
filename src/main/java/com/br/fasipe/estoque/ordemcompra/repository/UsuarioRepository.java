package com.br.fasipe.estoque.ordemcompra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.br.fasipe.estoque.ordemcompra.models.Usuario;

/**
 * Repository para operações de banco de dados da entidade Usuario.
 * 
 * Fornece métodos para autenticação e consulta de usuários.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2025
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca usuário por login.
     * 
     * @param login login do usuário
     * @return Optional contendo o usuário se encontrado
     */
    @Query("SELECT u FROM Usuario u WHERE u.login = :login")
    Optional<Usuario> findByLogin(@Param("login") String login);

    /**
     * Verifica se existe usuário com as credenciais fornecidas.
     * 
     * @param login login do usuário
     * @param senha senha do usuário (deve estar hash)
     * @return true se as credenciais são válidas
     */
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.login = :login AND u.senha = :senha")
    boolean existsByLoginAndSenha(@Param("login") String login, @Param("senha") String senha);

    /**
     * Busca usuário por login e senha.
     * 
     * @param login login do usuário
     * @param senha senha do usuário
     * @return Optional contendo o usuário se as credenciais forem válidas
     */
    @Query("SELECT u FROM Usuario u WHERE u.login = :login AND u.senha = :senha")
    Optional<Usuario> findByLoginAndSenha(@Param("login") String login, @Param("senha") String senha);
}