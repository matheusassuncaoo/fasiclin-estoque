package com.br.fasipe.estoque.ordemcompra.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidade que representa um lote de produtos no sistema.
 * 
 * Esta classe mapeia a tabela LOTE do banco de dados e contém
 * informações sobre lotes de produtos recebidos através de
 * ordens de compra, incluindo data de vencimento e quantidade.
 * 
 * Campos principais:
 * - idLote: Identificador único do lote
 * - idOrdComp: ID da ordem de compra relacionada
 * - dataVenc: Data de vencimento do lote
 * - qntd: Quantidade de produtos no lote
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "LOTE")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Lote {

    /**
     * Identificador único do lote.
     * Chave primária auto-incrementada.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDLOTE")
    private Integer idLote;

    /**
     * ID da ordem de compra relacionada.
     * Campo obrigatório que estabelece a relação com a ordem de compra
     * que originou este lote.
     */
    @NotNull(message = "ID da ordem de compra é obrigatório")
    @Column(name = "ID_ORDCOMP", nullable = false)
    private Integer idOrdComp;

    /**
     * Data de vencimento do lote.
     * Campo obrigatório que define quando o lote expira.
     */
    @NotNull(message = "Data de vencimento é obrigatória")
    @Column(name = "DATAVENC", nullable = false)
    private LocalDate dataVenc;

    /**
     * Quantidade de produtos no lote.
     * Campo obrigatório que deve ser maior que zero.
     */
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    @Column(name = "QNTD", nullable = false)
    private Integer qntd;



    /**
     * Verifica se o lote está vencido.
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
     * Verifica se o lote está próximo do vencimento (30 dias).
     * 
     * @return true se estiver próximo do vencimento, false caso contrário
     */
    public boolean isProximoVencimento() {
        if (dataVenc != null) {
            return dataVenc.isBefore(LocalDate.now().plusDays(30)) && !isVencido();
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
     * Verifica se o lote tem quantidade disponível.
     * 
     * @return true se tiver quantidade maior que zero, false caso contrário
     */
    public boolean temQuantidadeDisponivel() {
        return qntd != null && qntd > 0;
    }

    /**
     * Reduz a quantidade do lote.
     * 
     * @param quantidade Quantidade a ser reduzida
     * @throws IllegalArgumentException se a quantidade for inválida
     */
    public void reduzirQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (qntd == null || qntd < quantidade) {
            throw new IllegalArgumentException("Quantidade insuficiente no lote");
        }
        this.qntd -= quantidade;
    }

    /**
     * Adiciona quantidade ao lote.
     * 
     * @param quantidade Quantidade a ser adicionada
     * @throws IllegalArgumentException se a quantidade for inválida
     */
    public void adicionarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (this.qntd == null) {
            this.qntd = quantidade;
        } else {
            this.qntd += quantidade;
        }
    }

    /**
     * Obtém o status do lote baseado na data de vencimento.
     * 
     * @return Status do lote (VENCIDO, PROXIMO_VENCIMENTO, VALIDO)
     */
    public StatusLote getStatus() {
        if (isVencido()) {
            return StatusLote.VENCIDO;
        } else if (isProximoVencimento()) {
            return StatusLote.PROXIMO_VENCIMENTO;
        } else {
            return StatusLote.VALIDO;
        }
    }

    /**
     * Enum para representar o status do lote.
     */
    public enum StatusLote {
        VENCIDO("Vencido"),
        PROXIMO_VENCIMENTO("Próximo do Vencimento"),
        VALIDO("Válido");

        private final String descricao;

        StatusLote(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }


}