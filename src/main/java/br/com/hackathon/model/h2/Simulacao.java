package br.com.hackathon.model.h2;

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

    @Column(name = "VR_TOTAL_PARCELAS")
    private BigDecimal valorTotalParcelas;

    @Column(name = "TS_DT_CRIACAO")
    @CreationTimestamp
    private LocalDate dataCriacao;
}
