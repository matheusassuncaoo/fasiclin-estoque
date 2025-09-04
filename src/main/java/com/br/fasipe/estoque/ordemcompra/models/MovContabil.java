package com.br.fasipe.estoque.ordemcompra.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade que representa uma movimentação contábil no sistema.
 * 
 * Esta classe mapeia a tabela MOVCONTABIL do banco de dados e contém
 * informações sobre lançamentos contábeis relacionados a ordens de compra
 * e itens de venda.
 * 
 * Campos principais:
 * - idMovContab: Identificador único da movimentação contábil
 * - numeLancam: Número único do lançamento contábil
 * - dataLancame: Data do lançamento
 * - idOrdComp: ID da ordem de compra relacionada (opcional)
 * - idItemVenda: ID do item de venda relacionado (opcional)
 * - idPlanoConta: ID do plano de contas (obrigatório)
 * - valDbto: Valor do débito
 * - valCdto: Valor do crédito
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "MOVCONTABIL")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovContabil {

    /**
     * Identificador único da movimentação contábil.
     * Chave primária auto-incrementada.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDMOVCONTAB")
    private Integer idMovContab;

    /**
     * Número único do lançamento contábil.
     * Campo obrigatório e único no sistema.
     */
    @NotNull(message = "Número do lançamento é obrigatório")
    @Column(name = "NUMELANCAM", nullable = false, unique = true)
    private Integer numeLancam;

    /**
     * Data do lançamento contábil.
     * Campo opcional.
     */
    @Column(name = "DATALANCAME")
    private LocalDate dataLancame;

    /**
     * ID da ordem de compra relacionada.
     * Campo opcional - usado quando a movimentação está relacionada a uma compra.
     */
    @Column(name = "ID_ORDCOMP")
    private Integer idOrdComp;

    /**
     * ID do item de venda relacionado.
     * Campo opcional - usado quando a movimentação está relacionada a uma venda.
     */
    @Column(name = "ID_ITEMVENDA")
    private Integer idItemVenda;

    /**
     * ID do plano de contas.
     * Campo obrigatório que define a conta contábil do lançamento.
     */
    @NotNull(message = "ID do plano de contas é obrigatório")
    @Column(name = "ID_PLANOCONTA", nullable = false)
    private Integer idPlanoConta;

    /**
     * Valor do débito da movimentação.
     * Campo obrigatório com precisão de 10 dígitos e 2 casas decimais.
     */
    @NotNull(message = "Valor do débito é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor do débito deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Valor do débito deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Column(name = "VALDBTO", nullable = false, precision = 10, scale = 2)
    private BigDecimal valDbto;

    /**
     * Valor do crédito da movimentação.
     * Campo obrigatório com precisão de 10 dígitos e 2 casas decimais.
     */
    @NotNull(message = "Valor do crédito é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor do crédito deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Valor do crédito deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Column(name = "VALCDTO", nullable = false, precision = 10, scale = 2)
    private BigDecimal valCdto;



    // Métodos utilitários

    /**
     * Calcula o saldo da movimentação (débito - crédito).
     * 
     * @return Saldo da movimentação
     */
    public BigDecimal calcularSaldo() {
        if (valDbto != null && valCdto != null) {
            return valDbto.subtract(valCdto);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Verifica se a movimentação está balanceada (débito = crédito).
     * 
     * @return true se estiver balanceada, false caso contrário
     */
    public boolean isBalanceada() {
        return calcularSaldo().compareTo(BigDecimal.ZERO) == 0;
    }


}