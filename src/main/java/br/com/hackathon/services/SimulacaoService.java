package br.com.hackathon.services;

import br.com.hackathon.dao.SimulacaoDao;
import br.com.hackathon.dto.*;
import br.com.hackathon.dto.simulacao.ParcelaDto;
import br.com.hackathon.dto.simulacao.SimulacaoDto;
import br.com.hackathon.dto.volume_produto_simulacao.RespostaVolumeProdutoSimulacaoDto;
import br.com.hackathon.dto.volume_produto_simulacao.VolumeProdutoSimulacaoDto;
import br.com.hackathon.model.h2.Simulacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@ApplicationScoped
public class SimulacaoService {

    @Inject
    SimulacaoDao simulacaoDao;

    @Inject
    TelemetriaService telemetriaService;

    @Inject
    ProdutoService produtoService;

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

    public RespostaVolumeProdutoSimulacaoDto calcularVolumeSimuladoProduto(Integer codigoProduto, LocalDate data) {

        List<Simulacao> simulacoesProdutoPorDia = simulacaoDao.listAllByCodigoProduto(codigoProduto, data);
        ProdutoDto produtoDto = produtoService.buscarProdutoPorId(codigoProduto);

        BigDecimal valorTotalCredito = BigDecimal.ZERO;
        BigDecimal valorTotalDesejado = BigDecimal.ZERO;

        for (Simulacao s : simulacoesProdutoPorDia) {
            valorTotalDesejado = valorTotalDesejado.add(s.getValorDesejado());
            valorTotalCredito =  valorTotalCredito.add(s.getValorTotalParcelas());
        }

        MathContext mathContext = new MathContext(10, RoundingMode.HALF_UP);
        BigDecimal valorMedioPrestacao = valorTotalCredito.divide(BigDecimal.valueOf(simulacoesProdutoPorDia.size()), mathContext);

        return RespostaVolumeProdutoSimulacaoDto.builder()
                .dataReferencia(data)
                .simulacoes(Collections.singletonList(VolumeProdutoSimulacaoDto.builder()
                        .codigoProduto(produtoDto.getCodigoProduto())
                        .descricaoProduto(produtoDto.getNomeProduto())
                        .taxaMediaJuro(produtoDto.getTaxaJuros().setScale(4, RoundingMode.HALF_UP))
                        .valorMedioPrestacao(valorMedioPrestacao.setScale(2, RoundingMode.HALF_UP))
                        .valorTotalDesejado(valorTotalDesejado.setScale(2, RoundingMode.HALF_UP))
                        .valorTotalCredito(valorTotalCredito.setScale(2, RoundingMode.HALF_UP))
                        .build()))
                .build();
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    public void salvarSimulacao(CriarSimulacaoDto criarSimulacaoDto, SimulacaoDto simulacaoDto, ProdutoDto produtoDto) {

        BigDecimal valorTotalParcelasSac = simulacaoDto.getParcelas()
                .stream()
                .map(ParcelaDto::getValorPrestacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try {
            simulacaoDao.save(Simulacao.builder()
                            .codigoProduto(produtoDto.getCodigoProduto())
                            .valorDesejado(criarSimulacaoDto.getValorDesejado())
                            .prazo(criarSimulacaoDto.getPrazo())
                            .valorTotalParcelas(valorTotalParcelasSac)
                            .build());
        } catch (Exception e) {
            log.info("erro ao salvar nova simulacao {}", e.getMessage());
        }
    }
}
