package com.br.fasipe.estoque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicação principal do Sistema de Estoque Fasiclin.
 * 
 * @EnableScheduling - Habilita tarefas agendadas para verificação
 *                   automática de reposição de estoque.
 */
@SpringBootApplication
@EnableScheduling
public class EstoqueApplication {

	public static void main(String[] args) {
		SpringApplication.run(EstoqueApplication.class, args);
	}

}
