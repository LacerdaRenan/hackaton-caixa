package br.com.hackathon.api.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorPayload {
    public String codigo;
    public String mensagem;
}
