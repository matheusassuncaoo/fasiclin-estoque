package com.br.fasipe.estoque.ordemcompra.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra;
import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra.StatusOrdemCompra;
import com.br.fasipe.estoque.ordemcompra.services.ItemOrdemCompraService;
import com.br.fasipe.estoque.ordemcompra.services.OrdemCompraService;
import com.br.fasipe.estoque.ordemcompra.services.UsuarioService;

import jakarta.persistence.EntityNotFoundException;

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

/**
 * Controller REST para gerenciamento de Ordens de Compra.
 * 
 * Fornece endpoints para operações CRUD e consultas especializadas
 * relacionadas às ordens de compra do sistema de estoque.
 * 
 * @author Sistema Fasiclin
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/ordens-compra")
@Validated
@CrossOrigin(origins = { "http://localhost:5500", "http://127.0.0.1:5500" }, allowCredentials = "true")
@Tag(name = "Ordem de Compra", description = "API para gerenciamento de ordens de compra")
public class OrdemCompraController {

    @Autowired
    private OrdemCompraService ordemCompraService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ItemOrdemCompraService itemOrdemCompraService;

    /**
     * Busca uma ordem de compra por ID.
     * 
     * @param id ID da ordem de compra
     * @return ResponseEntity com a ordem de compra encontrada
     * @throws EntityNotFoundException se a ordem não for encontrada
     */
    @Operation(summary = "Buscar ordem de compra por ID", description = "Retorna uma ordem de compra específica baseada no ID fornecido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de compra encontrada", content = @Content(schema = @Schema(implementation = OrdemCompra.class))),
            @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrdemCompra> findById(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull @Min(1) Integer id) {
        OrdemCompra ordemCompra = ordemCompraService.findById(id);
        return ResponseEntity.ok(ordemCompra);
    }

    /**
     * Lista todas as ordens de compra com paginação.
     * 
     * @param pageable configurações de paginação
     * @return ResponseEntity com página de ordens de compra
     */
    @Operation(summary = "Listar todas as ordens de compra", description = "Retorna uma lista paginada de todas as ordens de compra")
    @ApiResponse(responseCode = "200", description = "Lista de ordens de compra retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<OrdemCompra>> findAll() {
        List<OrdemCompra> ordens = ordemCompraService.findAll();
        return ResponseEntity.ok(ordens);
    }

    /**
     * Cria uma nova ordem de compra.
     * 
     * @param ordemCompra dados da ordem de compra a ser criada
     * @return ResponseEntity com a ordem de compra criada
     */
    @Operation(summary = "Criar nova ordem de compra", description = "Cria uma nova ordem de compra no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ordem de compra criada com sucesso", content = @Content(schema = @Schema(implementation = OrdemCompra.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
    })
    @PostMapping
    public ResponseEntity<OrdemCompra> create(
            @Parameter(description = "Dados da ordem de compra", required = true) @Valid @RequestBody OrdemCompra ordemCompra) {
        OrdemCompra novaOrdem = ordemCompraService.create(ordemCompra);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaOrdem);
    }

    /**
     * Atualiza uma ordem de compra existente.
     * 
     * @param id          ID da ordem de compra a ser atualizada
     * @param ordemCompra novos dados da ordem de compra
     * @return ResponseEntity com a ordem de compra atualizada
     */
    @Operation(summary = "Atualizar ordem de compra", description = "Atualiza os dados de uma ordem de compra existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem de compra atualizada com sucesso", content = @Content(schema = @Schema(implementation = OrdemCompra.class))),
            @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "422", description = "Erro de validação de negócio")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrdemCompra> update(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull @Min(1) Integer id,
            @Parameter(description = "Novos dados da ordem de compra", required = true) @Valid @RequestBody OrdemCompra ordemCompra) {
        try {
            // Garante que o ID do path seja usado
            ordemCompra.setId(id);
            OrdemCompra ordemAtualizada = ordemCompraService.update(ordemCompra);
            return ResponseEntity.ok(ordemAtualizada);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * Remove uma ordem de compra.
     * 
     * @param id ID da ordem de compra a ser removida
     * @return ResponseEntity vazio
     */
    @Operation(summary = "Remover ordem de compra", description = "Remove uma ordem de compra do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ordem de compra removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada"),
            @ApiResponse(responseCode = "422", description = "Não é possível remover ordem com este status")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull @Min(1) Integer id) {
        try {
            ordemCompraService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // Este bloco agora é teoricamente redundante se a chamada for sempre para /authenticated,
            // mas é uma boa prática mantê-lo para o endpoint não autenticado.
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    /**
     * Busca ordens de compra por status.
     * 
     * @param status status da ordem de compra
     * @return ResponseEntity com lista de ordens de compra
     */
    @Operation(summary = "Buscar ordens por status", description = "Retorna ordens de compra filtradas por status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrdemCompra>> findByStatus(
            @Parameter(description = "Status da ordem de compra", required = true) @PathVariable StatusOrdemCompra status) {
        List<OrdemCompra> ordens = ordemCompraService.findByStatus(status);
        return ResponseEntity.ok(ordens);
    }

    /**
     * Busca ordens de compra por faixa de valor.
     * 
     * @param valorMin valor mínimo
     * @param valorMax valor máximo
     * @return ResponseEntity com lista de ordens de compra
     */
    @Operation(summary = "Buscar ordens por faixa de valor", description = "Retorna ordens de compra dentro de uma faixa de valores")
    @GetMapping("/valor")
    public ResponseEntity<List<OrdemCompra>> findByValorBetween(
            @Parameter(description = "Valor mínimo", required = true) @RequestParam @NotNull BigDecimal valorMin,
            @Parameter(description = "Valor máximo", required = true) @RequestParam @NotNull BigDecimal valorMax) {
        List<OrdemCompra> ordens = ordemCompraService.findByValorBetween(valorMin, valorMax);
        return ResponseEntity.ok(ordens);
    }

    /**
     * Busca ordens de compra por data prevista.
     * 
     * @param dataPrevista data prevista de entrega
     * @return ResponseEntity com lista de ordens de compra
     */
    @Operation(summary = "Buscar ordens por data prevista", description = "Retorna ordens de compra com data prevista específica")
    @GetMapping("/data-prevista")
    public ResponseEntity<List<OrdemCompra>> findByDataPrevista(
            @Parameter(description = "Data prevista de entrega", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPrevista) {
        List<OrdemCompra> ordens = ordemCompraService.findByDataPrevista(dataPrevista);
        return ResponseEntity.ok(ordens);
    }

    /**
     * Busca ordens de compra por período de criação.
     * 
     * @param dataInicio data de início do período
     * @param dataFim    data de fim do período
     * @return ResponseEntity com lista de ordens de compra
     */
    @Operation(summary = "Buscar ordens por período de criação", description = "Retorna ordens de compra criadas em um período específico")
    @GetMapping("/periodo")
    public ResponseEntity<List<OrdemCompra>> findByPeriodoCriacao(
            @Parameter(description = "Data inicial do período (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final do período (formato: yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<OrdemCompra> ordens = ordemCompraService.findByPeriodoCriacao(dataInicio, dataFim);
        return ResponseEntity.ok(ordens);
    }

    /**
     * Busca ordens de compra em atraso.
     * 
     * @return ResponseEntity com lista de ordens de compra em atraso
     */
    @Operation(summary = "Buscar ordens em atraso", description = "Retorna ordens de compra com entrega em atraso")
    @GetMapping("/atraso")
    public ResponseEntity<List<OrdemCompra>> findOrdensEmAtraso() {
        List<OrdemCompra> ordens = ordemCompraService.findOrdensEmAtraso();
        return ResponseEntity.ok(ordens);
    }

    /**
     * Conta ordens de compra por status.
     * 
     * @param status status da ordem de compra
     * @return ResponseEntity com a contagem
     */
    @Operation(summary = "Contar ordens por status", description = "Retorna a quantidade de ordens de compra por status")
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countByStatus(
            @Parameter(description = "Status da ordem de compra", required = true) @PathVariable StatusOrdemCompra status) {
        Long count = ordemCompraService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    /**
     * Remove uma ordem de compra com autenticação.
     * 
     * @param id      ID da ordem de compra a ser removida
     * @param request dados de autenticação e motivo
     * @return ResponseEntity com confirmação
     */
    @Operation(summary = "Remover ordem de compra com autenticação", description = "Remove uma ordem de compra com validação de usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ordem removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada"),
            @ApiResponse(responseCode = "422", description = "Ordem não pode ser removida")
    })
    @DeleteMapping("/{id}/authenticated")
    public ResponseEntity<?> deleteWithAuth(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull @Min(1) Integer id,
            @Parameter(description = "Credenciais de autenticação", required = true) @Valid @RequestBody com.br.fasipe.estoque.ordemcompra.dto.DeactivationRequestDTO request) {

        try {
            // Validar credenciais do usuário
            boolean credenciaisValidas = usuarioService.autenticarUsuario(request.getLogin(), request.getSenha());

            if (!credenciaisValidas) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciais inválidas",
                                "message", "Login ou senha incorretos"));
            }

            // Remover a ordem com auditoria
            ordemCompraService.deleteWithAudit(id, request.getLogin(), request.getMotivo());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ordem de compra removida com sucesso"));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Não encontrado", "message", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", "Operação não permitida", "message", e.getMessage()));

        } catch (Exception e) {
            // Adiciona log do erro no console do servidor para depuração
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno", "message", "Erro inesperado: " + e.getMessage()));
        }
    }

    /**
     * Lista todos os itens de uma ordem de compra específica.
     * 
     * @param id ID da ordem de compra
     * @return ResponseEntity com lista de itens da ordem
     */
    @Operation(summary = "Listar itens da ordem de compra", description = "Retorna todos os itens de uma ordem de compra específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de itens retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada")
    })
    @GetMapping("/{id}/itens")
    public ResponseEntity<Map<String, Object>> listarItens(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull @Min(1) Integer id) {

        try {
            // Verificar se a ordem existe
            OrdemCompra ordem = ordemCompraService.findById(id);
            if (ordem == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ordem não encontrada",
                                "message", "Ordem de compra #" + id + " não existe"));
            }

            // Buscar itens da ordem
            List<ItemOrdemCompra> itens = itemOrdemCompraService.findByIdOrdemCompra(id);

            // Calcular resumo
            BigDecimal valorTotal = itens.stream()
                    .map(ItemOrdemCompra::getVlrTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalItens = itens.size();
            int quantidadeTotal = itens.stream()
                    .mapToInt(ItemOrdemCompra::getQntd)
                    .sum();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "itens", itens,
                    "resumo", Map.of(
                            "totalItens", totalItens,
                            "quantidadeTotal", quantidadeTotal,
                            "valorTotal", valorTotal)));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Não encontrado", "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno",
                            "message", "Erro inesperado: " + e.getMessage()));
        }
    }

    /**
     * Teste CORS - endpoint simples para verificar conectividade
     * 
     * @param id ID da ordem de compra
     * @return ResponseEntity com status de teste
     */
    @Operation(summary = "Teste de conectividade CORS", description = "Endpoint de teste para verificar CORS")
    @GetMapping("/{id}/itens/test")
    public ResponseEntity<Map<String, Object>> testarConectividade(
            @PathVariable @NotNull @Min(1) Integer id) {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "CORS funcionando",
                "ordemId", id,
                "timestamp", System.currentTimeMillis()));
    }

    /**
     * Adiciona múltiplos itens a uma ordem de compra.
     * 
     * @param id    ID da ordem de compra
     * @param itens Lista de itens a serem adicionados
     * @return ResponseEntity com resultado da operação
     */
    @Operation(summary = "Adicionar itens à ordem", description = "Adiciona múltiplos itens a uma ordem de compra existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Itens adicionados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem de compra não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados dos itens inválidos")
    })
    @PostMapping("/{id}/itens")
    public ResponseEntity<Map<String, Object>> adicionarItens(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull @Min(1) Integer id,
            @Parameter(description = "Lista de itens", required = true) @Valid @RequestBody List<Map<String, Object>> itens) {

        try {
            // Validações iniciais
            if (itens == null || itens.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Lista de itens inválida",
                                "message", "A lista de itens não pode estar vazia"));
            }

            // Verificar se a ordem existe
            OrdemCompra ordem = ordemCompraService.findById(id);
            if (ordem == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ordem não encontrada",
                                "message", "Ordem de compra #" + id + " não existe"));
            }

            // Verificar se a ordem permite adição de itens (status)
            if (ordem.getStatusOrdemCompra() == StatusOrdemCompra.CONC ||
                    ordem.getStatusOrdemCompra() == StatusOrdemCompra.CANC) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Status inválido",
                                "message", "Não é possível adicionar itens a uma ordem " +
                                        ordem.getStatusOrdemCompra().toString().toLowerCase()));
            }

            // Validar dados dos itens antes da conversão
            for (int i = 0; i < itens.size(); i++) {
                Map<String, Object> itemMap = itens.get(i);
                String posicao = "Item " + (i + 1);

                if (!itemMap.containsKey("produtoId") || itemMap.get("produtoId") == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Produto obrigatório",
                                    "message", posicao + ": ID do produto é obrigatório"));
                }

                if (!itemMap.containsKey("quantidade") || itemMap.get("quantidade") == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Quantidade obrigatória",
                                    "message", posicao + ": Quantidade é obrigatória"));
                }

                try {
                    int quantidade = Integer.valueOf(itemMap.get("quantidade").toString());
                    if (quantidade <= 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Quantidade inválida",
                                        "message", posicao + ": Quantidade deve ser maior que zero"));
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Quantidade inválida",
                                    "message", posicao + ": Quantidade deve ser um número inteiro"));
                }
            }

            // Converter Map para entidades ItemOrdemCompra
            List<ItemOrdemCompra> novoItens = itens.stream().map(itemMap -> {
                ItemOrdemCompra item = new ItemOrdemCompra();
                item.setIdOrdComp(Math.toIntExact(id));
                item.setIdProduto(Integer.valueOf(itemMap.get("produtoId").toString()));
                item.setQntd(Integer.valueOf(itemMap.get("quantidade").toString()));
                
                // Preço unitário é opcional - será definido pelo almoxarifado posteriormente
                // Se não fornecido, será definido como 0 pelo @PrePersist
                if (itemMap.containsKey("precoUnitario") && itemMap.get("precoUnitario") != null) {
                    item.setValor(new BigDecimal(itemMap.get("precoUnitario").toString()));
                }

                // Data de vencimento (usar data padrão por enquanto - pode ser melhorada)
                if (itemMap.get("dataVencimento") != null) {
                    item.setDataVenc(LocalDate.parse(itemMap.get("dataVencimento").toString()));
                } else {
                    item.setDataVenc(LocalDate.now().plusDays(30)); // 30 dias por padrão
                }

                // Calcular valor total automaticamente
                item.inicializarValorTotal();

                return item;
            }).toList();

            // Salvar os itens usando o service
            List<ItemOrdemCompra> itensSalvos = itemOrdemCompraService.salvarItensOrdem(Math.toIntExact(id), novoItens);

            // Recalcular valor total da ordem com base nos itens
            BigDecimal novoValorTotalOrdem = itemOrdemCompraService.sumValorTotalByIdOrdemCompra(Math.toIntExact(id));
            ordem.setValor(novoValorTotalOrdem);
            ordemCompraService.update(ordem);

            int itensAdicionados = itensSalvos.size();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", itensAdicionados + " itens adicionados com sucesso",
                    "itensAdicionados", itensAdicionados,
                    "novoValorTotal", ordem.getValor()));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Não encontrado", "message", e.getMessage()));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Dados inválidos",
                            "message", "Formato numérico inválido nos itens"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno",
                            "message", "Erro inesperado: " + e.getMessage()));
        }
    }

    /**
     * Atualiza um item específico de uma ordem de compra.
     * 
     * @param ordemId  ID da ordem de compra
     * @param itemId   ID do item a ser atualizado
     * @param itemData Novos dados do item
     * @return ResponseEntity com resultado da operação
     */
    @Operation(summary = "Atualizar item da ordem", description = "Atualiza os dados de um item específico de uma ordem de compra")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item ou ordem não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{ordemId}/itens/{itemId}")
    public ResponseEntity<Map<String, Object>> atualizarItem(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull @Min(1) Integer ordemId,
            @Parameter(description = "ID do item", required = true) @PathVariable @NotNull @Min(1) Integer itemId,
            @Parameter(description = "Novos dados do item", required = true) @Valid @RequestBody Map<String, Object> itemData) {

        try {
            // Verificar se a ordem existe
            OrdemCompra ordem = ordemCompraService.findById(ordemId);
            if (ordem == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ordem não encontrada",
                                "message", "Ordem de compra #" + ordemId + " não existe"));
            }

            // Verificar se o item existe
            ItemOrdemCompra item = itemOrdemCompraService.findById(itemId);
            if (item == null || !item.getIdOrdComp().equals(ordemId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Item não encontrado",
                                "message", "Item #" + itemId + " não existe na ordem #" + ordemId));
            }

            // Validar dados
            if (itemData.containsKey("quantidade") && itemData.get("quantidade") != null) {
                try {
                    int novaQuantidade = Integer.valueOf(itemData.get("quantidade").toString());
                    if (novaQuantidade <= 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Quantidade inválida",
                                        "message", "Quantidade deve ser maior que zero"));
                    }
                    item.setQntd(novaQuantidade);
                } catch (NumberFormatException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Quantidade inválida",
                                    "message", "Quantidade deve ser um número inteiro"));
                }
            }

            if (itemData.containsKey("precoUnitario") && itemData.get("precoUnitario") != null) {
                try {
                    BigDecimal novoPreco = new BigDecimal(itemData.get("precoUnitario").toString());
                    if (novoPreco.compareTo(BigDecimal.ZERO) <= 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Preço inválido",
                                        "message", "Preço deve ser maior que zero"));
                    }
                    item.setValor(novoPreco);
                } catch (NumberFormatException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Preço inválido",
                                    "message", "Preço deve ser um número decimal válido"));
                }
            }

            // Recalcular valor total do item
            item.inicializarValorTotal();

            // Salvar item atualizado
            ItemOrdemCompra itemAtualizado = itemOrdemCompraService.update(item);

            // Recalcular valor total da ordem
            BigDecimal novoValorTotalOrdem = itemOrdemCompraService.sumValorTotalByIdOrdemCompra(ordemId);
            ordem.setValor(novoValorTotalOrdem);
            ordemCompraService.update(ordem);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Item atualizado com sucesso",
                    "item", itemAtualizado,
                    "novoValorTotalOrdem", novoValorTotalOrdem));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Não encontrado", "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno",
                            "message", "Erro inesperado: " + e.getMessage()));
        }
    }

    /**
     * Remove um item específico de uma ordem de compra.
     * 
     * @param ordemId ID da ordem de compra
     * @param itemId  ID do item a ser removido
     * @return ResponseEntity com resultado da operação
     */
    @Operation(summary = "Remover item da ordem", description = "Remove um item específico de uma ordem de compra")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item ou ordem não encontrada")
    })
    @DeleteMapping("/{ordemId}/itens/{itemId}")
    public ResponseEntity<Map<String, Object>> removerItem(
            @Parameter(description = "ID da ordem de compra", required = true) @PathVariable @NotNull @Min(1) Integer ordemId,
            @Parameter(description = "ID do item", required = true) @PathVariable @NotNull @Min(1) Integer itemId) {

        try {
            // Verificar se a ordem existe
            OrdemCompra ordem = ordemCompraService.findById(ordemId);
            if (ordem == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ordem não encontrada",
                                "message", "Ordem de compra #" + ordemId + " não existe"));
            }

            // Verificar se o item existe e pertence à ordem
            ItemOrdemCompra item = itemOrdemCompraService.findById(itemId);
            if (item == null || !item.getIdOrdComp().equals(ordemId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Item não encontrado",
                                "message", "Item #" + itemId + " não existe na ordem #" + ordemId));
            }

            // Remover o item
            itemOrdemCompraService.deleteById(itemId);

            // Recalcular valor total da ordem
            BigDecimal novoValorTotalOrdem = itemOrdemCompraService.sumValorTotalByIdOrdemCompra(ordemId);
            ordem.setValor(novoValorTotalOrdem);
            ordemCompraService.update(ordem);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Item removido com sucesso",
                    "itemRemovidoId", itemId,
                    "novoValorTotalOrdem", novoValorTotalOrdem));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Não encontrado", "message", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno",
                            "message", "Erro inesperado: " + e.getMessage()));
        }
    }
}
