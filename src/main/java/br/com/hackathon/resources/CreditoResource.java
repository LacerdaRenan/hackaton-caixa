package br.com.hackathon.resources;

import br.com.hackathon.dto.CriarSimulacaoDto;
import br.com.hackathon.services.CreditoService;
import br.com.hackathon.services.ProdutoService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/credito")
public class CreditoResource {

    @Inject
    CreditoService creditoService;

    @POST
    @Path("simular")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response realizarSimulacaoCredito(@Valid CriarSimulacaoDto criarSimulacaoDto) {
        var simulacao = creditoService.novaSimulacao(criarSimulacaoDto);
        return Response.status(Response.Status.CREATED)
                .entity(simulacao)
                .build();
    }
}
