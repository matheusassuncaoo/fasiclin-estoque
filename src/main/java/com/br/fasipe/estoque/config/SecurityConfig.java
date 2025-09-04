package com.br.fasipe.estoque.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para o sistema de estoque.
 * 
 * <p>Esta configuração desabilita a autenticação para facilitar o desenvolvimento
 * e testes da API. Em produção, deve ser implementada uma estratégia de segurança
 * adequada com autenticação e autorização.</p>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura a cadeia de filtros de segurança.
     * 
     * <p>Desabilita CSRF, permite todas as requisições HTTP e desabilita
     * a autenticação básica para facilitar o desenvolvimento.</p>
     * 
     * @param http o objeto HttpSecurity para configuração
     * @return SecurityFilterChain configurado
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());
        
        return http.build();
    }
}