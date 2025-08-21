package br.com.hackathon.resources;

import br.com.hackathon.services.SimulacaoService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/simulacao")
@RequestScoped
public class SimulacaoResource {

    @Inject
    SimulacaoService simulacaoService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarTodasSimulacoes(
            @QueryParam("pagina") @DefaultValue("1") short pagina,
            @QueryParam("tamanhoPagina") @DefaultValue("5") int tamanhoPagina) {
        var simulacoes = simulacaoService.listarSimulacoes(pagina, tamanhoPagina);
        return Response.ok(simulacoes).build();
    }
}
