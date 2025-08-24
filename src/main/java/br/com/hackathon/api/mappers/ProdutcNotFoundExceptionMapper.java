package br.com.hackathon.api.mappers;

import br.com.hackathon.api.payload.ErrorPayload;
import br.com.hackathon.exceptions.ProductNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ProdutcNotFoundExceptionMapper implements ExceptionMapper<ProductNotFoundException> {

    @Override
    public Response toResponse(ProductNotFoundException e) {
        ErrorPayload errorPayload = ErrorPayload.builder()
                .codigo("PRODUTO_NAO_ENCONTRADO")
                .mensagem("Nenhum produto disponivel para os dados informados")
                .build();

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorPayload)
                .build();
    }
}
