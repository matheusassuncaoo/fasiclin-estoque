package com.br.fasipe.estoque.ordemcompra.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.fasipe.estoque.ordemcompra.models.MovContabil;
import com.br.fasipe.estoque.ordemcompra.services.MovContabilService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Controller REST para gerenciamento de movimentações contábeis.
 * 
 * Fornece endpoints para operações CRUD e consultas específicas
 * relacionadas às movimentações contábeis.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/movimentacoes-contabeis")
@Tag(name = "Movimentações Contábeis", description = "Operações relacionadas às movimentações contábeis")
public class MovContabilController {

    @Autowired
    private MovContabilService movContabilService;

    /**
     * Busca uma movimentação contábil por ID.
     * 
     * @param id ID da movimentação
     * @return ResponseEntity com a movimentação encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar movimentação por ID", description = "Retorna uma movimentação contábil específica pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimentação encontrada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content)
    })
    public ResponseEntity<MovContabil> findById(
            @Parameter(description = "ID da movimentação contábil", required = true) @PathVariable @NotNull Integer id) {
        try {
            MovContabil movContabil = movContabilService.findById(id);
            return ResponseEntity.ok(movContabil);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista todas as movimentações contábeis.
     * 
     * @return ResponseEntity com lista de movimentações
     */
    @GetMapping
    @Operation(summary = "Listar todas as movimentações", description = "Retorna uma lista com todas as movimentações contábeis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class)))
    })
    public ResponseEntity<List<MovContabil>> findAll() {
        List<MovContabil> movimentacoes = movContabilService.findAll();
        return ResponseEntity.ok(movimentacoes);
    }

    /**
     * Cria uma nova movimentação contábil.
     * 
     * @param movContabil Dados da movimentação a ser criada
     * @return ResponseEntity com a movimentação criada
     */
    @PostMapping
    @Operation(summary = "Criar nova movimentação", description = "Cria uma nova movimentação contábil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movimentação criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content)
    })
    public ResponseEntity<MovContabil> create(
            @Parameter(description = "Dados da movimentação contábil", required = true) @Valid @RequestBody MovContabil movContabil) {
        try {
            MovContabil novaMovimentacao = movContabilService.create(movContabil);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaMovimentacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Atualiza uma movimentação contábil existente.
     * 
     * @param movContabil Dados atualizados da movimentação
     * @return ResponseEntity com a movimentação atualizada
     */
    @PutMapping
    @Operation(summary = "Atualizar movimentação", description = "Atualiza uma movimentação contábil existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Movimentação atualizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content)
    })
    public ResponseEntity<MovContabil> update(
            @Parameter(description = "Dados atualizados da movimentação", required = true) @Valid @RequestBody MovContabil movContabil) {
        try {
            MovContabil movimentacaoAtualizada = movContabilService.update(movContabil);
            return ResponseEntity.ok(movimentacaoAtualizada);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Remove uma movimentação contábil por ID.
     * 
     * @param id ID da movimentação a ser removida
     * @return ResponseEntity indicando o resultado da operação
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover movimentação", description = "Remove uma movimentação contábil pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Movimentação removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "409", description = "Conflito - movimentação não pode ser removida")
    })
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID da movimentação a ser removida", required = true) @PathVariable @NotNull Integer id) {
        try {
            movContabilService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Busca movimentações por número do lançamento.
     * 
     * @param numeroLancamento Número do lançamento
     * @return ResponseEntity com lista de movimentações
     */
    @GetMapping("/lancamento/{numeroLancamento}")
    @Operation(summary = "Buscar por número do lançamento", description = "Retorna movimentações com número de lançamento específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "Número inválido", content = @Content)
    })
    public ResponseEntity<List<MovContabil>> findByNumeroLancamento(
            @Parameter(description = "Número do lançamento", required = true) @PathVariable @NotNull Integer numeroLancamento) {
        try {
            List<MovContabil> movimentacoes = movContabilService.findByNumeroLancamento(numeroLancamento);
            return ResponseEntity.ok(movimentacoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca movimentações por ID do plano de contas.
     * 
     * @param idPlanoConta ID do plano de contas
     * @return ResponseEntity com lista de movimentações
     */
    @GetMapping("/plano-conta/{idPlanoConta}")
    @Operation(summary = "Buscar por plano de contas", description = "Retorna movimentações de um plano de contas específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content)
    })
    public ResponseEntity<List<MovContabil>> findByIdPlanoConta(
            @Parameter(description = "ID do plano de contas", required = true) @PathVariable @NotNull Integer idPlanoConta) {
        try {
            List<MovContabil> movimentacoes = movContabilService.findByIdPlanoConta(idPlanoConta);
            return ResponseEntity.ok(movimentacoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca movimentações por ID da ordem de compra.
     * 
     * @param idOrdComp ID da ordem de compra
     * @return ResponseEntity com lista de movimentações
     */
    @GetMapping("/ordem-compra/{idOrdComp}")
    @Operation(summary = "Buscar por ordem de compra", description = "Retorna movimentações de uma ordem de compra específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content)
    })
    public ResponseEntity<List<MovContabil>> findByIdOrdComp(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull Integer idOrdComp) {
        try {
            List<MovContabil> movimentacoes = movContabilService.findByIdOrdComp(idOrdComp);
            return ResponseEntity.ok(movimentacoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca movimentações por data de lançamento.
     * 
     * @param dataLancamento Data do lançamento
     * @return ResponseEntity com lista de movimentações
     */
    @GetMapping("/data-lancamento/{dataLancamento}")
    @Operation(summary = "Buscar por data de lançamento", description = "Retorna movimentações de uma data específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "Data inválida", content = @Content)
    })
    public ResponseEntity<List<MovContabil>> findByDataLancamento(
            @Parameter(description = "Data do lançamento (formato: yyyy-MM-dd)", required = true) @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataLancamento) {
        try {
            List<MovContabil> movimentacoes = movContabilService.findByDataLancamento(dataLancamento);
            return ResponseEntity.ok(movimentacoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca movimentações por tipo (débito/crédito).
     * 
     * @param tipo Tipo da movimentação (D para débito, C para crédito)
     * @return ResponseEntity com lista de movimentações
     */
    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Buscar por tipo de movimentação", description = "Retorna movimentações de um tipo específico (D=Débito, C=Crédito)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "Tipo inválido", content = @Content)
    })
    public ResponseEntity<List<MovContabil>> findByTipo(
            @Parameter(description = "Tipo da movimentação (D ou C)", required = true) @PathVariable @NotNull String tipo) {
        try {
            List<MovContabil> movimentacoes = movContabilService.findByTipo(tipo);
            return ResponseEntity.ok(movimentacoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca movimentações por faixa de datas.
     * 
     * @param dataInicio Data inicial
     * @param dataFim    Data final
     * @return ResponseEntity com lista de movimentações
     */
    @GetMapping("/periodo")
    @Operation(summary = "Buscar por período", description = "Retorna movimentações dentro do período especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content)
    })
    public ResponseEntity<List<MovContabil>> findByDataLancamentoBetween(
            @Parameter(description = "Data inicial (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        try {
            List<MovContabil> movimentacoes = movContabilService.findByDataLancamentoBetween(dataInicio, dataFim);
            return ResponseEntity.ok(movimentacoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca movimentações por faixa de valores.
     * 
     * @param valorMinimo Valor mínimo
     * @param valorMaximo Valor máximo
     * @return ResponseEntity com lista de movimentações
     */
    @GetMapping("/faixa-valor")
    @Operation(summary = "Buscar por faixa de valor", description = "Retorna movimentações com valor dentro da faixa especificada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content)
    })
    public ResponseEntity<List<MovContabil>> findByValorBetween(
            @Parameter(description = "Valor mínimo", required = true) @RequestParam @NotNull BigDecimal valorMinimo,
            @Parameter(description = "Valor máximo", required = true) @RequestParam @NotNull BigDecimal valorMaximo) {
        try {
            List<MovContabil> movimentacoes = movContabilService.findByValorBetween(valorMinimo, valorMaximo);
            return ResponseEntity.ok(movimentacoes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Conta movimentações por ordem de compra.
     * 
     * @param idOrdComp ID da ordem de compra
     * @return ResponseEntity com a contagem
     */
    @GetMapping("/count/ordem-compra/{idOrdComp}")
    @Operation(summary = "Contar movimentações por ordem de compra", description = "Retorna a quantidade de movimentações de uma ordem de compra")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    public ResponseEntity<Long> countByIdOrdComp(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull Integer idOrdComp) {
        try {
            Long count = movContabilService.countByIdOrdComp(idOrdComp);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calcula saldo por plano de contas.
     * 
     * @param idPlanoConta ID do plano de contas
     * @return ResponseEntity com o saldo
     */
    @GetMapping("/saldo/plano-conta/{idPlanoConta}")
    @Operation(summary = "Calcular saldo por plano de contas", description = "Retorna o saldo (débitos - créditos) de um plano de contas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo calculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    public ResponseEntity<BigDecimal> calcularSaldoPorPlanoConta(
            @Parameter(description = "ID do plano de contas", required = true) @PathVariable @NotNull Integer idPlanoConta) {
        try {
            BigDecimal saldo = movContabilService.calcularSaldoPorPlanoConta(idPlanoConta);
            return ResponseEntity.ok(saldo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calcula saldo por plano de contas e período.
     * 
     * @param idPlanoConta ID do plano de contas
     * @param dataInicio   Data inicial
     * @param dataFim      Data final
     * @return ResponseEntity com o saldo
     */
    @GetMapping("/saldo/plano-conta/{idPlanoConta}/periodo")
    @Operation(summary = "Calcular saldo por plano de contas e período", description = "Retorna o saldo de um plano de contas dentro do período especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo calculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    public ResponseEntity<BigDecimal> calcularSaldoPorPlanoContaEPeriodo(
            @Parameter(description = "ID do plano de contas", required = true) @PathVariable @NotNull Integer idPlanoConta,
            @Parameter(description = "Data inicial (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        try {
            BigDecimal saldo = movContabilService.calcularSaldoPorPlanoContaEPeriodo(idPlanoConta, dataInicio, dataFim);
            return ResponseEntity.ok(saldo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gera relatório de movimentações por período.
     * 
     * @param dataInicio Data inicial
     * @param dataFim    Data final
     * @return ResponseEntity com o relatório
     */
    @GetMapping("/relatorio/periodo")
    @Operation(summary = "Gerar relatório por período", description = "Gera relatório de movimentações contábeis por período")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovContabil.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content)
    })
    public ResponseEntity<List<MovContabil>> gerarRelatorioPorPeriodo(
            @Parameter(description = "Data inicial (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        try {
            List<MovContabil> relatorio = movContabilService.gerarRelatorioPorPeriodo(dataInicio, dataFim);
            return ResponseEntity.ok(relatorio);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}