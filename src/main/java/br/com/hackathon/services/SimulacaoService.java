package br.com.hackathon.services;

import br.com.hackathon.api.payload.PaginaPayload;
import br.com.hackathon.dao.SimulacaoDao;
import br.com.hackathon.dto.*;
import br.com.hackathon.dto.simulacao.*;
import br.com.hackathon.dto.volume_produto_simulacao.RespostaVolumeProdutoSimulacaoDto;
import br.com.hackathon.dto.volume_produto_simulacao.VolumeProdutoSimulacaoDto;
import br.com.hackathon.enums.EnumTipoFinanciamento;
import br.com.hackathon.model.mysql.Simulacao;
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
    private ProdutoService produtoService;

    private final MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

    public RespostaSimulacaoDto criarSimulacao(CriarSimulacaoDto criarSimulacaoDto) {

        log.info("Dados simulacao {}", criarSimulacaoDto);

        ProdutoDto produtoDto = produtoService.buscarProdutoPorParametro(criarSimulacaoDto);
        log.info("produto selecionado: {}", produtoDto);

        SimulacaoDto simulacaoSac = calcularFinanciamento(EnumTipoFinanciamento.SAC, criarSimulacaoDto, produtoDto);
        SimulacaoDto simulacaoPrice = calcularFinanciamento(EnumTipoFinanciamento.PRICE, criarSimulacaoDto, produtoDto);

        List<SimulacaoDto> resultadoSimulacao = new ArrayList<>();
        resultadoSimulacao.add(simulacaoSac);
        resultadoSimulacao.add(simulacaoPrice);

        Simulacao simulacao = salvarSimulacao(criarSimulacaoDto, simulacaoSac, produtoDto);

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

    public PaginaPayload<SimulacaoRegistroDto> listarSimulacoesPaginadas(Short pagina, Integer tamanhoPagina) {

        List<SimulacaoRegistroDto> simulacoesPaginadas = simulacaoDao.listarPaginadas(pagina, tamanhoPagina);

        Long totalRegistros = simulacaoDao.contarTotalRegistros();

        PaginaPayload<SimulacaoRegistroDto> paginaConsulta = new PaginaPayload<>();

        paginaConsulta.setPagina(pagina);
        paginaConsulta.setQtdRegistros(totalRegistros);
        paginaConsulta.setQtdRegistrosPagina(simulacoesPaginadas.size());
        paginaConsulta.setRegistros(simulacoesPaginadas);

        return paginaConsulta;
    }

    public RespostaVolumeProdutoSimulacaoDto calcularVolumeSimuladoData(LocalDate data) {

        List<Simulacao> simulacoesProdutoPorDia = simulacaoDao.listarPorData(data);

        log.info("volume simulado na data {}: {}", data, simulacoesProdutoPorDia.size());

        Map<Integer, VolumeProdutoSimulacaoDto> mapVolumeSimulacoes = processaMapReduceSimulacoes(simulacoesProdutoPorDia);
        List<VolumeProdutoSimulacaoDto> simulacoes = mapToVolumeProdutoSimulacaoDto(mapVolumeSimulacoes);

        return RespostaVolumeProdutoSimulacaoDto.builder()
                .dataReferencia(data)
                .simulacoes(simulacoes)
                .build();
    }

    private Map<Integer, VolumeProdutoSimulacaoDto> processaMapReduceSimulacoes(List<Simulacao> simulacoes) {
        return simulacoes.stream()
                .collect(Collectors.toMap(
                        Simulacao::getCodigoProduto,
                        s -> new VolumeProdutoSimulacaoDto(
                                s.getCodigoProduto(),
                                s.getDescricaoProduto(),
                                s.getTaxaJuros(),
                                s.getValorTotalParcelas().divide(BigDecimal.valueOf(s.getPrazo()), mc),
                                s.getValorDesejado(),
                                s.getValorTotalParcelas()
                        ),
                        (v1, v2) -> new VolumeProdutoSimulacaoDto(
                                v1.getCodigoProduto(),
                                v1.getDescricaoProduto(),
                                (v1.getTaxaMediaJuro().add(v2.getTaxaMediaJuro())).divide(BigDecimal.TWO, mc),
                                (v1.getValorMedioPrestacao().add(v2.getValorMedioPrestacao())).divide(BigDecimal.TWO, mc),
                                v1.getValorTotalDesejado().add(v2.getValorTotalDesejado()),
                                v1.getValorTotalCredito().add(v2.getValorTotalCredito())
                        )
                ));
    }

    private List<VolumeProdutoSimulacaoDto> mapToVolumeProdutoSimulacaoDto(Map<Integer, VolumeProdutoSimulacaoDto> mapVolumeSimulacoes) {
        mapVolumeSimulacoes.values()
                .forEach(m -> {
                    m.setTaxaMediaJuro(m.getTaxaMediaJuro().setScale(4, RoundingMode.HALF_UP));
                    m.setValorMedioPrestacao(m.getValorMedioPrestacao().setScale(2, RoundingMode.HALF_UP));
                    m.setValorTotalDesejado(m.getValorTotalDesejado().setScale(2, RoundingMode.HALF_UP));
                    m.setValorTotalCredito(m.getValorTotalCredito().setScale(2, RoundingMode.HALF_UP));
                });
         return mapVolumeSimulacoes.values().stream().toList();
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    public Simulacao salvarSimulacao(CriarSimulacaoDto criarSimulacaoDto, SimulacaoDto simulacaoDto, ProdutoDto produtoDto) {

        BigDecimal valorTotalParcelasSac = simulacaoDto.getParcelas()
                .stream()
                .map(ParcelaDto::getValorPrestacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return simulacaoDao.save(Simulacao.builder()
                        .codigoProduto(produtoDto.getCodigoProduto())
                        .descricaoProduto(produtoDto.getNomeProduto())
                        .taxaJuros(produtoDto.getTaxaJuros())
                        .valorDesejado(criarSimulacaoDto.getValorDesejado())
                        .prazo(criarSimulacaoDto.getPrazo())
                        .valorTotalParcelas(valorTotalParcelasSac)
                        .build());
    }
}
