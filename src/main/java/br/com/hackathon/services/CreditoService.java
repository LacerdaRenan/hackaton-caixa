package br.com.hackathon.services;

import br.com.hackathon.dao.ProdutoDao;
import br.com.hackathon.dto.*;
import br.com.hackathon.enums.EnumTipoFinanciamento;
import br.com.hackathon.model.Produto;
import br.com.hackathon.resources.SimuladorResource;
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

    public RespostaSimulacaoDto novaSimulacao(CriarSimulacaoDto criarSimulacaoDto) {

        ProdutoDto mockProdutoDto = ProdutoDto.builder()
                .taxaJuros(new BigDecimal("0.0179"))
                .build();

        SimulacaoDto simulacaoPrice = calcularFinanciamentoPrice(criarSimulacaoDto, mockProdutoDto);
        List<SimulacaoDto> resultadoSimulacao = new ArrayList<>();
        resultadoSimulacao.add(simulacaoPrice);

        return RespostaSimulacaoDto.builder()
                .resultadoSimulacao(resultadoSimulacao)
                .build();
    }

    private SimulacaoDto calcularFinanciamentoPrice(CriarSimulacaoDto criarSimulacaoDto, ProdutoDto produtoDto) {

        Integer numeroParcelas = criarSimulacaoDto.getPrazo();
        BigDecimal valor = criarSimulacaoDto.getValorDesejado();
        BigDecimal taxaJuros = produtoDto.getTaxaJuros();

        List<ParcelaDto> parcelaDtoList = calcularDadosParcelaPrice(valor, numeroParcelas, taxaJuros);

        return SimulacaoDto.builder()
                .tipo(EnumTipoFinanciamento.PRICE.name())
                .parcelas(parcelaDtoList)
                .build();
    }

    private BigDecimal calularParcelaBrutaPrice(BigDecimal valor, Integer numeroParcelas, BigDecimal taxaJuros) {
        MathContext mathContext = new MathContext(10, RoundingMode.HALF_UP);

        BigDecimal potenciaTaxaMaisUm = BigDecimal.ONE.add(taxaJuros).pow(numeroParcelas);
        BigDecimal numeradorFator = potenciaTaxaMaisUm.subtract(BigDecimal.ONE);
        BigDecimal denominadorFator = potenciaTaxaMaisUm.multiply(taxaJuros);
        BigDecimal fator = numeradorFator.divide(denominadorFator, mathContext);

        return valor.divide(fator, mathContext);
    }

    private List<ParcelaDto> calcularDadosParcelaPrice(BigDecimal valor, Integer numeroParcelas, BigDecimal taxaJuros) {
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
