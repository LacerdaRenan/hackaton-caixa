package br.com.hackathon.services;

import br.com.hackathon.api.payload.PaginaPayload;
import br.com.hackathon.dao.SimulacaoDao;
import br.com.hackathon.dto.*;
import br.com.hackathon.dto.simulacao.CriarSimulacaoDto;
import br.com.hackathon.dto.simulacao.ParcelaDto;
import br.com.hackathon.dto.simulacao.RespostaSimulacaoDto;
import br.com.hackathon.dto.simulacao.SimulacaoDto;
import br.com.hackathon.dto.volume_produto_simulacao.RespostaVolumeProdutoSimulacaoDto;
import br.com.hackathon.dto.volume_produto_simulacao.ValoresParciais;
import br.com.hackathon.dto.volume_produto_simulacao.VolumeProdutoSimulacaoDto;
import br.com.hackathon.enums.EnumTipoFinanciamento;
import br.com.hackathon.model.h2.Simulacao;
import br.com.hackathon.model.sqlserver.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class SimulacaoService {

    @Inject
    private SimulacaoDao simulacaoDao;

    @Inject
    private TelemetriaService telemetriaService;

    @Inject
    private ProdutoService produtoService;

    private final MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

    public RespostaSimulacaoDto criarSimulacao(CriarSimulacaoDto criarSimulacaoDto) {

        log.info("Dados simulacao {}", criarSimulacaoDto);

        Long inicioProcessamentoTelemetria = System.nanoTime();

        ProdutoDto produtoDto = produtoService.buscarProdutoPorParametro(criarSimulacaoDto);
        log.info("produto selecionado: {}", produtoDto);

        SimulacaoDto simulacaoSac = calcularFinanciamento(EnumTipoFinanciamento.SAC, criarSimulacaoDto, produtoDto);
        SimulacaoDto simulacaoPrice = calcularFinanciamento(EnumTipoFinanciamento.PRICE, criarSimulacaoDto, produtoDto);

        List<SimulacaoDto> resultadoSimulacao = new ArrayList<>();
        resultadoSimulacao.add(simulacaoSac);
        resultadoSimulacao.add(simulacaoPrice);

        Simulacao simulacao = salvarSimulacao(criarSimulacaoDto, simulacaoSac, produtoDto);

        Long fimProcessamentoTelemetria = System.nanoTime();
        Long duracaoProcessamentoTelemetria = (fimProcessamentoTelemetria - inicioProcessamentoTelemetria) / 1_000_000;

        telemetriaService.registrarDadosApi("Simulacao Credito", duracaoProcessamentoTelemetria, (short) 201);

        return RespostaSimulacaoDto.builder()
                .idSimulacao(simulacao.getIdSimulacao())
                .codigoProduto(produtoDto.getCodigoProduto())
                .descricaoProduto(produtoDto.getNomeProduto())
                .taxaJuros(produtoDto.getTaxaJuros().setScale(4, RoundingMode.HALF_UP))
                .resultadoSimulacao(resultadoSimulacao)
                .build();
    }

    private SimulacaoDto calcularFinanciamento(EnumTipoFinanciamento tipoFinanciamento, CriarSimulacaoDto criarSimulacaoDto, ProdutoDto produtoDto) {

        Short numeroParcelas = criarSimulacaoDto.getPrazo();
        BigDecimal valor = criarSimulacaoDto.getValorDesejado();
        BigDecimal taxaJuros = produtoDto.getTaxaJuros();

        List<ParcelaDto> parcelaDtoList = EnumTipoFinanciamento.SAC.equals(tipoFinanciamento)
                ? calcularDadosParcelaSac(valor, numeroParcelas, taxaJuros)
                : calcularDadosParcelaPrice(valor, numeroParcelas, taxaJuros);

        return SimulacaoDto.builder()
                .tipo(tipoFinanciamento.name())
                .parcelas(parcelaDtoList)
                .build();
    }

    private List<ParcelaDto> calcularDadosParcelaSac(BigDecimal valor, Short numeroParcelas, BigDecimal taxaJuros) {

        BigDecimal amortizacao = valor.divide(BigDecimal.valueOf(numeroParcelas), mc);
        BigDecimal jurosPrimeiraParcela = valor.multiply(taxaJuros);
        BigDecimal primeiraParcela = amortizacao.add(jurosPrimeiraParcela);
        BigDecimal constanteReducao = amortizacao.multiply(taxaJuros);

        List<ParcelaDto> parcelaDtoList = new ArrayList<>();

        for (int i=1; i<=numeroParcelas; i++) {

            BigDecimal parcelaSac = calcularValorParcelaSac(primeiraParcela, constanteReducao, i);
            BigDecimal jurosParcela = parcelaSac.subtract(amortizacao);

            parcelaDtoList.add(ParcelaDto.builder()
                    .numero(i)
                    .valorAmortizacao(amortizacao.setScale(2, RoundingMode.HALF_UP))
                    .valorJuros(jurosParcela.setScale(2, RoundingMode.HALF_UP))
                    .valorPrestacao(parcelaSac.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return parcelaDtoList;
    }

    private BigDecimal calcularValorParcelaSac(BigDecimal primeiraParcela, BigDecimal constanteReducao, Integer parcelaAtual) {
        BigDecimal diferenca = (BigDecimal.valueOf(parcelaAtual).subtract(BigDecimal.ONE)).multiply(constanteReducao);
        return primeiraParcela.subtract(diferenca);
    }

    private List<ParcelaDto> calcularDadosParcelaPrice(BigDecimal valor, Short numeroParcelas, BigDecimal taxaJuros) {
        BigDecimal parcelaBrutaPrice = calularParcelaBrutaPrice(valor, numeroParcelas, taxaJuros);

        BigDecimal jurosPrimeiraParcela = valor.multiply(taxaJuros);
        BigDecimal amortizacaoPrimeiraParcela = parcelaBrutaPrice.subtract(jurosPrimeiraParcela);

        List<ParcelaDto> parcelasDtoPrice = new ArrayList<>();

        for(int i=1; i<=numeroParcelas; i++) {

            BigDecimal amortizacaParcela = calculaValorAmortizacaoParcelaPrice(amortizacaoPrimeiraParcela, taxaJuros, i);
            BigDecimal jurosParcela = parcelaBrutaPrice.subtract(amortizacaParcela);

            parcelasDtoPrice.add(ParcelaDto.builder()
                    .numero(i)
                    .valorAmortizacao(amortizacaParcela.setScale(2, RoundingMode.HALF_UP))
                    .valorJuros(jurosParcela.setScale(2, RoundingMode.HALF_UP))
                    .valorPrestacao(parcelaBrutaPrice.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return parcelasDtoPrice;
    }

    private BigDecimal calularParcelaBrutaPrice(BigDecimal valor, Short numeroParcelas, BigDecimal taxaJuros) {

        BigDecimal potenciaTaxaMaisUm = BigDecimal.ONE.add(taxaJuros).pow(numeroParcelas);
        BigDecimal numeradorFator = potenciaTaxaMaisUm.subtract(BigDecimal.ONE);
        BigDecimal denominadorFator = potenciaTaxaMaisUm.multiply(taxaJuros);
        BigDecimal fator = numeradorFator.divide(denominadorFator, mc);

        return valor.divide(fator, mc);
    }

    private BigDecimal calculaValorAmortizacaoParcelaPrice(BigDecimal amortizacaoPrimeiraParcela, BigDecimal taxaJuros, Integer parcelaAtual) {
        return BigDecimal.ONE.add(taxaJuros).pow(parcelaAtual-1).multiply(amortizacaoPrimeiraParcela);
    }

    public PaginaPayload<Simulacao> listarSimulacoesPaginadas(Short pagina, Integer tamanhoPagina) {

        Long inicioProcessamentoTelemetria = System.nanoTime();

        List<Simulacao> simulacoesPaginadas = simulacaoDao.listarPaginadas(pagina, tamanhoPagina);
        Long totalRegistros = simulacaoDao.contarTotalRegistros();

        PaginaPayload<Simulacao> paginaConsulta = new PaginaPayload<>();

        paginaConsulta.setPagina(pagina);
        paginaConsulta.setQtdRegistros(totalRegistros);
        paginaConsulta.setQtdRegistrosPagina(simulacoesPaginadas.size());
        paginaConsulta.setRegistros(simulacoesPaginadas);

        Long fimProcessamentoTelemetria = System.nanoTime();
        Long duracaoProcessamentoTelemetria = (fimProcessamentoTelemetria - inicioProcessamentoTelemetria) / 1_000_000;

        telemetriaService.registrarDadosApi("Listar Simulacoes", duracaoProcessamentoTelemetria, (short) 200);

        return paginaConsulta;
    }

    public RespostaVolumeProdutoSimulacaoDto calcularVolumeSimuladoData(LocalDate data) {

        List<Simulacao> simulacoesProdutoPorDia = simulacaoDao.listarPorData(data);

        log.info("volume simulado na data {}: {}", data, simulacoesProdutoPorDia.size());

        Map<Integer, ValoresParciais> mapValoresParciais = processaMapReduceSimulacoes(simulacoesProdutoPorDia);
        List<VolumeProdutoSimulacaoDto> simulacoes = mapToVolumeProdutoSimulacaoDto(mapValoresParciais);

        return RespostaVolumeProdutoSimulacaoDto.builder()
                .dataReferencia(data)
                .simulacoes(simulacoes)
                .build();
    }

    private Map<Integer, ValoresParciais> processaMapReduceSimulacoes(List<Simulacao> simulacoes) {
        return simulacoes.stream()
                .collect(Collectors.toMap(
                        Simulacao::getCodigoProduto,
                        s -> new ValoresParciais(
                                s.getValorTotalParcelas().divide(s.getValorDesejado().multiply(BigDecimal.valueOf(s.getPrazo())), mc),
                                s.getValorTotalParcelas().divide(BigDecimal.valueOf(s.getPrazo()), mc),
                                s.getValorDesejado(),
                                s.getValorTotalParcelas()
                        ),
                        (v1, v2) -> new ValoresParciais(
                                (v1.taxaMediaJuro().add(v2.taxaMediaJuro())).divide(BigDecimal.TWO, mc),
                                (v1.valorMedioPrestacao().add(v2.valorMedioPrestacao())).divide(BigDecimal.TWO, mc),
                                v1.valorTotalDesejado().add(v2.valorTotalDesejado()),
                                v1.valorTotalCredito().add(v2.valorTotalCredito())
                        )
                ));
    }

    private List<VolumeProdutoSimulacaoDto> mapToVolumeProdutoSimulacaoDto(Map<Integer, ValoresParciais> mapValoresParciais) {
        List<VolumeProdutoSimulacaoDto> volumeProdutoSimulacaoDtos = new ArrayList<>();
        List<Produto> produtos = produtoService.listarProdutos();

        for (Map.Entry<Integer, ValoresParciais> parciaisEntry : mapValoresParciais.entrySet()) {
            Produto produto = produtos.stream()
                    .filter(p -> p.getCodigoProduto().equals(parciaisEntry.getKey()))
                    .findFirst()
                    .orElse(new Produto());

            volumeProdutoSimulacaoDtos.add(
                    VolumeProdutoSimulacaoDto.builder()
                            .codigoProduto(parciaisEntry.getKey())
                            .descricaoProduto(produto.getNomeProduto())
                            .taxaMediaJuro(parciaisEntry.getValue().taxaMediaJuro().setScale(3, RoundingMode.HALF_UP))
                            .valorMedioPrestacao(parciaisEntry.getValue().valorMedioPrestacao().setScale(2, RoundingMode.HALF_UP))
                            .valorTotalDesejado(parciaisEntry.getValue().valorTotalDesejado())
                            .valorTotalCredito(parciaisEntry.getValue().valorTotalCredito())
                            .build()
            );
        }
        return volumeProdutoSimulacaoDtos;
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    public Simulacao salvarSimulacao(CriarSimulacaoDto criarSimulacaoDto, SimulacaoDto simulacaoDto, ProdutoDto produtoDto) {

        BigDecimal valorTotalParcelasSac = simulacaoDto.getParcelas()
                .stream()
                .map(ParcelaDto::getValorPrestacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return simulacaoDao.save(Simulacao.builder()
                        .codigoProduto(produtoDto.getCodigoProduto())
                        .valorDesejado(criarSimulacaoDto.getValorDesejado())
                        .prazo(criarSimulacaoDto.getPrazo())
                        .valorTotalParcelas(valorTotalParcelasSac)
                        .build());
    }
}
