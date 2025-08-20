package br.com.hackathon.dao;

import br.com.hackathon.model.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class ProdutoDao {

    @Inject
    EntityManager em;

    public List<Produto> listAll() {
        return em.createQuery("FROM Produto", Produto.class).getResultList();
    }

    public Produto getById(Integer id) {
        TypedQuery<Produto> query = em.createQuery("SELECT p.* FROM Produto WHERE p.CO_PRODUTO = :idProduto", Produto.class);
        query.setParameter("idProduto", id);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
