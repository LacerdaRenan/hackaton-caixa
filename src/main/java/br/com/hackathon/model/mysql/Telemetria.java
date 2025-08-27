package br.com.hackathon.model.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "API_DATA")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Telemetria {

    @Id
    @Column(name = "CO_API_DATA")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NM_API")
    private String nomeApi;

    @Column(name = "TS_REQ")
    private Long duracao;

    @Column(name = "ST_CD_RSP")
    private Integer statusCodeResponse;

    @Column(name = "TS_DT_CRIACAO")
    @CreationTimestamp
    private LocalDate dataCriacao;
}
