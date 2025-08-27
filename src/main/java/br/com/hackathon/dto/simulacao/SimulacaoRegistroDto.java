package br.com.hackathon.dto.simulacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class SimulacaoRegistroDto {
    private Long idSimulacao;
    private BigDecimal valorDesejado;
    private Short prazo;
    private BigDecimal valorTotalParcelas;
}
