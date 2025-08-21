package br.com.hackathon.dto.telemetria;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TelemetriaDto {
    private LocalDate dataReferencia;
    private List<DadosTelemetriaDto> listaEndpoints;
}
