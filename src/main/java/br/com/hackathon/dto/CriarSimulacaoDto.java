package br.com.hackathon.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CriarSimulacaoDto {
    @NotNull private BigDecimal valorDesejado;
    @NotNull private Integer prazo;
}
