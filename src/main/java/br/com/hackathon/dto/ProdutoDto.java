package br.com.hackathon.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProdutoDto {
    private Integer codigoProduto;
    private String nomeProduto;
    private BigDecimal taxaJuros;
}
