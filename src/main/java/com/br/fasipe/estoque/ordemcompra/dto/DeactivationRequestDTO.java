package com.br.fasipe.estoque.ordemcompra.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisições de desativação que requerem autenticação.
 * 
 * Este DTO é usado quando operações sensíveis precisam de validação de credenciais,
 * como desativação de ordens de compra.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2025
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeactivationRequestDTO {

    @NotBlank(message = "Login é obrigatório")
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    private String motivo; // Motivo da desativação (opcional)
}