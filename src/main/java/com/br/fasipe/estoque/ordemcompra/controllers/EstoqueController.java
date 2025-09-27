package com.br.fasipe.estoque.ordemcompra.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.br.fasipe.estoque.ordemcompra.models.Estoque;
import com.br.fasipe.estoque.ordemcompra.services.EstoqueService;

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
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Controller REST para gerenciamento de Estoque.
 * 
 * <p>
 * Fornece endpoints para operações CRUD e consultas especializadas
 * relacionadas ao controle de inventário do sistema de estoque.
 * </p>
 * 
 * <p>
 * <strong>Funcionalidades disponíveis:</strong>
 * </p>
 * <ul>
 * <li>CRUD completo de registros de estoque</li>
 * <li>Consultas por produto, lote e quantidade</li>
 * <li>Operações de movimentação (entrada/saída)</li>
 * <li>Alertas de estoque baixo e zerado</li>
 * <li>Relatórios agregados por produto</li>
 * <li>Validações de integridade e negócio</li>
 * </ul>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/estoque")
@Validated
@Tag(name = "Estoque", description = "API para gerenciamento de estoque e inventário")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    /**
     * Busca um registro de estoque por ID.
     * 
     * @param id ID do registro de estoque
     * @return ResponseEntity com o registro de estoque encontrado
     * @throws EntityNotFoundException se o registro não for encontrado
     */
    @Operation(summary = "Buscar registro de estoque por ID", description = "Retorna um registro específico de estoque baseado no ID fornecido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro de estoque encontrado", content = @Content(schema = @Schema(implementation = Estoque.class))),
            @ApiResponse(responseCode = "404", description = "Registro de estoque não encontrado"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Estoque> findById(
            @Parameter(description = "ID do registro de estoque", required = true) @PathVariable @NotNull @Min(1) Integer id) {
        Estoque estoque = estoqueService.findById(id);
        return ResponseEntity.ok(estoque);
    }

    /**
     * Lista todos os registros de estoque.
     * 
     * @return ResponseEntity com lista de todos os registros de estoque
     */
    @Operation(summary = "Listar todos os registros de estoque", description = "Retorna uma lista completa de todos os registros de estoque")
    @ApiResponse(responseCode = "200", description = "Lista de registros de estoque retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<Estoque>> findAll() {
        List<Estoque> estoques = estoqueService.findAll();
        return ResponseEntity.ok(estoques);
    }

    /**
     * Cria um novo registro de estoque.
     * 
     * @param estoque dados do registro de estoque a ser criado
     * @return ResponseEntity com o registro de estoque criado
     */
    @Operation(summary = "Criar novo registro de estoque", description = "Cria um novo registro de estoque no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registro de estoque criado com sucesso", content = @Content(schema = @Schema(implementation = Estoque.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
    })
    @PostMapping
    public ResponseEntity<Estoque> create(
            @Parameter(description = "Dados do registro de estoque", required = true) @Valid @RequestBody Estoque estoque) {
        Estoque novoEstoque = estoqueService.create(estoque);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoEstoque);
    }

    /**
     * Atualiza um registro de estoque existente.
     * 
     * @param id      ID do registro de estoque
     * @param estoque novos dados do registro de estoque
     * @return ResponseEntity com o registro de estoque atualizado
     */
    @Operation(summary = "Atualizar registro de estoque", description = "Atualiza os dados de um registro de estoque existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro de estoque atualizado com sucesso", content = @Content(schema = @Schema(implementation = Estoque.class))),
            @ApiResponse(responseCode = "404", description = "Registro de estoque não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Estoque> update(
            @Parameter(description = "ID do registro de estoque", required = true) @PathVariable @NotNull @Min(1) Integer id,
            @Parameter(description = "Novos dados do registro de estoque", required = true) @Valid @RequestBody Estoque estoque) {
        estoque.setId(id);
        Estoque estoqueAtualizado = estoqueService.update(estoque);
        return ResponseEntity.ok(estoqueAtualizado);
    }

    /**
     * Remove um registro de estoque.
     * 
     * @param id ID do registro de estoque a ser removido
     * @return ResponseEntity vazio
     */
    @Operation(summary = "Remover registro de estoque", description = "Remove um registro de estoque do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Registro de estoque removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro de estoque não encontrado"),
            @ApiResponse(responseCode = "422", description = "Não é possível remover registro com estoque disponível")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do registro de estoque", required = true) @PathVariable @NotNull @Min(1) Integer id) {
        estoqueService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Busca registros de estoque por produto.
     * 
     * @param idProduto ID do produto
     * @return ResponseEntity com lista de registros do produto
     */
    @Operation(summary = "Buscar estoque por produto", description = "Retorna todos os registros de estoque de um produto específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registros encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID do produto inválido")
    })
    @GetMapping("/produto/{idProduto}")
    public ResponseEntity<List<Estoque>> findByIdProduto(
            @Parameter(description = "ID do produto", required = true) @PathVariable @NotNull @Min(1) Integer idProduto) {
        List<Estoque> estoques = estoqueService.findByIdProduto(idProduto);
        return ResponseEntity.ok(estoques);
    }

    /**
     * Busca registros de estoque por lote.
     * 
     * @param idLote ID do lote
     * @return ResponseEntity com lista de registros do lote
     */
    @Operation(summary = "Buscar estoque por lote", description = "Retorna todos os registros de estoque de um lote específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registros encontrados com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID do lote inválido")
    })
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<Estoque>> findByIdLote(
            @Parameter(description = "ID do lote", required = true) @PathVariable @NotNull @Min(1) Integer idLote) {
        List<Estoque> estoques = estoqueService.findByIdLote(idLote);
        return ResponseEntity.ok(estoques);
    }

    /**
     * Busca registros por quantidade específica.
     * 
     * @param quantidade Quantidade em estoque
     * @return ResponseEntity com lista de registros
     */
    @Operation(summary = "Buscar por quantidade específica", description = "Retorna registros com quantidade exata especificada")
    @GetMapping("/quantidade/{quantidade}")
    public ResponseEntity<List<Estoque>> findByQuantidade(
            @Parameter(description = "Quantidade em estoque", required = true) @PathVariable @NotNull @PositiveOrZero Integer quantidade) {
        List<Estoque> estoques = estoqueService.findByQuantidade(quantidade);
        return ResponseEntity.ok(estoques);
    }

    /**
     * Busca registros por faixa de quantidade.
     * 
     * @param quantidadeMin Quantidade mínima
     * @param quantidadeMax Quantidade máxima
     * @return ResponseEntity com lista de registros na faixa
     */
    @Operation(summary = "Buscar por faixa de quantidade", description = "Retorna registros com quantidade dentro da faixa especificada")
    @GetMapping("/quantidade")
    public ResponseEntity<List<Estoque>> findByQuantidadeBetween(
            @Parameter(description = "Quantidade mínima", required = true) @RequestParam @NotNull @PositiveOrZero Integer quantidadeMin,
            @Parameter(description = "Quantidade máxima", required = true) @RequestParam @NotNull @PositiveOrZero Integer quantidadeMax) {
        List<Estoque> estoques = estoqueService.findByQuantidadeBetween(quantidadeMin, quantidadeMax);
        return ResponseEntity.ok(estoques);
    }

    /**
     * Busca registros com estoque baixo.
     * 
     * @param quantidadeMinima Quantidade considerada como estoque baixo
     * @return ResponseEntity com lista de registros com estoque baixo
     */
    @Operation(summary = "Buscar estoque baixo", description = "Retorna registros com quantidade abaixo do limite especificado")
    @GetMapping("/baixo")
    public ResponseEntity<List<Estoque>> findEstoqueBaixo(
            @Parameter(description = "Quantidade mínima considerada como estoque baixo", required = true) @RequestParam @NotNull @Positive Integer quantidadeMinima) {
        List<Estoque> estoques = estoqueService.findEstoqueBaixo(quantidadeMinima);
        return ResponseEntity.ok(estoques);
    }

    /**
     * Busca registros com estoque zerado.
     * 
     * @return ResponseEntity com lista de registros com estoque zerado
     */
    @Operation(summary = "Buscar estoque zerado", description = "Retorna todos os registros com quantidade zero")
    @GetMapping("/zerado")
    public ResponseEntity<List<Estoque>> findEstoqueZerado() {
        List<Estoque> estoques = estoqueService.findEstoqueZerado();
        return ResponseEntity.ok(estoques);
    }

    /**
     * Conta registros de estoque por produto.
     * 
     * @param idProduto ID do produto
     * @return ResponseEntity com quantidade de registros
     */
    @Operation(summary = "Contar registros por produto", description = "Retorna a quantidade de registros de estoque de um produto")
    @GetMapping("/count/produto/{idProduto}")
    public ResponseEntity<Long> countByIdProduto(
            @Parameter(description = "ID do produto", required = true) @PathVariable @NotNull @Min(1) Integer idProduto) {
        Long count = estoqueService.countByIdProduto(idProduto);
        return ResponseEntity.ok(count);
    }

    /**
     * Soma quantidade total por produto.
     * 
     * @param idProduto ID do produto
     * @return ResponseEntity com quantidade total em estoque
     */
    @Operation(summary = "Somar quantidade total por produto", description = "Retorna a quantidade total em estoque de um produto (todos os lotes)")
    @GetMapping("/sum/produto/{idProduto}")
    public ResponseEntity<Long> sumQuantidadeByIdProduto(
            @Parameter(description = "ID do produto", required = true) @PathVariable @NotNull @Min(1) Integer idProduto) {
        Long total = estoqueService.sumQuantidadeByIdProduto(idProduto);
        return ResponseEntity.ok(total);
    }

    /**
     * Adiciona quantidade ao estoque.
     * 
     * @param idProduto  ID do produto
     * @param idLote     ID do lote (opcional)
     * @param quantidade Quantidade a ser adicionada
     * @return ResponseEntity com registro atualizado
     */
    @Operation(summary = "Adicionar estoque", description = "Adiciona quantidade ao estoque de um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque adicionado com sucesso", content = @Content(schema = @Schema(implementation = Estoque.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
    })
    @PostMapping("/adicionar")
    public ResponseEntity<Estoque> adicionarEstoque(
            @Parameter(description = "ID do produto", required = true) @RequestParam @NotNull @Min(1) Integer idProduto,
            @Parameter(description = "ID do lote (opcional)") @RequestParam(required = false) Integer idLote,
            @Parameter(description = "Quantidade a ser adicionada", required = true) @RequestParam @NotNull @Positive Integer quantidade) {
        Estoque estoque = estoqueService.adicionarEstoque(idProduto, idLote, quantidade);
        return ResponseEntity.ok(estoque);
    }

    /**
     * Remove quantidade do estoque.
     * 
     * @param idProduto  ID do produto
     * @param idLote     ID do lote (opcional)
     * @param quantidade Quantidade a ser removida
     * @return ResponseEntity com registro atualizado
     */
    @Operation(summary = "Remover estoque", description = "Remove quantidade do estoque de um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque removido com sucesso", content = @Content(schema = @Schema(implementation = Estoque.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado"),
            @ApiResponse(responseCode = "422", description = "Estoque insuficiente")
    })
    @PostMapping("/remover")
    public ResponseEntity<Estoque> removerEstoque(
            @Parameter(description = "ID do produto", required = true) @RequestParam @NotNull @Min(1) Integer idProduto,
            @Parameter(description = "ID do lote (opcional)") @RequestParam(required = false) Integer idLote,
            @Parameter(description = "Quantidade a ser removida", required = true) @RequestParam @NotNull @Positive Integer quantidade) {
        Estoque estoque = estoqueService.removerEstoque(idProduto, idLote, quantidade);
        return ResponseEntity.ok(estoque);
    }
}
