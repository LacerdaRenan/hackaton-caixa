package br.com.hackathon.dao;

import br.com.hackathon.model.sqlserver.Produto;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import io.quarkus.hibernate.orm.PersistenceUnit;

import java.util.List;

@ApplicationScoped
public class ProdutoDao {

    @PersistenceUnit("sqlserver")
    EntityManager em;

    @CacheResult(cacheName = "produtos")
    public List<Produto> listarTodosProdutos() {
        return em.createQuery("FROM Produto", Produto.class).getResultList();
    }
}
