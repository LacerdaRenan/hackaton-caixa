package br.com.hackathon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CriarSimulacaoDto {

    @NotNull
    private BigDecimal valorDesejado;

    @NotNull
    @Min(1)
    private Short prazo;
}
