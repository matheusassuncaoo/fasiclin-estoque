package com.br.fasipe.estoque.ordemcompra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.br.fasipe.estoque.ordemcompra.models.Produto;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    @Query("SELECT p FROM Produto p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    @QueryHints({
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Produto> findByNomeContaining(@Param("nome") String nome);

    @Query("SELECT p FROM Produto p WHERE LOWER(p.nome) = LOWER(:nome)")
    @QueryHints({
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Produto> findByNome(@Param("nome") String nome);

    @Query("SELECT DISTINCT p FROM Produto p JOIN Estoque e ON p.id = e.idProduto WHERE e.qtdEstoque <= p.pntPedido ORDER BY p.nome")
    @QueryHints({
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Produto> findProdutosParaReposicao();

    @Query("SELECT DISTINCT p FROM Produto p JOIN Estoque e ON p.id = e.idProduto WHERE e.qtdEstoque > p.pntPedido AND e.qtdEstoque <= p.stqMin ORDER BY p.nome")
    @QueryHints({
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Produto> findProdutosEstoqueBaixo();

    @Query("SELECT DISTINCT p FROM Produto p JOIN Estoque e ON p.id = e.idProduto WHERE e.qtdEstoque <= p.pntPedido ORDER BY e.qtdEstoque ASC, p.nome")
    @QueryHints({
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    List<Produto> findProdutosEstoqueCritico();

    @Query("SELECT p FROM Produto p WHERE p.codBarras = :codBarras")
    @QueryHints({
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.fetchSize", value = "50")
    })
    Optional<Produto> findByCodigoBarras(@Param("codBarras") String codBarras);

    @Query("SELECT p FROM Produto p WHERE LOWER(p.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))")
    List<Produto> findByDescricaoContaining(@Param("descricao") String descricao);

}