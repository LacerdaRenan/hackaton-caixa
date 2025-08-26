package br.com.hackathon.dto.volume_produto_simulacao;

import java.math.BigDecimal;

public record ValoresParciais(
        BigDecimal taxaMediaJuro,
        BigDecimal valorMedioPrestacao,
        BigDecimal valorTotalDesejado,
        BigDecimal valorTotalCredito
) {}
