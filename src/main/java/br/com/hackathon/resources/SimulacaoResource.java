package br.com.hackathon.resources;

import br.com.hackathon.services.SimulacaoService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/simulacao")
@RequestScoped
public class SimulacaoResource {

    @Inject
    SimulacaoService simulacaoService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarTodasSimulacoes() {
        var simulacoes = simulacaoService.listarSimulacoes();
        return Response.ok(simulacoes).build();
    }
}
