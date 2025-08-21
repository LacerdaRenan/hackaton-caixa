package br.com.hackathon.dto.volume_produto_simulacao;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RespostaVolumeProdutoSimulacaoDto {
    private LocalDateTime dataReferencia;
    private List<VolumeProdutoSimulacaoDto> simulacoes;
}
