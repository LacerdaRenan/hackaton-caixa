package br.com.hackathon.services;

import br.com.hackathon.dao.SimulacaoDao;
import br.com.hackathon.dto.CriarSimulacaoDto;
import br.com.hackathon.dto.PaginaConsultaDto;
import br.com.hackathon.dto.ParcelaDto;
import br.com.hackathon.dto.SimulacaoDto;
import br.com.hackathon.dto.volume_produto_simulacao.RespostaVolumeProdutoSimulacaoDto;
import br.com.hackathon.model.h2.Simulacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@ApplicationScoped
public class SimulacaoService {

    @Inject
    SimulacaoDao simulacaoDao;

    @Inject
    TelemetriaService telemetriaService;

    public PaginaConsultaDto<Simulacao> listarSimulacoes(Short pagina, Integer tamanhoPagina) {
        try {

            Long inicioProcessamentoTelemetria = System.nanoTime();

            List<Simulacao> simulacoesPaginadas = simulacaoDao.listAllPaginado(pagina, tamanhoPagina);
            Long totalRegistros = simulacaoDao.contarTotalRegistros();

            PaginaConsultaDto<Simulacao> consultaPaginada = new PaginaConsultaDto<>();

            consultaPaginada.setPagina(pagina);
            consultaPaginada.setQtdRegistros(totalRegistros);
            consultaPaginada.setQtdRegistrosPagina(simulacoesPaginadas.size());
            consultaPaginada.setRegistros(simulacoesPaginadas);

            Long fimProcessamentoTelemetria = System.nanoTime();
            Long duracaoProcessamentoTelemetria = (fimProcessamentoTelemetria - inicioProcessamentoTelemetria) / 1_000_000;

            telemetriaService.registrarDadosApi("Listar Simulacoes", duracaoProcessamentoTelemetria, (short) 200);

            return consultaPaginada;

        } catch (Exception e) {
            log.info("erro ao buscar simulacoes {}", e.getMessage());
            return new PaginaConsultaDto<>();
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    public void salvarSimulacao(CriarSimulacaoDto criarSimulacaoDto, SimulacaoDto simulacaoDto) {

        BigDecimal valorTotalParcelasSac = simulacaoDto.getParcelas()
                .stream()
                .map(ParcelaDto::getValorPrestacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try {
            simulacaoDao.save(Simulacao.builder()
                            .valorDesejado(criarSimulacaoDto.getValorDesejado())
                            .prazo(criarSimulacaoDto.getPrazo())
                            .valorTotalParcelas(valorTotalParcelasSac)
                            .build());
        } catch (Exception e) {
            log.info("erro ao salvar nova simulacao {}", e.getMessage());
        }
    }
}
