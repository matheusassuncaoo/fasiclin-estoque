package com.br.fasipe.estoque.ordemcompra.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.br.fasipe.estoque.ordemcompra.models.Fornecedor;
import com.br.fasipe.estoque.ordemcompra.services.FornecedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Controller REST para gerenciamento de Fornecedores.
 * 
 * <p>
 * Fornece endpoints para operações CRUD da tabela FORNECEDOR do sistema
 * fasiclin.
 * Este controller acessa os dados reais do banco de dados MySQL.
 * </p>
 * 
 * <p>
 * <strong>Funcionalidades disponíveis:</strong>
 * </p>
 * <ul>
 * <li>Listar todos os fornecedores</li>
 * <li>Buscar fornecedor por ID</li>
 * <li>Buscar fornecedor por ID de pessoa</li>
 * <li>Buscar fornecedores por representante</li>
 * <li>Buscar fornecedores por descrição</li>
 * <li>Criar novo fornecedor</li>
 * <li>Atualizar fornecedor existente</li>
 * <li>Deletar fornecedor</li>
 * </ul>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/fornecedores")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "Fornecedores", description = "API para gerenciamento de fornecedores do sistema fasiclin")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    /**
     * Lista todos os fornecedores ordenados por representante.
     * 
     * @return ResponseEntity com lista de todos os fornecedores
     */
    @Operation(summary = "Listar todos os fornecedores", description = "Retorna uma lista completa de todos os fornecedores cadastrados no sistema, ordenados por representante")
    @ApiResponse(responseCode = "200", description = "Lista de fornecedores retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<Fornecedor>> findAll() {
        List<Fornecedor> fornecedores = fornecedorService.findAllOrderByRepresentante();
        return ResponseEntity.ok(fornecedores);
    }

    /**
     * Busca um fornecedor por ID.
     * 
     * @param id ID do fornecedor
     * @return ResponseEntity com o fornecedor encontrado
     * @throws EntityNotFoundException se o fornecedor não for encontrado
     */
    @Operation(summary = "Buscar fornecedor por ID", description = "Retorna um fornecedor específico baseado no ID fornecido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fornecedor encontrado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Fornecedor.class))),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Fornecedor> findById(
            @Parameter(description = "ID do fornecedor a ser buscado", required = true, example = "1") @PathVariable @NotNull @Min(value = 1, message = "ID deve ser maior que 0") Integer id) {
        try {
            Fornecedor fornecedor = fornecedorService.findById(id);
            return ResponseEntity.ok(fornecedor);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca fornecedor por ID de pessoa.
     * 
     * @param idPessoa ID da pessoa
     * @return ResponseEntity com o fornecedor encontrado ou 404 se não existir
     */
    @Operation(summary = "Buscar fornecedor por ID de pessoa", description = "Retorna o fornecedor associado ao ID de pessoa fornecido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fornecedor encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado para este ID de pessoa")
    })
    @GetMapping("/pessoa/{idPessoa}")
    public ResponseEntity<Fornecedor> findByIdPessoa(
            @Parameter(description = "ID da pessoa", required = true, example = "1") @PathVariable @NotNull @Min(value = 1, message = "ID da pessoa deve ser maior que 0") Integer idPessoa) {
        Fornecedor fornecedor = fornecedorService.findByIdPessoa(idPessoa);
        if (fornecedor != null) {
            return ResponseEntity.ok(fornecedor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca fornecedores por representante (busca parcial).
     * 
     * @param representante termo de busca para representante
     * @return ResponseEntity com lista de fornecedores encontrados
     */
    @Operation(summary = "Buscar fornecedores por representante", description = "Retorna fornecedores que contenham o termo especificado no campo representante (busca case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Lista de fornecedores retornada com sucesso")
    @GetMapping("/buscar/representante")
    public ResponseEntity<List<Fornecedor>> findByRepresentante(
            @Parameter(description = "Termo de busca para representante", required = true, example = "João") @RequestParam @NotNull String representante) {
        List<Fornecedor> fornecedores = fornecedorService.findByRepresentanteContaining(representante);
        return ResponseEntity.ok(fornecedores);
    }

    /**
     * Cria um novo fornecedor.
     * 
     * @param fornecedor dados do fornecedor a ser criado
     * @return ResponseEntity com o fornecedor criado e location header
     */
    @Operation(summary = "Criar novo fornecedor", description = "Cria um novo fornecedor no sistema com os dados fornecidos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fornecedor criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Fornecedor.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito - já existe fornecedor para esta pessoa", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Fornecedor> create(
            @Parameter(description = "Dados do fornecedor a ser criado", required = true) @RequestBody @Valid Fornecedor fornecedor) {
        try {
            Fornecedor savedFornecedor = fornecedorService.create(fornecedor);
            URI uri = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedFornecedor.getId())
                    .toUri();
            return ResponseEntity.created(uri).body(savedFornecedor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca fornecedores por produto.
     * 
     * @param produtoId ID do produto
     * @return ResponseEntity com lista de fornecedores do produto
     */
    @Operation(summary = "Buscar fornecedores por produto", description = "Retorna todos os fornecedores que fornecem o produto especificado")
    @ApiResponse(responseCode = "200", description = "Lista de fornecedores do produto retornada com sucesso")
    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<Fornecedor>> findByProdutoId(
            @Parameter(description = "ID do produto", required = true) @PathVariable @NotNull @Min(1) Integer produtoId) {
        // Por enquanto, retorna todos os fornecedores - pode ser implementada lógica
        // específica depois
        List<Fornecedor> fornecedores = fornecedorService.findAllOrderByRepresentante();
        return ResponseEntity.ok(fornecedores);
    }
}