package br.com.hackathon.dto.volume_produto_simulacao;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RespostaVolumeProdutoSimulacaoDto {
    private LocalDate dataReferencia;
    private List<VolumeProdutoSimulacaoDto> simulacoes;
}
