package br.com.hackathon.services;

import br.com.hackathon.dto.*;
import br.com.hackathon.enums.EnumTipoFinanciamento;
import br.com.hackathon.model.sqlserver.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class CreditoService {

    @Inject
    ProdutoService produtoService;

    @Inject
    SimulacaoService simulacaoService;

    public RespostaSimulacaoDto novaSimulacao(CriarSimulacaoDto criarSimulacaoDto) {

        ProdutoDto produtoDto = definirProduto(criarSimulacaoDto);
        log.info("produto selecionado : codigo {} nome {}", produtoDto.getCodigoProduto(), produtoDto.getNomeProduto());

        SimulacaoDto simulacaoSac = calcularFinanciamento(EnumTipoFinanciamento.SAC, criarSimulacaoDto, produtoDto);
        SimulacaoDto simulacaoPrice = calcularFinanciamento(EnumTipoFinanciamento.PRICE, criarSimulacaoDto, produtoDto);

        List<SimulacaoDto> resultadoSimulacao = new ArrayList<>();
        resultadoSimulacao.add(simulacaoSac);
        resultadoSimulacao.add(simulacaoPrice);

        simulacaoService.salvarSimulacao(criarSimulacaoDto, simulacaoSac);

        return RespostaSimulacaoDto.builder()
                .codigoProduto(produtoDto.getCodigoProduto())
                .descricaoProduto(produtoDto.getNomeProduto())
                .taxaJuros(produtoDto.getTaxaJuros().setScale(4, RoundingMode.HALF_UP))
                .resultadoSimulacao(resultadoSimulacao)
                .build();
    }

    private ProdutoDto definirProduto(CriarSimulacaoDto criarSimulacaoDto) {

        Short numeroParcelas = criarSimulacaoDto.getPrazo();
        BigDecimal valor = criarSimulacaoDto.getValorDesejado();

        List<Produto> produtos = produtoService.listarProdutos();

        for (Produto p : produtos) {

            Short numeroMinimoMeses = p.getNumeroMinimoMeses() == null ? 1 : p.getNumeroMinimoMeses();
            Short numeroMaximoMeses = p.getNumeroMaximoMeses() == null ? numeroParcelas : p.getNumeroMaximoMeses();
            BigDecimal valorMinimo = p.getValorMinimo() == null ? BigDecimal.ONE : p.getValorMinimo();
            BigDecimal valorMaximo = p.getValorMaximo() == null ? valor : p.getValorMaximo();

            if (numeroParcelas.compareTo(numeroMinimoMeses) >= 0
                    && numeroParcelas.compareTo(numeroMaximoMeses) <= 0
                    && valor.compareTo(valorMinimo) >= 0
                    && valor.compareTo(valorMaximo) <= 0)
                return ProdutoDto.builder()
                        .codigoProduto(p.getCodigoProduto())
                        .nomeProduto(p.getNomeProduto())
                        .taxaJuros(p.getTaxaJuros())
                        .build();
        }

        log.info("Nenhum produto encontrado para os parametros fornecidos");

        return ProdutoDto.builder()
                .taxaJuros(new BigDecimal("0.0179"))
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
        MathContext mathContext = new MathContext(10, RoundingMode.HALF_UP);

        BigDecimal amortizacao = valor.divide(BigDecimal.valueOf(numeroParcelas), mathContext);
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

    private BigDecimal calularParcelaBrutaPrice(BigDecimal valor, Short numeroParcelas, BigDecimal taxaJuros) {
        MathContext mathContext = new MathContext(10, RoundingMode.HALF_UP);

        BigDecimal potenciaTaxaMaisUm = BigDecimal.ONE.add(taxaJuros).pow(numeroParcelas);
        BigDecimal numeradorFator = potenciaTaxaMaisUm.subtract(BigDecimal.ONE);
        BigDecimal denominadorFator = potenciaTaxaMaisUm.multiply(taxaJuros);
        BigDecimal fator = numeradorFator.divide(denominadorFator, mathContext);

        return valor.divide(fator, mathContext);
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

    private BigDecimal calculaValorAmortizacaoParcelaPrice(BigDecimal amortizacaoPrimeiraParcela, BigDecimal taxaJuros, Integer parcelaAtual) {
        return BigDecimal.ONE.add(taxaJuros).pow(parcelaAtual-1).multiply(amortizacaoPrimeiraParcela);
    }
}
