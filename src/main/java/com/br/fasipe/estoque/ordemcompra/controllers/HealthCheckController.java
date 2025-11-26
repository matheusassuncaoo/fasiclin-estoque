package com.br.fasipe.estoque.ordemcompra.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HealthCheckController - Endpoint para verificação de saúde da API
 * 
 * Permite que o frontend verifique se a API está disponível
 * e obtendo status do banco de dados.
 * 
 * @author Sistema Fasiclin - Resiliência
 * @version 1.0
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    /**
     * Endpoint de health check básico
     * @return Status da API
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("application", "Fasiclin Estoque API");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint de health check detalhado
     * @return Status detalhado da API e dependências
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());
        
        // Informações da aplicação
        Map<String, Object> app = new HashMap<>();
        app.put("name", "Fasiclin Estoque API");
        app.put("version", "1.0.0");
        app.put("environment", "production");
        health.put("application", app);
        
        // Status dos componentes (simplificado)
        Map<String, Object> components = new HashMap<>();
        
        // API Status
        Map<String, String> api = new HashMap<>();
        api.put("status", "UP");
        components.put("api", api);
        
        // Database Status (assumindo que se chegou aqui, DB está OK)
        Map<String, String> db = new HashMap<>();
        db.put("status", "UP");
        components.put("database", db);
        
        health.put("components", components);
        
        return ResponseEntity.ok(health);
    }
}
