package com.br.fasipe.estoque.ordemcompra.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.fasipe.estoque.ordemcompra.services.OrdemCompraAutomaticaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller REST para gerenciamento de Ordens de Compra Automáticas.
 * 
 * <p>
 * Fornece endpoints para verificar e disparar ordens de compra
 * automáticas baseadas nos níveis de estoque.
 * </p>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/ordens-automaticas")
@CrossOrigin(origins = { "http://localhost:5500", "http://127.0.0.1:5500" }, allowCredentials = "true")
@Tag(name = "Ordens Automáticas", description = "API para gerenciamento de ordens de compra automáticas")
public class OrdemCompraAutomaticaController {

    @Autowired
    private OrdemCompraAutomaticaService ordemAutomaticaService;

    /**
     * Retorna estatísticas do sistema de ordens automáticas.
     * 
     * @return ResponseEntity com estatísticas
     */
    @Operation(summary = "Obter estatísticas", description = "Retorna estatísticas do sistema de ordens automáticas")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> getEstatisticas() {
        Map<String, Object> stats = ordemAutomaticaService.getEstatisticas();
        return ResponseEntity.ok(stats);
    }

    /**
     * Força uma verificação manual de necessidade de reposição.
     * 
     * @return ResponseEntity com resultado da verificação
     */
    @Operation(summary = "Forçar verificação", description = "Executa verificação manual de produtos que precisam de reposição")
    @ApiResponse(responseCode = "200", description = "Verificação executada com sucesso")
    @PostMapping("/verificar")
    public ResponseEntity<Map<String, Object>> forcarVerificacao() {
        Map<String, Object> resultado = ordemAutomaticaService.forcarVerificacao();
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint de status do serviço automático.
     * 
     * @return ResponseEntity com status do serviço
     */
    @Operation(summary = "Status do serviço", description = "Retorna o status atual do serviço de ordens automáticas")
    @ApiResponse(responseCode = "200", description = "Status retornado com sucesso")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> stats = ordemAutomaticaService.getEstatisticas();
        return ResponseEntity.ok(Map.of(
                "status", "online",
                "servicoAtivo", stats.get("servicoAtivo"),
                "ultimaVerificacao", stats.get("ultimaVerificacao"),
                "produtosCriticos", stats.get("produtosCriticos")));
    }
}

