package br.com.hackathon.api.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PaginaPayload<T> {
    private Short pagina;
    private Long qtdRegistros;
    private Integer qtdRegistrosPagina;
    private List<T> registros;
}
