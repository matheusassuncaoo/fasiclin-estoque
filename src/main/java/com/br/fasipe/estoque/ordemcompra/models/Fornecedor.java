package com.br.fasipe.estoque.ordemcompra.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um fornecedor no sistema.
 * 
 * <p>
 * Esta classe mapeia a tabela FORNECEDOR do banco de dados MySQL e contém
 * informações completas sobre fornecedores, incluindo dados básicos de
 * identificação,
 * representante comercial e informações de contato.
 * </p>
 * 
 * <p>
 * <strong>Estrutura da tabela FORNECEDOR:</strong>
 * </p>
 * <ul>
 * <li><strong>IDFORNECEDOR:</strong> INT PRIMARY KEY AUTO_INCREMENT</li>
 * <li><strong>ID_PESSOA:</strong> INT NOT NULL UNIQUE</li>
 * <li><strong>REPRESENT:</strong> VARCHAR(100) (opcional)</li>
 * <li><strong>CONTREPRE:</strong> CHAR(15) (opcional)</li>
 * <li><strong>DECRICAO:</strong> VARCHAR(250) (opcional)</li>
 * </ul>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@Entity
@Table(name = "FORNECEDOR")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Fornecedor {

    public static final String TABLE_NAME = "FORNECEDOR";

    /**
     * Identificador único do fornecedor.
     * <p>
     * <strong>Coluna do banco:</strong> IDFORNECEDOR (INT, PRIMARY KEY,
     * AUTO_INCREMENT)
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDFORNECEDOR")
    private Integer id;

    /**
     * ID da pessoa (física ou jurídica) associada ao fornecedor.
     * <p>
     * <strong>Coluna do banco:</strong> ID_PESSOA (INT, NOT NULL, UNIQUE)
     * </p>
     */
    @NotNull(message = "ID da pessoa é obrigatório")
    @Column(name = "ID_PESSOA", nullable = false, unique = true)
    private Integer idPessoa;

    /**
     * Nome do representante comercial do fornecedor.
     * <p>
     * <strong>Coluna do banco:</strong> REPRESENT (VARCHAR(100), opcional)
     * </p>
     */
    @Size(max = 100, message = "Nome do representante deve ter no máximo 100 caracteres")
    @Column(name = "REPRESENT", length = 100)
    private String representante;

    /**
     * Contato do representante comercial (telefone/celular).
     * <p>
     * <strong>Coluna do banco:</strong> CONTREPRE (CHAR(15), opcional)
     * </p>
     */
    @Size(max = 15, message = "Contato do representante deve ter no máximo 15 caracteres")
    @Column(name = "CONTREPRE", length = 15)
    private String contatoRepresentante;

    /**
     * Descrição adicional do fornecedor.
     * <p>
     * <strong>Coluna do banco:</strong> DECRICAO (VARCHAR(250), opcional)
     * </p>
     * 
     * <p>
     * <strong>Nota:</strong> O nome da coluna no banco está com erro de grafia
     * (DECRICAO ao invés de DESCRICAO)
     * </p>
     */
    @Size(max = 250, message = "Descrição deve ter no máximo 250 caracteres")
    @Column(name = "DECRICAO", length = 250)
    private String descricao;

    // Métodos utilitários

    /**
     * Verifica se o fornecedor tem representante comercial.
     * 
     * @return true se tiver representante, false caso contrário
     */
    public boolean temRepresentante() {
        return representante != null && !representante.trim().isEmpty();
    }

    /**
     * Verifica se o fornecedor tem contato do representante.
     * 
     * @return true se tiver contato do representante, false caso contrário
     */
    public boolean temContatoRepresentante() {
        return contatoRepresentante != null && !contatoRepresentante.trim().isEmpty();
    }

    /**
     * Verifica se o fornecedor tem descrição.
     * 
     * @return true se tiver descrição, false caso contrário
     */
    public boolean temDescricao() {
        return descricao != null && !descricao.trim().isEmpty();
    }

    /**
     * Obtém informações resumidas do fornecedor.
     * 
     * @return String com informações básicas do fornecedor
     */
    public String getResumo() {
        StringBuilder resumo = new StringBuilder();
        resumo.append("Fornecedor ID: ").append(id);

        if (temRepresentante()) {
            resumo.append(", Representante: ").append(representante);
        }

        if (temContatoRepresentante()) {
            resumo.append(", Contato: ").append(contatoRepresentante);
        }

        return resumo.toString();
    }

}