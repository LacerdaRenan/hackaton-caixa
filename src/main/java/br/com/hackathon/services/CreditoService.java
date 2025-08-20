package br.com.hackathon.services;

import br.com.hackathon.dao.ProdutoDao;
import br.com.hackathon.dto.CriarSimulacaoDto;
import br.com.hackathon.dto.RespostaSimulacaoDto;
import br.com.hackathon.model.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
public class CreditoService {

    public RespostaSimulacaoDto novaSimulacao(CriarSimulacaoDto criarSimulacaoDto) {
        return RespostaSimulacaoDto.builder().build();
    }

    /**
     *  No sistema price, as parcelas são de mesmo valor
     */
    private void calcularFinanciamentoPrice(int tempo, double taxa, double total) {

        double fator = (Math.pow((1+taxa), tempo) - 1) / (Math.pow((1+taxa), tempo) * taxa);
        double parcela = total / fator;

    }
}
