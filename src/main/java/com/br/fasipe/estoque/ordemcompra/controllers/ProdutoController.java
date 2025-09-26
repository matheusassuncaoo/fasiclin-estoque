package com.br.fasipe.estoque.ordemcompra.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.fasipe.estoque.ordemcompra.models.Produto;
import com.br.fasipe.estoque.ordemcompra.services.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Controller REST para gerenciamento de Produtos.
 * 
 * <p>Fornece endpoints para consultas da tabela PRODUTO do sistema fasiclin.
 * Este controller acessa os dados reais do banco de dados MySQL.</p>
 * 
 * <p><strong>Funcionalidades disponíveis:</strong></p>
 * <ul>
 *   <li>Listar todos os produtos</li>
 *   <li>Buscar produto por ID</li>
 *   <li>Buscar produtos que precisam de reposição</li>
 *   <li>Buscar produtos por código de barras</li>
 *   <li>Buscar produtos por nome</li>
 * </ul>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/produtos")
@Validated
@Tag(name = "Produtos", description = "API para consulta de produtos do sistema fasiclin")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    /**
     * Lista todos os produtos.
     * 
     * @return ResponseEntity com lista de todos os produtos
     */
    @Operation(summary = "Listar todos os produtos", 
               description = "Retorna uma lista completa de todos os produtos cadastrados no sistema")
    @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<Produto>> findAll() {
        List<Produto> produtos = produtoService.findAll();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca um produto por ID.
     * 
     * @param id ID do produto
     * @return ResponseEntity com o produto encontrado
     * @throws EntityNotFoundException se o produto não for encontrado
     */
    @Operation(summary = "Buscar produto por ID", 
               description = "Retorna um produto específico baseado no ID fornecido")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = Produto.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Produto> findById(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable @NotNull @Min(1) Integer id) {
        try {
            Produto produto = produtoService.findById(id);
            return ResponseEntity.ok(produto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca produtos que precisam de reposição.
     * 
     * <p>Retorna produtos com estoque atual menor ou igual ao ponto de pedido
     * ou com estoque menor que o mínimo configurado.</p>
     * 
     * @return ResponseEntity com lista de produtos para reposição
     */
    @Operation(summary = "Buscar produtos para reposição", 
               description = "Retorna produtos que precisam de reposição baseado no estoque atual vs estoque mínimo/ponto de pedido")
    @ApiResponse(responseCode = "200", description = "Lista de produtos para reposição retornada com sucesso")
    @GetMapping("/reposicao")
    public ResponseEntity<List<Produto>> findProdutosParaReposicao() {
        List<Produto> produtos = produtoService.findProdutosParaReposicao();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produto por código de barras.
     * 
     * @param codigoBarras Código de barras do produto
     * @return ResponseEntity com o produto encontrado
     */
    @Operation(summary = "Buscar produto por código de barras", 
               description = "Retorna um produto específico baseado no código de barras")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(schema = @Schema(implementation = Produto.class))),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "400", description = "Código de barras inválido")
    })
    @GetMapping("/codigo-barras/{codigoBarras}")
    public ResponseEntity<Produto> findByCodigoBarras(
            @Parameter(description = "Código de barras do produto", required = true)
            @PathVariable @NotNull String codigoBarras) {
        try {
            Produto produto = produtoService.findByCodigoBarras(codigoBarras);
            return ResponseEntity.ok(produto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca produtos por nome.
     * 
     * @param nome Nome ou parte do nome do produto
     * @return ResponseEntity com lista de produtos encontrados
     */
    @Operation(summary = "Buscar produtos por nome", 
               description = "Retorna produtos que contenham o texto especificado no nome")
    @ApiResponse(responseCode = "200", description = "Lista de produtos encontrados")
    @GetMapping("/buscar")
    public ResponseEntity<List<Produto>> findByNome(
            @Parameter(description = "Nome ou parte do nome do produto", required = true)
            @RequestParam @NotNull String nome) {
        List<Produto> produtos = produtoService.findByNome(nome);
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produtos com estoque baixo.
     * 
     * @return ResponseEntity com lista de produtos com estoque baixo
     */
    @Operation(summary = "Buscar produtos com estoque baixo", 
               description = "Retorna produtos com estoque atual menor que o estoque mínimo")
    @ApiResponse(responseCode = "200", description = "Lista de produtos com estoque baixo")
    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<Produto>> findProdutosEstoqueBaixo() {
        List<Produto> produtos = produtoService.findProdutosEstoqueBaixo();
        return ResponseEntity.ok(produtos);
    }

    /**
     * Busca produtos com estoque crítico (zerado).
     * 
     * @return ResponseEntity com lista de produtos com estoque zerado
     */
    @Operation(summary = "Buscar produtos com estoque crítico", 
               description = "Retorna produtos com estoque zerado ou criticamente baixo")
    @ApiResponse(responseCode = "200", description = "Lista de produtos com estoque crítico")
    @GetMapping("/estoque-critico")
    public ResponseEntity<List<Produto>> findProdutosEstoqueCritico() {
        List<Produto> produtos = produtoService.findProdutosEstoqueCritico();
        return ResponseEntity.ok(produtos);
    }
}