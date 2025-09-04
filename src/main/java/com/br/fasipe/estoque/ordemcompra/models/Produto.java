package com.br.fasipe.estoque.ordemcompra.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidade que representa um produto no sistema.
 * 
 * Esta classe mapeia a tabela PRODUTO do banco de dados e contém
 * informações completas sobre produtos, incluindo dados básicos,
 * controle de estoque e configurações de armazenamento.
 * 
 * Campos principais:
 * - idProduto: Identificador único do produto
 * - nome: Nome do produto
 * - descricao: Descrição detalhada
 * - idAlmox: ID do almoxarifado (opcional)
 * - idUnMedi: ID da unidade de medida
 * - codBarras: Código de barras (opcional)
 * - tempIdeal: Temperatura ideal de armazenamento (opcional)
 * - stqMax: Estoque máximo
 * - stqMin: Estoque mínimo
 * - pntPedido: Ponto de pedido
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "PRODUTO")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Produto {

    /**
     * Identificador único do produto.
     * Chave primária auto-incrementada.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDPRODUTO")
    private Integer idProduto;

    /**
     * Nome do produto.
     * Campo obrigatório com até 50 caracteres.
     */
    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    @Column(name = "NOME", nullable = false, length = 50)
    private String nome;

    /**
     * Descrição detalhada do produto.
     * Campo obrigatório com até 250 caracteres.
     */
    @NotBlank(message = "Descrição do produto é obrigatória")
    @Size(max = 250, message = "Descrição deve ter no máximo 250 caracteres")
    @Column(name = "DESCRICAO", nullable = false, length = 250)
    private String descricao;

    /**
     * ID do almoxarifado onde o produto é armazenado.
     * Campo opcional.
     */
    @Column(name = "ID_ALMOX")
    private Integer idAlmox;

    /**
     * ID da unidade de medida do produto.
     * Campo obrigatório que define como o produto é medido/contado.
     */
    @NotNull(message = "ID da unidade de medida é obrigatório")
    @Column(name = "ID_UNMEDI", nullable = false)
    private Integer idUnMedi;

    /**
     * Código de barras do produto.
     * Campo opcional com até 250 caracteres.
     */
    @Size(max = 250, message = "Código de barras deve ter no máximo 250 caracteres")
    @Column(name = "CODBARRAS", length = 250)
    private String codBarras;

    /**
     * Temperatura ideal de armazenamento em graus Celsius.
     * Campo opcional com precisão de 3 dígitos e 1 casa decimal.
     */
    @DecimalMax(value = "99.9", message = "Temperatura ideal deve ser no máximo 99.9°C")
    @DecimalMin(value = "-99.9", message = "Temperatura ideal deve ser no mínimo -99.9°C")
    @Digits(integer = 2, fraction = 1, message = "Temperatura deve ter no máximo 2 dígitos inteiros e 1 decimal")
    @Column(name = "TEMPIDEAL", precision = 3, scale = 1)
    private BigDecimal tempIdeal;

    /**
     * Estoque máximo permitido.
     * Campo obrigatório que define o limite superior de estoque.
     */
    @NotNull(message = "Estoque máximo é obrigatório")
    @Min(value = 1, message = "Estoque máximo deve ser maior que zero")
    @Column(name = "STQMAX", nullable = false)
    private Integer stqMax;

    /**
     * Estoque mínimo permitido.
     * Campo obrigatório que define o limite inferior de estoque.
     */
    @NotNull(message = "Estoque mínimo é obrigatório")
    @Min(value = 0, message = "Estoque mínimo deve ser maior ou igual a zero")
    @Column(name = "STQMIN", nullable = false)
    private Integer stqMin;

    /**
     * Ponto de pedido.
     * Campo obrigatório que define quando fazer novo pedido.
     */
    @NotNull(message = "Ponto de pedido é obrigatório")
    @Min(value = 0, message = "Ponto de pedido deve ser maior ou igual a zero")
    @Column(name = "PNTPEDIDO", nullable = false)
    private Integer pntPedido;



    // Métodos utilitários

    /**
     * Verifica se o produto tem código de barras.
     * 
     * @return true se tiver código de barras, false caso contrário
     */
    public boolean temCodigoBarras() {
        return codBarras != null && !codBarras.trim().isEmpty();
    }

    /**
     * Verifica se o produto tem temperatura ideal definida.
     * 
     * @return true se tiver temperatura ideal, false caso contrário
     */
    public boolean temTemperaturaIdeal() {
        return tempIdeal != null;
    }

    /**
     * Verifica se o produto está associado a um almoxarifado.
     * 
     * @return true se estiver associado, false caso contrário
     */
    public boolean temAlmoxarifado() {
        return idAlmox != null;
    }

    /**
     * Calcula a faixa de estoque ideal (entre mínimo e máximo).
     * 
     * @return Diferença entre estoque máximo e mínimo
     */
    public Integer calcularFaixaEstoque() {
        if (stqMax != null && stqMin != null) {
            return stqMax - stqMin;
        }
        return 0;
    }

    /**
     * Verifica se uma quantidade está dentro dos limites de estoque.
     * 
     * @param quantidade Quantidade a ser verificada
     * @return true se estiver dentro dos limites, false caso contrário
     */
    public boolean quantidadeDentroLimites(Integer quantidade) {
        if (quantidade == null) {
            return false;
        }
        return quantidade >= stqMin && quantidade <= stqMax;
    }

    /**
     * Verifica se uma quantidade está abaixo do ponto de pedido.
     * 
     * @param quantidade Quantidade atual em estoque
     * @return true se estiver abaixo do ponto de pedido, false caso contrário
     */
    public boolean precisaReposicao(Integer quantidade) {
        if (quantidade == null || pntPedido == null) {
            return false;
        }
        return quantidade <= pntPedido;
    }

    /**
     * Obtém o status do estoque baseado na quantidade atual.
     * 
     * @param quantidadeAtual Quantidade atual em estoque
     * @return Status do estoque
     */
    public StatusEstoque getStatusEstoque(Integer quantidadeAtual) {
        if (quantidadeAtual == null) {
            return StatusEstoque.INDEFINIDO;
        }
        
        if (quantidadeAtual == 0) {
            return StatusEstoque.ZERADO;
        } else if (quantidadeAtual <= pntPedido) {
            return StatusEstoque.CRITICO;
        } else if (quantidadeAtual <= stqMin) {
            return StatusEstoque.BAIXO;
        } else if (quantidadeAtual >= stqMax) {
            return StatusEstoque.EXCESSO;
        } else {
            return StatusEstoque.NORMAL;
        }
    }

    /**
     * Enum para representar o status do estoque.
     */
    public enum StatusEstoque {
        ZERADO("Estoque Zerado"),
        CRITICO("Estoque Crítico"),
        BAIXO("Estoque Baixo"),
        NORMAL("Estoque Normal"),
        EXCESSO("Estoque em Excesso"),
        INDEFINIDO("Status Indefinido");

        private final String descricao;

        StatusEstoque(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }


}