package com.br.fasipe.estoque.ordemcompra.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Esta classe mapeia a tabela ESTOQUE do banco de dados.
 * 
 * Relacionamentos no banco:
 * - FK_ESTOQUE_PRODUTO: ID_PRODUTO -> PRODUTO(IDPRODUTO)
 * - FK_ESTOQUE_LOTE: ID_LOTE -> LOTE(IDLOTE)
 * - Referenciada por: MOVIMENTACAO, PRODSOLIC
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 */
@Entity
@Table(name = "ESTOQUE")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Estoque {

    public static final String TABLE_NAME = "ESTOQUE";

    /**
     * Identificador único do registro de estoque.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDESTOQUE")
    private Integer id;

    /**
     * Identificador do produto relacionado ao estoque.
     * Referencia PRODUTO(IDPRODUTO).
     */
    @NotNull(message = "ID do produto é obrigatório")
    @Column(name = "ID_PRODUTO", nullable = false)
    private Integer idProduto;

    /**
     * Identificador do lote do produto.
     * Referencia LOTE(IDLOTE).
     */
    @Column(name = "ID_LOTE")
    private Integer idLote;

    /**
     * Quantidade atual disponível em estoque.
     */
    @NotNull(message = "Quantidade em estoque é obrigatória")
    @PositiveOrZero(message = "Quantidade em estoque deve ser um valor positivo ou zero")
    @Column(name = "QTDESTOQUE", nullable = false)
    private Integer qtdEstoque;
}
