package br.com.hackathon.model.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "SIMULACAO")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulacao {

    @Id
    @Column(name = "CO_SIMULACAO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSimulacao;

    @Column(name = "VR_SIMULADO")
    private BigDecimal valorDesejado;

    @Column(name = "NU_MESES")
    private Short prazo;

    @Column(name = "CO_PRODUTO")
    private Integer codigoProduto;

    @Column(name = "NO_PRODUTO")
    private String descricaoProduto;

    @Column(name = "TAXA_JUROS_SIM", precision = 10, scale = 9)
    private BigDecimal taxaJuros;

    @Column(name = "VR_TOTAL_PARCELAS", precision = 18, scale = 2)
    private BigDecimal valorTotalParcelas;

    @Column(name = "TS_DT_CRIACAO")
    @CreationTimestamp
    private LocalDate dataCriacao;
}
