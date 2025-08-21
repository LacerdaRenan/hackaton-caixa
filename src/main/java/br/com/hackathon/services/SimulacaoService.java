package br.com.hackathon.services;

import br.com.hackathon.dao.SimulacaoDao;
import br.com.hackathon.model.h2.Simulacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class SimulacaoService {

    @Inject
    SimulacaoDao simulacaoDao;

    public List<Simulacao> listarSimulacoes() {
        try {
            return simulacaoDao.listAll();
        } catch (Exception e) {
            log.info("erro ao buscas simulacoes");
            return new ArrayList<>();
        }
    }
}
