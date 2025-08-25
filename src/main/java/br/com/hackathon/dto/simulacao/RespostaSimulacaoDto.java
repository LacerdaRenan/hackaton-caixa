package br.com.hackathon.dto.simulacao;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RespostaSimulacaoDto {
    private Long idSimulacao;
    private Integer codigoProduto;
    private String descricaoProduto;
    private BigDecimal taxaJuros;
    private List<SimulacaoDto> resultadoSimulacao;
}
