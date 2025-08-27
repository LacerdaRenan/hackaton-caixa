package br.com.hackathon.dto.telemetria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DadosTelemetriaDto {
    private String nomeApi;
    private Long qtdRequisicoes;
    private Double tempoMedio;
    private Long tempoMinimo;
    private Long tempoMaximo;
    private Double percentualSucesso;
}
