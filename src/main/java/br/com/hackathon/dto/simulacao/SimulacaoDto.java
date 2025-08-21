package br.com.hackathon.dto.simulacao;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SimulacaoDto {
    private String tipo;
    private List<ParcelaDto> parcelas;
}
