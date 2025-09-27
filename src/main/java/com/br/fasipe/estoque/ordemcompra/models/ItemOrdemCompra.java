package com.br.fasipe.estoque.ordemcompra.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um item de ordem de compra no sistema.
 * 
 * Esta classe mapeia a tabela ITEM_ORDCOMP do banco de dados e contém
 * informações sobre os produtos incluídos em uma ordem de compra,
 * incluindo quantidade, valor e data de vencimento.
 * 
 * Campos principais:
 * - idItemOrd: Identificador único do item da ordem
 * - idOrdComp: ID da ordem de compra relacionada
 * - idProduto: ID do produto
 * - qntd: Quantidade do produto
 * - valor: Valor unitário do produto
 * - vlrTotal: Valor total calculado dinamicamente (quantidade × valor unitário)
 * - dataVenc: Data de vencimento do item
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "ITEM_ORDCOMP")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrdemCompra {

    /**
     * Identificador único do item da ordem de compra.
     * Chave primária auto-incrementada.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDITEMORD")
    private Integer idItemOrd;

    /**
     * ID da ordem de compra relacionada.
     * Campo obrigatório que estabelece a relação com a ordem de compra.
     */
    @NotNull(message = "ID da ordem de compra é obrigatório")
    @Column(name = "ID_ORDCOMP", nullable = false)
    private Integer idOrdComp;

    /**
     * ID do produto.
     * Campo obrigatório que identifica o produto sendo comprado.
     */
    @NotNull(message = "ID do produto é obrigatório")
    @Column(name = "ID_PRODUTO", nullable = false)
    private Integer idProduto;

    /**
     * Quantidade do produto.
     * Campo obrigatório que deve ser maior que zero.
     */
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Column(name = "QNTD", nullable = false)
    private Integer qntd;

    /**
     * Valor unitário do produto.
     * Campo obrigatório com precisão de 10 dígitos e 2 casas decimais.
     */
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Column(name = "VALOR", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    /**
     * Data de vencimento do item.
     * Campo obrigatório que define quando o item expira.
     */
    @NotNull(message = "Data de vencimento é obrigatória")
    @Future(message = "Data de vencimento deve ser futura")
    @Column(name = "DATAVENC", nullable = false)
    private LocalDate dataVenc;

    // Campo vlrTotal removido - não existe no banco
    // Valor total será calculado dinamicamente quando necessário

    // Métodos utilitários

    /**
     * Calcula o valor total do item (quantidade × valor unitário).
     * 
     * @return Valor total do item
     */
    public BigDecimal calcularValorTotal() {
        if (qntd != null && valor != null) {
            return valor.multiply(BigDecimal.valueOf(qntd));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Getter para valor total calculado dinamicamente.
     * Mantém compatibilidade com código existente.
     * 
     * @return Valor total do item
     */
    public BigDecimal getVlrTotal() {
        return calcularValorTotal();
    }

    /**
     * Verifica se o item está próximo do vencimento (30 dias).
     * 
     * @return true se estiver próximo do vencimento, false caso contrário
     */
    public boolean isProximoVencimento() {
        if (dataVenc != null) {
            return dataVenc.isBefore(LocalDate.now().plusDays(30));
        }
        return false;
    }

    /**
     * Verifica se o item está vencido.
     * 
     * @return true se estiver vencido, false caso contrário
     */
    public boolean isVencido() {
        if (dataVenc != null) {
            return dataVenc.isBefore(LocalDate.now());
        }
        return false;
    }

    /**
     * Calcula os dias restantes até o vencimento.
     * 
     * @return Número de dias até o vencimento (negativo se vencido)
     */
    public long diasParaVencimento() {
        if (dataVenc != null) {
            return LocalDate.now().until(dataVenc).getDays();
        }
        return 0;
    }

    /**
     * Método setter customizado para quantidade que recalcula o valor total
     * automaticamente.
     * 
     * @param qntd Nova quantidade
     */
    public void setQntd(Integer qntd) {
        this.qntd = qntd;
        calcularValorTotal();
    }

    /**
     * Método setter customizado para valor que recalcula o valor total
     * automaticamente.
     * 
     * @param valor Novo valor unitário
     */
    public void setValor(BigDecimal valor) {
        this.valor = valor;
        calcularValorTotal();
    }

    /**
     * Inicializa o valor total baseado na quantidade e valor atual.
     * Método mantido para compatibilidade - valor é calculado dinamicamente.
     */
    public void inicializarValorTotal() {
        // Método mantido para compatibilidade
        // O valor total é calculado dinamicamente via getVlrTotal()
    }

}