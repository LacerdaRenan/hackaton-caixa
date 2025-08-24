package br.com.hackathon.dao;

import br.com.hackathon.dto.CriarSimulacaoDto;
import br.com.hackathon.model.sqlserver.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import io.quarkus.hibernate.orm.PersistenceUnit;

import java.util.List;

@ApplicationScoped
public class ProdutoDao {

    @PersistenceUnit("sqlserver")
    EntityManager em;

    public List<Produto> listAll() {
        return em.createQuery("FROM Produto", Produto.class).getResultList();
    }

    public Produto getById(Integer codigoProduto) {
        TypedQuery<Produto> query = em.createQuery("SELECT p FROM Produto p WHERE p.codigoProduto = :codigoProduto", Produto.class);
        query.setParameter("codigoProduto", codigoProduto);

        return query.getSingleResult();
    }

    public List<Produto> buscarProdutoPorParametros(CriarSimulacaoDto criarSimulacaoDto) {
        String sql = """
                SELECT p
                FROM Produto p
                WHERE   (:prazoDesejado >= p.numeroMinimoMeses OR p.numeroMinimoMeses IS NULL)
                AND     (:prazoDesejado <= p.numeroMaximoMeses OR p.numeroMaximoMeses IS NULL)
                AND     (:valorDesejado >= p.valorMinimo OR p.valorMinimo IS NULL)
                AND     (:valorDesejado <= p.valorMaximo OR p.valorMaximo IS NULL)
                """;

        TypedQuery<Produto> query = em.createQuery(sql, Produto.class);
        query.setParameter("prazoDesejado", criarSimulacaoDto.getPrazo());
        query.setParameter("valorDesejado", criarSimulacaoDto.getValorDesejado());

        return query.getResultList();
    }
}
