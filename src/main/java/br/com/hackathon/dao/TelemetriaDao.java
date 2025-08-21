package br.com.hackathon.dao;

import br.com.hackathon.model.h2.Telemetria;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TelemetriaDao {

    @PersistenceUnit("h2")
    EntityManager em;

    public List<Telemetria> buscarDadosApiPorData(LocalDate data) {
        TypedQuery<Telemetria> query = em.createQuery("SELECT d FROM Telemetria d WHERE d.dataCriacao = :data", Telemetria.class);
        query.setParameter("data", data);

        return query.getResultList();
    }

    public void save(Telemetria dadosApi) {
        em.persist(dadosApi);
    }
}
