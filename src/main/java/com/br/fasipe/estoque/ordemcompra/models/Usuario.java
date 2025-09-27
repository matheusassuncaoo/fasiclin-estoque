package com.br.fasipe.estoque.ordemcompra.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade Usuario representando a tabela USUARIO do banco de dados.
 * 
 * Esta classe representa um usuário do sistema com credenciais de autenticação.
 * É utilizada para validação de usuários em operações sensíveis como
 * desativação de ordens.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "USUARIO")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDUSUARIO")
    private Integer id;

    @Column(name = "ID_PROFISSIO")
    private Integer idProfissional;

    @Column(name = "ID_PESSOAFIS")
    private Integer idPessoaFisica;

    @NotBlank(message = "Login é obrigatório")
    @Size(min = 3, max = 100, message = "Login deve ter entre 3 e 100 caracteres")
    @Column(name = "LOGUSUARIO", nullable = false, unique = true, length = 100)
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    @Column(name = "SENHAUSUA", nullable = false, length = 250)
    private String senha;
}