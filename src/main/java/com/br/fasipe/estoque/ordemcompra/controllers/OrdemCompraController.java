package com.br.fasipe.estoque.ordemcompra.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra;
import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra.StatusOrdemCompra;
import com.br.fasipe.estoque.ordemcompra.services.OrdemCompraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Controller REST para gerenciamento de Ordens de Compra.
 * 
 * Fornece endpoints para operações CRUD e consultas especializadas
 * relacionadas às ordens de compra do sistema de estoque.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/ordens-compra")
@Validated
@Tag(name = "Ordem de Compra", description = "API para gerenciamento de ordens de compra")
public class OrdemCompraController {

    @Autowired
    private OrdemCompraService ordemCompraService;

    /**
     * Busca uma ordem de compra por ID.
     * 
     * @param id ID da ordem de compra
     * @return ResponseEntity com a ordem de compra encontrada
     * @throws EntityNotFoundException se a ordem não for encontrada
     */
    @Operation(summary = "Buscar ordem de compra por ID", 
               description = "Retorna uma ordem de compra específica baseada no ID fornecido")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordem de compra encontrada",
                    content = @Content(schema = @Schema(implementation = OrdemCompra.class))),
        @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada"),
        @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrdemCompra> findById(
            @Parameter(description = "ID da ordem de compra", required = true)
            @PathVariable @NotNull @Min(1) Integer id) {
        OrdemCompra ordemCompra = ordemCompraService.findById(id);
        return ResponseEntity.ok(ordemCompra);
    }

    /**
     * Lista todas as ordens de compra com paginação.
     * 
     * @param pageable configurações de paginação
     * @return ResponseEntity com página de ordens de compra
     */
    @Operation(summary = "Listar todas as ordens de compra", 
               description = "Retorna uma lista paginada de todas as ordens de compra")
    @ApiResponse(responseCode = "200", description = "Lista de ordens de compra retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<OrdemCompra>> findAll() {
        List<OrdemCompra> ordens = ordemCompraService.findAll();
        return ResponseEntity.ok(ordens);
    }

    /**
     * Cria uma nova ordem de compra.
     * 
     * @param ordemCompra dados da ordem de compra a ser criada
     * @return ResponseEntity com a ordem de compra criada
     */
    @Operation(summary = "Criar nova ordem de compra", 
               description = "Cria uma nova ordem de compra no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ordem de compra criada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemCompra.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
    })
    @PostMapping
    public ResponseEntity<OrdemCompra> create(
            @Parameter(description = "Dados da ordem de compra", required = true)
            @Valid @RequestBody OrdemCompra ordemCompra) {
        OrdemCompra novaOrdem = ordemCompraService.create(ordemCompra);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaOrdem);
    }

    /**
     * Atualiza uma ordem de compra existente.
     * 
     * @param id ID da ordem de compra a ser atualizada
     * @param ordemCompra novos dados da ordem de compra
     * @return ResponseEntity com a ordem de compra atualizada
     */
    @Operation(summary = "Atualizar ordem de compra", 
               description = "Atualiza os dados de uma ordem de compra existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ordem de compra atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrdemCompra.class))),
        @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrdemCompra> update(
            @Parameter(description = "ID da ordem de compra", required = true)
            @PathVariable @NotNull @Min(1) Integer id,
            @Parameter(description = "Novos dados da ordem de compra", required = true)
            @Valid @RequestBody OrdemCompra ordemCompra) {
        ordemCompra.setId(id);
        OrdemCompra ordemAtualizada = ordemCompraService.update(ordemCompra);
        return ResponseEntity.ok(ordemAtualizada);
    }

    /**
     * Remove uma ordem de compra.
     * 
     * @param id ID da ordem de compra a ser removida
     * @return ResponseEntity vazio
     */
    @Operation(summary = "Remover ordem de compra", 
               description = "Remove uma ordem de compra do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ordem de compra removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada"),
        @ApiResponse(responseCode = "422", description = "Não é possível remover ordem com este status")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da ordem de compra", required = true)
            @PathVariable @NotNull @Min(1) Integer id) {
        ordemCompraService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca ordens de compra por status.
     * 
     * @param status status da ordem de compra
     * @return ResponseEntity com lista de ordens de compra
     */
    @Operation(summary = "Buscar ordens por status", 
               description = "Retorna ordens de compra filtradas por status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrdemCompra>> findByStatus(
            @Parameter(description = "Status da ordem de compra", required = true)
            @PathVariable StatusOrdemCompra status) {
        List<OrdemCompra> ordens = ordemCompraService.findByStatus(status);
        return ResponseEntity.ok(ordens);
    }

    /**
     * Busca ordens de compra por faixa de valor.
     * 
     * @param valorMin valor mínimo
     * @param valorMax valor máximo
     * @return ResponseEntity com lista de ordens de compra
     */
    @Operation(summary = "Buscar ordens por faixa de valor", 
               description = "Retorna ordens de compra dentro de uma faixa de valores")
    @GetMapping("/valor")
    public ResponseEntity<List<OrdemCompra>> findByValorBetween(
            @Parameter(description = "Valor mínimo", required = true)
            @RequestParam @NotNull BigDecimal valorMin,
            @Parameter(description = "Valor máximo", required = true)
            @RequestParam @NotNull BigDecimal valorMax) {
        List<OrdemCompra> ordens = ordemCompraService.findByValorBetween(valorMin, valorMax);
        return ResponseEntity.ok(ordens);
    }

    /**
     * Busca ordens de compra por data prevista.
     * 
     * @param dataPrevista data prevista de entrega
     * @return ResponseEntity com lista de ordens de compra
     */
    @Operation(summary = "Buscar ordens por data prevista", 
               description = "Retorna ordens de compra com data prevista específica")
    @GetMapping("/data-prevista")
    public ResponseEntity<List<OrdemCompra>> findByDataPrevista(
            @Parameter(description = "Data prevista de entrega", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPrevista) {
        List<OrdemCompra> ordens = ordemCompraService.findByDataPrevista(dataPrevista);
        return ResponseEntity.ok(ordens);
    }

    /**
     * Busca ordens de compra por período de criação.
     * 
     * @param dataInicio data de início do período
     * @param dataFim data de fim do período
     * @return ResponseEntity com lista de ordens de compra
     */
    @Operation(summary = "Buscar ordens por período de criação", 
               description = "Retorna ordens de compra criadas em um período específico")
    @GetMapping("/periodo")
    public ResponseEntity<List<OrdemCompra>> findByPeriodoCriacao(
            @Parameter(description = "Data inicial do período (formato: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final do período (formato: yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<OrdemCompra> ordens = ordemCompraService.findByPeriodoCriacao(dataInicio, dataFim);
        return ResponseEntity.ok(ordens);
    }

    /**
     * Busca ordens de compra em atraso.
     * 
     * @return ResponseEntity com lista de ordens de compra em atraso
     */
    @Operation(summary = "Buscar ordens em atraso", 
               description = "Retorna ordens de compra com entrega em atraso")
    @GetMapping("/atraso")
    public ResponseEntity<List<OrdemCompra>> findOrdensEmAtraso() {
        List<OrdemCompra> ordens = ordemCompraService.findOrdensEmAtraso();
        return ResponseEntity.ok(ordens);
    }

    /**
     * Conta ordens de compra por status.
     * 
     * @param status status da ordem de compra
     * @return ResponseEntity com a contagem
     */
    @Operation(summary = "Contar ordens por status", 
               description = "Retorna a quantidade de ordens de compra por status")
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countByStatus(
            @Parameter(description = "Status da ordem de compra", required = true)
            @PathVariable StatusOrdemCompra status) {
        Long count = ordemCompraService.countByStatus(status);
        return ResponseEntity.ok(count);
    }
}
