package br.com.hackathon.dao;

import br.com.hackathon.model.h2.Simulacao;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class SimulacaoDao {

    @PersistenceUnit("h2")
    EntityManager em;

    public List<Simulacao> listAll(Short pagina, Integer tamanhoPagina) {
        TypedQuery<Simulacao> query = em.createQuery("FROM Simulacao ORDER BY idSimulacao", Simulacao.class);
        query.setFirstResult((pagina-1)*tamanhoPagina);
        query.setMaxResults(tamanhoPagina);

        return query.getResultList();
    }

    public Long contarTotalRegistros() {
        return em.createQuery("SELECT COUNT(s) FROM Simulacao s", Long.class).getSingleResult();
    }

    public void save(Simulacao simulacao) {
        em.persist(simulacao);
    }
}
