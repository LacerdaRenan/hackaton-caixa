package br.com.hackathon.dto.telemetria;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DadosTelemetriaDto {
    private String nomeApi;
    private Long qtdRequisicoes;
    private Double tempoMedio;
    private Long tempoMinimo;
    private Long tempoMaximo;
    private Float percentualSucesso;
}
