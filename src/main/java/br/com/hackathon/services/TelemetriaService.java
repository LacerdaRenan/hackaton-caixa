package br.com.hackathon.services;

import br.com.hackathon.dao.TelemetriaDao;
import br.com.hackathon.dto.telemetria.DadosTelemetriaDto;
import br.com.hackathon.dto.telemetria.TelemetriaDto;
import br.com.hackathon.model.h2.Telemetria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TelemetriaService {

    @Inject
    TelemetriaDao telemetriaDao;

    public TelemetriaDto consultaDadosTelemetria(LocalDate data) {
        List<DadosTelemetriaDto> dadosTelemetriaDtos = telemetriaDao.buscarDadosTelemetriaData(data);

        return TelemetriaDto.builder()
                .dataReferencia(data)
                .listaEndpoints(dadosTelemetriaDtos)
                .build();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void registrarDadosApi(String nomeApi, Long duracao, Integer statusResponse) {
        telemetriaDao.save(Telemetria.builder()
                        .nomeApi(nomeApi)
                        .duracao(duracao)
                        .statusCodeResponse(statusResponse)
                        .build());
    }
}
