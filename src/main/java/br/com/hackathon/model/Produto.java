package br.com.hackathon.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "PRODUTO")
public class Produto {

    @Id
    @Column(name = "CO_PRODUTO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigoProduto;

    @Column(name = "NO_PRODUTO")
    private String nomeProduto;

    @Column(name = "PC_TAXA_JUROS")
    private BigDecimal taxaJuros;

    @Column(name = "NU_MINIMO_MESES")
    private Short numeroMinimoMeses;

    @Column(name = "NU_MAXIMO_MESES")
    private Short numeroMaximoMeses;

    @Column(name = "VR_MINIMO")
    private BigDecimal valorMinimo;

    @Column(name = "VR_MAXIMO")
    private BigDecimal valorMaximo;
}
