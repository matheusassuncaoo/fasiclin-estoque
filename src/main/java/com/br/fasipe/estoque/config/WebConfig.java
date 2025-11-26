package com.br.fasipe.estoque.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuração de CORS e recursos estáticos para o sistema.
 * Suporta tanto desenvolvimento local quanto produção no Render.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5500,http://127.0.0.1:5500}")
    private String allowedOriginsConfig;

    /**
     * Retorna lista de origens permitidas
     */
    private List<String> getAllowedOrigins() {
        List<String> origins = new ArrayList<>();
        
        // Origens de desenvolvimento
        origins.add("http://localhost:5500");
        origins.add("http://127.0.0.1:5500");
        origins.add("http://localhost:8080");
        origins.add("http://127.0.0.1:8080");
        
        // Origens de produção (Render)
        origins.add("https://*.onrender.com");
        
        // Origens configuradas via variável de ambiente
        if (allowedOriginsConfig != null && !allowedOriginsConfig.isEmpty()) {
            String[] configuredOrigins = allowedOriginsConfig.split(",");
            for (String origin : configuredOrigins) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty() && !origins.contains(trimmed)) {
                    origins.add(trimmed);
                }
            }
        }
        
        return origins;
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        List<String> origins = getAllowedOrigins();
        
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowCredentials(true)      
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Servir arquivos estáticos do frontend
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/frontend/")
                .setCachePeriod(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Usar allowedOriginPatterns para suportar wildcards
        configuration.setAllowedOriginPatterns(getAllowedOrigins());
        
        // Métodos HTTP necessários
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers necessários
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));
        
        // Permitir credentials
        configuration.setAllowCredentials(true);
        
        // Cache preflight por 1 hora
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}