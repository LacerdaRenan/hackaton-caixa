package br.com.hackathon.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DadosSimulacaoDto {
    private int pagina;
    private int qtdRegistros;
    private int qtdRegistrosPagina;
    private List<RegistroDto> registros;
}
