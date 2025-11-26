package com.br.fasipe.estoque.ordemcompra.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.br.fasipe.estoque.ordemcompra.models.Estoque;
import com.br.fasipe.estoque.ordemcompra.models.ItemOrdemCompra;
import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra;
import com.br.fasipe.estoque.ordemcompra.models.OrdemCompra.StatusOrdemCompra;
import com.br.fasipe.estoque.ordemcompra.models.Produto;

import jakarta.transaction.Transactional;

/**
 * Service para geração automática de Ordens de Compra.
 * 
 * <p>
 * Este serviço monitora o estoque de produtos e gera automaticamente
 * ordens de compra quando os níveis de estoque atingem pontos críticos.
 * </p>
 * 
 * <p>
 * <strong>Funcionalidades:</strong>
 * </p>
 * <ul>
 * <li>Verificação periódica de estoque</li>
 * <li>Geração automática de ordens de compra</li>
 * <li>Cálculo de quantidade ideal para reposição</li>
 * <li>Agrupamento de produtos por necessidade</li>
 * </ul>
 * 
 * @author Sistema Fasiclin - Módulo Estoque
 * @version 1.0
 * @since 2025
 */
@Service
public class OrdemCompraAutomaticaService {

    private static final Logger logger = LoggerFactory.getLogger(OrdemCompraAutomaticaService.class);

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private OrdemCompraService ordemCompraService;

    @Autowired
    private ItemOrdemCompraService itemOrdemCompraService;

    @Autowired
    private EstoqueService estoqueService;

    @Value("${ordem.automatica.enabled:true}")
    private boolean ordemAutomaticaEnabled;

    @Value("${ordem.automatica.dias.entrega:7}")
    private int diasParaEntrega;

    // Flag para controle de execução (evitar duplicatas)
    private volatile boolean verificacaoEmAndamento = false;

    // Última verificação
    private volatile LocalDate ultimaVerificacao = null;

    /**
     * Verifica necessidade de reposição a cada 2 horas.
     * Cron: segundo minuto hora dia mês dia-da-semana
     * "0 0 *\/2 * * *" = A cada 2 horas, no minuto 0
     */
    @Scheduled(cron = "0 0 */2 * * *")
    public void verificarReposicaoAgendada() {
        if (!ordemAutomaticaEnabled) {
            logger.info("[OrdemAutomatica] Serviço desabilitado via configuração");
            return;
        }

        // Evitar execuções paralelas
        if (verificacaoEmAndamento) {
            logger.warn("[OrdemAutomatica] Verificação já em andamento, pulando...");
            return;
        }

        try {
            verificacaoEmAndamento = true;
            logger.info("[OrdemAutomatica] Iniciando verificação de reposição agendada");
            verificarNecessidadeReposicao();
        } finally {
            verificacaoEmAndamento = false;
        }
    }

    /**
     * Verifica produtos que precisam de reposição e cria ordens automaticamente.
     * Pode ser chamado manualmente via endpoint ou pelo scheduler.
     * 
     * @return Resultado da verificação com detalhes
     */
    @Transactional
    public Map<String, Object> verificarNecessidadeReposicao() {
        logger.info("[OrdemAutomatica] Buscando produtos para reposição...");

        try {
            // Buscar produtos em estado crítico
            List<Produto> produtosCriticos = produtoService.findProdutosEstoqueCritico();

            if (produtosCriticos.isEmpty()) {
                logger.info("[OrdemAutomatica] Nenhum produto em estado crítico");
                ultimaVerificacao = LocalDate.now();
                return Map.of(
                        "success", true,
                        "message", "Nenhum produto precisa de reposição",
                        "produtosVerificados", produtoService.findAll().size(),
                        "produtosCriticos", 0,
                        "ordemCriada", false,
                        "dataVerificacao", LocalDate.now().toString());
            }

            logger.info("[OrdemAutomatica] {} produtos em estado crítico encontrados",
                    produtosCriticos.size());

            // Criar ordem de compra automática
            OrdemCompra ordemCriada = criarOrdemAutomatica(produtosCriticos);

            ultimaVerificacao = LocalDate.now();

            return Map.of(
                    "success", true,
                    "message", "Ordem de compra criada automaticamente",
                    "produtosVerificados", produtoService.findAll().size(),
                    "produtosCriticos", produtosCriticos.size(),
                    "ordemCriada", true,
                    "ordemId", ordemCriada.getId(),
                    "valorTotal", ordemCriada.getValor(),
                    "dataVerificacao", LocalDate.now().toString());

        } catch (Exception e) {
            logger.error("[OrdemAutomatica] Erro na verificação: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "Erro na verificação: " + e.getMessage(),
                    "dataVerificacao", LocalDate.now().toString());
        }
    }

    /**
     * Cria uma ordem de compra automática com os produtos que precisam reposição.
     * 
     * @param produtos Lista de produtos para incluir na ordem
     * @return Ordem de compra criada
     */
    @Transactional
    public OrdemCompra criarOrdemAutomatica(List<Produto> produtos) {
        logger.info("[OrdemAutomatica] Criando ordem para {} produtos", produtos.size());

        // Criar cabeçalho da ordem
        OrdemCompra ordem = new OrdemCompra();
        ordem.setStatusOrdemCompra(StatusOrdemCompra.PEND);
        ordem.setDataOrdem(LocalDate.now());
        ordem.setDataPrev(LocalDate.now().plusDays(diasParaEntrega));
        ordem.setValor(BigDecimal.ZERO); // Será calculado após adicionar itens

        // Salvar ordem primeiro para obter o ID
        OrdemCompra ordemSalva = ordemCompraService.create(ordem);
        logger.info("[OrdemAutomatica] Ordem #{} criada com sucesso", ordemSalva.getId());

        // Adicionar itens
        BigDecimal valorTotal = BigDecimal.ZERO;
        int itensAdicionados = 0;

        for (Produto produto : produtos) {
            try {
                // Calcular quantidade necessária para atingir o estoque máximo
                Integer estoqueAtual = getEstoqueAtualProduto(produto.getId());
                Integer quantidadeNecessaria = calcularQuantidadeReposicao(produto, estoqueAtual);

                if (quantidadeNecessaria > 0) {
                    ItemOrdemCompra item = new ItemOrdemCompra();
                    item.setIdOrdComp(ordemSalva.getId());
                    item.setIdProduto(produto.getId());
                    item.setQntd(quantidadeNecessaria);
                    item.setValor(BigDecimal.ZERO); // Preço será definido posteriormente
                    item.setDataVenc(LocalDate.now().plusMonths(12)); // 1 ano de validade padrão

                    // Calcular valor total do item
                    item.inicializarValorTotal();

                    itemOrdemCompraService.create(item);
                    itensAdicionados++;

                    logger.debug("[OrdemAutomatica] Item adicionado: Produto #{} - Qtd: {}",
                            produto.getId(), quantidadeNecessaria);
                }
            } catch (Exception e) {
                logger.warn("[OrdemAutomatica] Erro ao adicionar produto #{}: {}",
                        produto.getId(), e.getMessage());
            }
        }

        // Recalcular valor total da ordem
        valorTotal = itemOrdemCompraService.sumValorTotalByIdOrdemCompra(ordemSalva.getId());
        ordemSalva.setValor(valorTotal);
        ordemCompraService.update(ordemSalva);

        logger.info("[OrdemAutomatica] Ordem #{} finalizada: {} itens, valor total: R$ {}",
                ordemSalva.getId(), itensAdicionados, valorTotal);

        return ordemSalva;
    }

    /**
     * Obtém o estoque atual de um produto.
     * 
     * @param idProduto ID do produto
     * @return Quantidade atual em estoque
     */
    private Integer getEstoqueAtualProduto(Integer idProduto) {
        try {
            List<Estoque> estoques = estoqueService.findByIdProduto(idProduto);
            return estoques.stream()
                    .mapToInt(e -> e.getQtdEstoque() != null ? e.getQtdEstoque() : 0)
                    .sum();
        } catch (Exception e) {
            logger.warn("[OrdemAutomatica] Erro ao buscar estoque do produto {}: {}",
                    idProduto, e.getMessage());
            return 0;
        }
    }

    /**
     * Calcula a quantidade ideal de reposição.
     * Fórmula: (Estoque Máximo - Estoque Atual)
     * 
     * @param produto      Produto para calcular
     * @param estoqueAtual Quantidade atual em estoque
     * @return Quantidade a ser solicitada
     */
    private Integer calcularQuantidadeReposicao(Produto produto, Integer estoqueAtual) {
        if (produto.getStqMax() == null) {
            // Se não tem máximo definido, usar o dobro do mínimo
            return produto.getStqMin() != null ? produto.getStqMin() * 2 : 10;
        }

        int atual = estoqueAtual != null ? estoqueAtual : 0;
        int quantidade = produto.getStqMax() - atual;

        // Garantir quantidade mínima de 1
        return Math.max(quantidade, 1);
    }

    /**
     * Retorna estatísticas do serviço automático.
     * 
     * @return Mapa com estatísticas
     */
    public Map<String, Object> getEstatisticas() {
        List<Produto> criticos = produtoService.findProdutosEstoqueCritico();
        List<Produto> baixos = produtoService.findProdutosEstoqueBaixo();
        List<Produto> reposicao = produtoService.findProdutosParaReposicao();

        return Map.of(
                "servicoAtivo", ordemAutomaticaEnabled,
                "ultimaVerificacao", ultimaVerificacao != null ? ultimaVerificacao.toString() : "Nunca",
                "produtosCriticos", criticos.size(),
                "produtosEstoqueBaixo", baixos.size(),
                "produtosParaReposicao", reposicao.size(),
                "diasParaEntrega", diasParaEntrega);
    }

    /**
     * Força uma verificação manual imediata.
     * 
     * @return Resultado da verificação
     */
    public Map<String, Object> forcarVerificacao() {
        logger.info("[OrdemAutomatica] Verificação manual solicitada");
        return verificarNecessidadeReposicao();
    }
}

