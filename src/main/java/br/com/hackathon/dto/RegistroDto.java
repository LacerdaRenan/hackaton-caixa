package br.com.hackathon.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RegistroDto {
    private Long idSimulacao;
    private BigDecimal valorDesejado;
    private Integer prazo;
    private BigDecimal valorTotalParcelas;
}
