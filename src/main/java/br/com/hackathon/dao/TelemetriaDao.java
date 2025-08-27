package br.com.hackathon.dao;

import br.com.hackathon.dto.telemetria.DadosTelemetriaDto;
import br.com.hackathon.model.mysql.Telemetria;
import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TelemetriaDao {

    @PersistenceUnit("mysql")
    EntityManager em;

    public List<DadosTelemetriaDto> buscarDadosTelemetriaData(LocalDate data) {
        String sql = """
                SELECT
                    t.nomeApi,
                    COUNT(*) AS qtdRequisicoes,
                    AVG(t.duracao) AS tempoMedio,
                    MIN(t.duracao) AS tempoMinimo,
                    MAX(t.duracao) AS tempoMaximo,
                    (SUM(CASE WHEN t.statusCodeResponse >= 200 AND t.statusCodeResponse < 300 THEN 1 ELSE 0 END) * 1.0 / COUNT(*)) AS percentualSucesso
                FROM
                    Telemetria t
                WHERE
                    t.dataCriacao = :data
                GROUP BY
                    t.nomeApi
                """;

        TypedQuery<DadosTelemetriaDto> query = em.createQuery(sql, DadosTelemetriaDto.class);
        query.setParameter("data", data);

        return query.getResultList();
    }

    public void save(Telemetria dadosApi) {
        em.persist(dadosApi);
    }
}
