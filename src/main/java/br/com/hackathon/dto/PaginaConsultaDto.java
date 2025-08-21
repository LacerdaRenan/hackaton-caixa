package br.com.hackathon.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PaginaConsultaDto<T> {
    private Short pagina;
    private Long qtdRegistros;
    private Integer qtdRegistrosPagina;
    private List<T> registros;
}
