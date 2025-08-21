package br.com.hackathon.dao;

import br.com.hackathon.model.h2.Simulacao;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class SimulacaoDao {

    @PersistenceUnit("h2")
    EntityManager em;

    public List<Simulacao> listAll() {
        return em.createQuery("FROM Simulacao", Simulacao.class).getResultList();
    }
}
