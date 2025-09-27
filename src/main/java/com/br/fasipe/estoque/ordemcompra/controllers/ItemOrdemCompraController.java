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

import com.br.fasipe.estoque.ordemcompra.models.ItemOrdemCompra;
import com.br.fasipe.estoque.ordemcompra.services.ItemOrdemCompraService;

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
 * Controller REST para gerenciamento de itens de ordem de compra.
 * 
 * Fornece endpoints para operações CRUD e consultas específicas
 * relacionadas aos itens de ordem de compra.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/itens-ordem-compra")
@Tag(name = "Itens de Ordem de Compra", description = "Operações relacionadas aos itens de ordem de compra")
public class ItemOrdemCompraController {

    @Autowired
    private ItemOrdemCompraService itemOrdemCompraService;

    /**
     * Busca um item de ordem de compra por ID.
     * 
     * @param id ID do item
     * @return ResponseEntity com o item encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar item por ID", description = "Retorna um item de ordem de compra específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item encontrado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class))),
            @ApiResponse(responseCode = "404", description = "Item não encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content)
    })
    public ResponseEntity<ItemOrdemCompra> findById(
            @Parameter(description = "ID do item de ordem de compra", required = true) @PathVariable @NotNull Integer id) {
        try {
            ItemOrdemCompra item = itemOrdemCompraService.findById(id);
            return ResponseEntity.ok(item);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista todos os itens de ordem de compra.
     * 
     * @return ResponseEntity com lista de itens
     */
    @GetMapping
    @Operation(summary = "Listar todos os itens", description = "Retorna uma lista com todos os itens de ordem de compra")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class)))
    })
    public ResponseEntity<List<ItemOrdemCompra>> findAll() {
        List<ItemOrdemCompra> itens = itemOrdemCompraService.findAll();
        return ResponseEntity.ok(itens);
    }

    /**
     * Cria um novo item de ordem de compra.
     * 
     * @param item Dados do item a ser criado
     * @return ResponseEntity com o item criado
     */
    @PostMapping
    @Operation(summary = "Criar novo item", description = "Cria um novo item de ordem de compra")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content)
    })
    public ResponseEntity<ItemOrdemCompra> create(
            @Parameter(description = "Dados do item de ordem de compra", required = true) @Valid @RequestBody ItemOrdemCompra item) {
        try {
            ItemOrdemCompra novoItem = itemOrdemCompraService.create(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Atualiza um item de ordem de compra existente.
     * 
     * @param item Dados atualizados do item
     * @return ResponseEntity com o item atualizado
     */
    @PutMapping
    @Operation(summary = "Atualizar item", description = "Atualiza um item de ordem de compra existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de dados", content = @Content)
    })
    public ResponseEntity<ItemOrdemCompra> update(
            @Parameter(description = "Dados atualizados do item", required = true) @Valid @RequestBody ItemOrdemCompra item) {
        try {
            ItemOrdemCompra itemAtualizado = itemOrdemCompraService.update(item);
            return ResponseEntity.ok(itemAtualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Remove um item de ordem de compra por ID.
     * 
     * @param id ID do item a ser removido
     * @return ResponseEntity indicando o resultado da operação
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover item", description = "Remove um item de ordem de compra pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "409", description = "Conflito - item não pode ser removido")
    })
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID do item a ser removido", required = true) @PathVariable @NotNull Integer id) {
        try {
            itemOrdemCompraService.deleteById(id);
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
     * Busca itens por ID da ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return ResponseEntity com lista de itens
     */
    @GetMapping("/ordem-compra/{idOrdemCompra}")
    @Operation(summary = "Buscar itens por ordem de compra", description = "Retorna todos os itens de uma ordem de compra específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content)
    })
    public ResponseEntity<List<ItemOrdemCompra>> findByIdOrdemCompra(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull Integer idOrdemCompra) {
        try {
            List<ItemOrdemCompra> itens = itemOrdemCompraService.findByIdOrdemCompra(idOrdemCompra);
            return ResponseEntity.ok(itens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca itens por ID do produto.
     * 
     * @param idProduto ID do produto
     * @return ResponseEntity com lista de itens
     */
    @GetMapping("/produto/{idProduto}")
    @Operation(summary = "Buscar itens por produto", description = "Retorna todos os itens relacionados a um produto específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content)
    })
    public ResponseEntity<List<ItemOrdemCompra>> findByIdProduto(
            @Parameter(description = "ID do produto", required = true) @PathVariable @NotNull Integer idProduto) {
        try {
            List<ItemOrdemCompra> itens = itemOrdemCompraService.findByIdProduto(idProduto);
            return ResponseEntity.ok(itens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca itens por data de vencimento.
     * 
     * @param dataVencimento Data de vencimento
     * @return ResponseEntity com lista de itens
     */
    @GetMapping("/vencimento/{dataVencimento}")
    @Operation(summary = "Buscar itens por data de vencimento", description = "Retorna itens com data de vencimento específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class))),
            @ApiResponse(responseCode = "400", description = "Data inválida", content = @Content)
    })
    public ResponseEntity<List<ItemOrdemCompra>> findByDataVencimento(
            @Parameter(description = "Data de vencimento (formato: yyyy-MM-dd)", required = true) @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimento) {
        try {
            List<ItemOrdemCompra> itens = itemOrdemCompraService.findByDataVencimento(dataVencimento);
            return ResponseEntity.ok(itens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca itens vencidos.
     * 
     * @return ResponseEntity com lista de itens vencidos
     */
    @GetMapping("/vencidos")
    @Operation(summary = "Buscar itens vencidos", description = "Retorna todos os itens com data de vencimento anterior à data atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class)))
    })
    public ResponseEntity<List<ItemOrdemCompra>> findItensVencidos() {
        List<ItemOrdemCompra> itens = itemOrdemCompraService.findItensVencidos();
        return ResponseEntity.ok(itens);
    }

    /**
     * Busca itens próximos ao vencimento.
     * 
     * @return ResponseEntity com lista de itens próximos ao vencimento
     */
    @GetMapping("/proximos-vencimento")
    @Operation(summary = "Buscar itens próximos ao vencimento", description = "Retorna itens que vencem nos próximos 30 dias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class)))
    })
    public ResponseEntity<List<ItemOrdemCompra>> findItensProximosVencimento() {
        List<ItemOrdemCompra> itens = itemOrdemCompraService.findItensProximosVencimento();
        return ResponseEntity.ok(itens);
    }

    /**
     * Busca itens por faixa de valor unitário.
     * 
     * @param valorMinimo Valor mínimo
     * @param valorMaximo Valor máximo
     * @return ResponseEntity com lista de itens
     */
    @GetMapping("/valor-unitario")
    @Operation(summary = "Buscar itens por faixa de valor", description = "Retorna itens com valor unitário dentro da faixa especificada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content)
    })
    public ResponseEntity<List<ItemOrdemCompra>> findByValorUnitarioBetween(
            @Parameter(description = "Valor mínimo", required = true) @RequestParam @NotNull BigDecimal valorMinimo,
            @Parameter(description = "Valor máximo", required = true) @RequestParam @NotNull BigDecimal valorMaximo) {
        try {
            List<ItemOrdemCompra> itens = itemOrdemCompraService.findByValorUnitarioBetween(valorMinimo, valorMaximo);
            return ResponseEntity.ok(itens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Conta itens por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return ResponseEntity com a contagem
     */
    @GetMapping("/count/ordem-compra/{idOrdemCompra}")
    @Operation(summary = "Contar itens por ordem de compra", description = "Retorna a quantidade de itens de uma ordem de compra")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    public ResponseEntity<Long> countByIdOrdemCompra(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull Integer idOrdemCompra) {
        try {
            Long count = itemOrdemCompraService.countByIdOrdemCompra(idOrdemCompra);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calcula valor total por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return ResponseEntity com o valor total
     */
    @GetMapping("/valor-total/ordem-compra/{idOrdemCompra}")
    @Operation(summary = "Calcular valor total por ordem de compra", description = "Retorna o valor total dos itens de uma ordem de compra")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valor total calculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    public ResponseEntity<BigDecimal> sumValorTotalByIdOrdemCompra(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull Integer idOrdemCompra) {
        try {
            BigDecimal valorTotal = itemOrdemCompraService.sumValorTotalByIdOrdemCompra(idOrdemCompra);
            return ResponseEntity.ok(valorTotal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Soma quantidade por ordem de compra.
     * 
     * @param idOrdemCompra ID da ordem de compra
     * @return ResponseEntity com a quantidade total
     */
    @GetMapping("/quantidade-total/ordem-compra/{idOrdemCompra}")
    @Operation(summary = "Somar quantidade por ordem de compra", description = "Retorna a quantidade total dos itens de uma ordem de compra")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantidade total calculada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    public ResponseEntity<Long> sumQuantidadeByIdOrdemCompra(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull Integer idOrdemCompra) {
        try {
            Long quantidadeTotal = itemOrdemCompraService.sumQuantidadeByIdOrdemCompra(idOrdemCompra);
            return ResponseEntity.ok(quantidadeTotal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Atualiza valor total de um item.
     * 
     * @param item Item a ter o valor total atualizado
     * @return ResponseEntity com o item atualizado
     */
    @PutMapping("/atualizar-valor-total")
    @Operation(summary = "Atualizar valor total", description = "Recalcula e atualiza o valor total de um item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valor total atualizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ItemOrdemCompra.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item não encontrado", content = @Content)
    })
    public ResponseEntity<ItemOrdemCompra> atualizarValorTotal(
            @Parameter(description = "Item a ser atualizado", required = true) @Valid @RequestBody ItemOrdemCompra item) {
        try {
            ItemOrdemCompra itemAtualizado = itemOrdemCompraService.atualizarValorTotal(item);
            return ResponseEntity.ok(itemAtualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}