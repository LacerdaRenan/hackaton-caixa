package br.com.hackathon.api.resources;

import br.com.hackathon.dto.simulacao.CriarSimulacaoDto;
import br.com.hackathon.services.SimulacaoService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;

@Path("/api/v1/simulacao")
@RequestScoped
public class SimulacaoResource {

    @Inject
    SimulacaoService simulacaoService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response realizarSimulacaoCredito(@Valid CriarSimulacaoDto criarSimulacaoDto) {
        var simulacao = simulacaoService.criarSimulacao(criarSimulacaoDto);
        return Response.status(Response.Status.CREATED)
                .entity(simulacao)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarTodasSimulacoes(
            @QueryParam("pagina") @DefaultValue("1") short pagina,
            @QueryParam("tamanhoPagina") @DefaultValue("5") int tamanhoPagina) {
        var simulacoes = simulacaoService.listarSimulacoesPaginadas(pagina, tamanhoPagina);
        return Response.ok(simulacoes).build();
    }

    @GET
    @Path("/{dataReferencia}")
    public Response listarVolumeSimuladoPorProduto(@PathParam("dataReferencia") LocalDate data) {
        var volumes = simulacaoService.calcularVolumeSimuladoData(data);
        return Response.ok(volumes).build();
    }
}
