package com.br.fasipe.estoque.ordemcompra.models;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ORDEMCOMPRA")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrdemCompra {

    public static final String TABLE_NAME = "ORDEMCOMPRA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDORDCOMP")
    private Integer id;

    @NotNull(message = "Status da ordem de compra é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUSORD", nullable = false)
    private StatusOrdemCompra statusOrdemCompra;

    @Column(name = "VALOR", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Data prevista é obrigatória")
    @Column(name = "DATAPREV", nullable = false)
    private LocalDate dataPrev;

    @NotNull(message = "Data da ordem é obrigatória")
    @Column(name = "DATAORDEM", nullable = false)
    private LocalDate dataOrdem;

    @Column(name = "DATAENTRE", nullable = false)
    private LocalDate dataEntre;
    
    /**
     * Método executado antes de persistir a entidade pela primeira vez.
     * Define valores padrão para campos que não são obrigatórios no frontend
     * mas são NOT NULL no banco de dados.
     */
    @PrePersist
    public void prePersist() {
        // Só aplica defaults se for criação (id == null)
        if (this.id == null) {
            // Se valor não foi definido, usar 0.00 como padrão
            if (this.valor == null) {
                this.valor = BigDecimal.ZERO;
            }
            
            // Se dataEntre não foi definida, usar a data prevista como padrão
            if (this.dataEntre == null) {
                this.dataEntre = this.dataPrev;
            }
        }
    }

    public enum StatusOrdemCompra {
        PEND,
        PROC,
        CONC,
        CANC
    }
}
