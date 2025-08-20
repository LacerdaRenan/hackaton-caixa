package br.com.hackathon.dto;

import br.com.hackathon.enums.EnumTipoFinanciamento;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SimulacaoDto {
    private EnumTipoFinanciamento tipo;
    private List<ParcelaDto> parcelas;
}
