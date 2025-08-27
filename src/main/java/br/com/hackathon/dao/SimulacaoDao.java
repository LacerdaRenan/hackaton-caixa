package br.com.hackathon.dao;

import br.com.hackathon.dto.simulacao.SimulacaoRegistroDto;
import br.com.hackathon.model.mysql.Simulacao;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class SimulacaoDao {

    @PersistenceUnit("mysql")
    EntityManager em;

    public List<SimulacaoRegistroDto> listarPaginadas(Short pagina, Integer tamanhoPagina) {

        String sql = """
                SELECT
                    s.idSimulacao,
                    s.valorDesejado,
                    s.prazo,
                    s.valorTotalParcelas
                FROM
                    Simulacao s
                ORDER BY
                    s.idSimulacao
                """;

        TypedQuery<SimulacaoRegistroDto> query = em.createQuery(sql, SimulacaoRegistroDto.class);
        query.setFirstResult((pagina-1)*tamanhoPagina);
        query.setMaxResults(tamanhoPagina);

        return query.getResultList();
    }

    public List<Simulacao> listarPorData(LocalDate data) {

        String sql = """
                SELECT s
                FROM Simulacao s
                WHERE s.dataCriacao = :data
                """ ;

        TypedQuery<Simulacao> query = em.createQuery(sql, Simulacao.class);
        query.setParameter("data", data);

        return query.getResultList();
    }

    public Long contarTotalRegistros() {
        return em.createQuery("SELECT COUNT(s) FROM Simulacao s", Long.class).getSingleResult();
    }

    public Simulacao save(Simulacao simulacao) {
        em.persist(simulacao);
        return simulacao;
    }
}
