package com.br.fasipe.estoque.ordemcompra.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.FutureOrPresent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ORDEMCOMPRA")
@AllArgsConstructor
@NoArgsConstructor
@Data
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

    @NotNull(message = "Valor da ordem de compra é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    @Column(name = "VALOR", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Data prevista é obrigatória")
    @FutureOrPresent(message = "Data prevista deve ser hoje ou no futuro")
    @Column(name = "DATAPREV", nullable = false)
    private LocalDate dataPrev;

    @NotNull(message = "Data da ordem é obrigatória")
    @PastOrPresent(message = "Data da ordem deve ser hoje ou no passado")
    @Column(name = "DATAORDEM", nullable = false)
    private LocalDate dataOrdem;

    @NotNull(message = "Data de entrega é obrigatória")
    @Column(name = "DATAENTRE", nullable = false)
    private LocalDate dataEntre;

    public enum StatusOrdemCompra {
        PEND,
        ANDA,
        CONC
    }
}
