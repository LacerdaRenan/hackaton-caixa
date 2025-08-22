package br.com.hackathon.dao;

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
}
