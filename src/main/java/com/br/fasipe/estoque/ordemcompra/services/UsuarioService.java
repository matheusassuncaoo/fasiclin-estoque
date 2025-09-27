package com.br.fasipe.estoque.ordemcompra.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.Usuario;
import com.br.fasipe.estoque.ordemcompra.repository.UsuarioRepository;

import jakarta.validation.constraints.NotBlank;

/**
 * Service para operações de autenticação de usuários.
 * 
 * Fornece métodos para validação de credenciais de usuários do sistema.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2025
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Autentica um usuário com login e senha.
     * 
     * @param login login do usuário
     * @param senha senha em texto plano
     * @return true se as credenciais são válidas
     */
    public boolean autenticarUsuario(@NotBlank String login, @NotBlank String senha) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }

        if (senha == null || senha.trim().isEmpty()) {
            return false;
        }

        try {
            // Por simplicidade, vamos assumir que a senha no banco pode estar em texto
            // plano
            // ou com hash simples MD5 (não recomendado para produção)

            // Primeiro, tentar com senha em texto plano
            boolean existsPlain = usuarioRepository.existsByLoginAndSenha(login.trim(), senha.trim());
            if (existsPlain) {
                return true;
            }

            // Se não encontrou, tentar com hash MD5
            String senhaHash = gerarHashMD5(senha.trim());
            boolean existsHash = usuarioRepository.existsByLoginAndSenha(login.trim(), senhaHash);

            return existsHash;

        } catch (Exception e) {
            System.err.println("Erro ao autenticar usuário: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca usuário por login.
     * 
     * @param login login do usuário
     * @return Optional contendo o usuário se encontrado
     */
    public Optional<Usuario> buscarPorLogin(@NotBlank String login) {
        if (login == null || login.trim().isEmpty()) {
            return Optional.empty();
        }

        return usuarioRepository.findByLogin(login.trim());
    }

    /**
     * Gera hash MD5 de uma string.
     * 
     * NOTA: MD5 não é seguro para produção. Use BCrypt ou similar em ambiente real.
     * 
     * @param input string para gerar hash
     * @return hash MD5 da string
     */
    private String gerarHashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash MD5", e);
        }
    }

    /**
     * Verifica se um usuário existe pelo login.
     * 
     * @param login login do usuário
     * @return true se o usuário existe
     */
    public boolean existePorLogin(@NotBlank String login) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }

        return usuarioRepository.findByLogin(login.trim()).isPresent();
    }
}