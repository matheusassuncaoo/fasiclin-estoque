package com.br.fasipe.estoque.ordemcompra.controllers;

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

import com.br.fasipe.estoque.ordemcompra.models.Lote;
import com.br.fasipe.estoque.ordemcompra.services.LoteService;

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
 * Controller REST para gerenciamento de lotes.
 * 
 * Fornece endpoints para operações CRUD e consultas específicas
 * relacionadas aos lotes de produtos.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Operações relacionadas aos lotes de produtos")
public class LoteController {

    @Autowired
    private LoteService loteService;

    /**
     * Busca um lote por ID.
     * 
     * @param id ID do lote
     * @return ResponseEntity com o lote encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar lote por ID", description = "Retorna um lote específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lote encontrado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class))),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content)
    })
    public ResponseEntity<Lote> findById(
            @Parameter(description = "ID do lote", required = true) @PathVariable @NotNull Integer id) {
        try {
            Lote lote = loteService.findById(id);
            return ResponseEntity.ok(lote);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista todos os lotes.
     * 
     * @return ResponseEntity com lista de lotes
     */
    @GetMapping
    @Operation(summary = "Listar todos os lotes", description = "Retorna uma lista com todos os lotes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class)))
    })
    public ResponseEntity<List<Lote>> findAll() {
        List<Lote> lotes = loteService.findAll();
        return ResponseEntity.ok(lotes);
    }

    /**
     * Cria um novo lote.
     * 
     * @param lote Dados do lote a ser criado
     * @return ResponseEntity com o lote criado
     */
    @PostMapping
    @Operation(summary = "Criar novo lote", description = "Cria um novo lote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lote criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content)
    })
    public ResponseEntity<Lote> create(
            @Parameter(description = "Dados do lote", required = true) @Valid @RequestBody Lote lote) {
        try {
            Lote novoLote = loteService.create(lote);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoLote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Atualiza um lote existente.
     * 
     * @param lote Dados atualizados do lote
     * @return ResponseEntity com o lote atualizado
     */
    @PutMapping
    @Operation(summary = "Atualizar lote", description = "Atualiza um lote existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lote atualizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content)
    })
    public ResponseEntity<Lote> update(
            @Parameter(description = "Dados atualizados do lote", required = true) @Valid @RequestBody Lote lote) {
        try {
            Lote loteAtualizado = loteService.update(lote);
            return ResponseEntity.ok(loteAtualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Remove um lote por ID.
     * 
     * @param id ID do lote a ser removido
     * @return ResponseEntity indicando o resultado da operação
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover lote", description = "Remove um lote pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Lote removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "409", description = "Conflito - lote não pode ser removido")
    })
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID do lote a ser removido", required = true) @PathVariable @NotNull Integer id) {
        try {
            loteService.deleteById(id);
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
     * Busca lotes por data de validade.
     * 
     * @param dataValidade Data de validade
     * @return ResponseEntity com lista de lotes
     */
    @GetMapping("/validade/{dataValidade}")
    @Operation(summary = "Buscar lotes por data de validade", description = "Retorna lotes com data de validade específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class))),
            @ApiResponse(responseCode = "400", description = "Data inválida", content = @Content)
    })
    public ResponseEntity<List<Lote>> findByDataValidade(
            @Parameter(description = "Data de validade (formato: yyyy-MM-dd)", required = true) @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataValidade) {
        try {
            List<Lote> lotes = loteService.findByDataValidade(dataValidade);
            return ResponseEntity.ok(lotes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca lotes vencidos.
     * 
     * @return ResponseEntity com lista de lotes vencidos
     */
    @GetMapping("/vencidos")
    @Operation(summary = "Buscar lotes vencidos", description = "Retorna todos os lotes com data de validade anterior à data atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class)))
    })
    public ResponseEntity<List<Lote>> findLotesVencidos() {
        List<Lote> lotes = loteService.findLotesVencidos();
        return ResponseEntity.ok(lotes);
    }

    /**
     * Busca lotes próximos ao vencimento.
     * 
     * @return ResponseEntity com lista de lotes próximos ao vencimento
     */
    @GetMapping("/proximos-vencimento")
    @Operation(summary = "Buscar lotes próximos ao vencimento", description = "Retorna lotes que vencem nos próximos 30 dias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class)))
    })
    public ResponseEntity<List<Lote>> findLotesProximosVencimento() {
        List<Lote> lotes = loteService.findLotesProximosVencimento();
        return ResponseEntity.ok(lotes);
    }

    /**
     * Busca lotes válidos (não vencidos).
     * 
     * @return ResponseEntity com lista de lotes válidos
     */
    @GetMapping("/validos")
    @Operation(summary = "Buscar lotes válidos", description = "Retorna todos os lotes com data de validade posterior à data atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class)))
    })
    public ResponseEntity<List<Lote>> findLotesValidos() {
        List<Lote> lotes = loteService.findLotesValidos();
        return ResponseEntity.ok(lotes);
    }

    /**
     * Busca lotes por faixa de datas de validade.
     * 
     * @param dataInicio Data inicial
     * @param dataFim    Data final
     * @return ResponseEntity com lista de lotes
     */
    @GetMapping("/validade-periodo")
    @Operation(summary = "Buscar lotes por período de validade", description = "Retorna lotes com validade dentro do período especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lote.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content)
    })
    public ResponseEntity<List<Lote>> findByDataValidadeBetween(
            @Parameter(description = "Data inicial (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        try {
            List<Lote> lotes = loteService.findByDataValidadeBetween(dataInicio, dataFim);
            return ResponseEntity.ok(lotes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verifica se um lote está vencido.
     * 
     * @param lote Lote a ser verificado
     * @return ResponseEntity com o resultado da verificação
     */
    @PostMapping("/verificar-vencimento")
    @Operation(summary = "Verificar vencimento do lote", description = "Verifica se um lote está vencido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<Boolean> isLoteVencido(
            @Parameter(description = "Lote a ser verificado", required = true) @Valid @RequestBody Lote lote) {
        try {
            Boolean vencido = loteService.isLoteVencido(lote);
            return ResponseEntity.ok(vencido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verifica se um lote está próximo ao vencimento.
     * 
     * @param lote Lote a ser verificado
     * @return ResponseEntity com o resultado da verificação
     */
    @PostMapping("/verificar-proximo-vencimento")
    @Operation(summary = "Verificar proximidade do vencimento", description = "Verifica se um lote está próximo ao vencimento (30 dias)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<Boolean> isLoteProximoVencimento(
            @Parameter(description = "Lote a ser verificado", required = true) @Valid @RequestBody Lote lote) {
        try {
            Boolean proximoVencimento = loteService.isLoteProximoVencimento(lote);
            return ResponseEntity.ok(proximoVencimento);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}