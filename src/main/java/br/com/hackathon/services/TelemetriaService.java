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
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class TelemetriaService {

    @Inject
    TelemetriaDao telemetriaDao;

    public TelemetriaDto consultaDadosTelemetria(LocalDate data) {

        List<Telemetria> dadosTelemetria = telemetriaDao.buscarDadosApiPorData(data);

        Map<String, LongSummaryStatistics> dadosPorApi = dadosTelemetria.stream().collect(Collectors.groupingBy(
                Telemetria::getNomeApi,
                Collectors.summarizingLong(Telemetria::getDuracao)
        ));

        List<DadosTelemetriaDto> listaEndpoints = dadosPorApi.entrySet()
                .stream()
                .map(e -> DadosTelemetriaDto.builder()
                        .nomeApi(e.getKey())
                        .qtdRequisicoes(e.getValue().getCount())
                        .tempoMedio(e.getValue().getAverage())
                        .tempoMinimo(e.getValue().getMin())
                        .tempoMaximo(e.getValue().getMax())
                        .build())
                .toList();

        return TelemetriaDto.builder()
                .dataReferencia(data)
                .listaEndpoints(listaEndpoints)
                .build();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void registrarDadosApi(String nomeApi, Long duracao, Short statusResponse) {
        telemetriaDao.save(Telemetria.builder()
                        .nomeApi(nomeApi)
                        .duracao(duracao)
                        .statusCodeResponse(statusResponse)
                        .build());
    }
}
