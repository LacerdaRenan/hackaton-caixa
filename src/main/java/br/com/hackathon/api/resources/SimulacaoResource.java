package br.com.hackathon.api.resources;

import br.com.hackathon.api.payload.ErrorPayload;
import br.com.hackathon.api.payload.PaginaSimulacaoPayload;
import br.com.hackathon.dto.simulacao.CriarSimulacaoDto;
import br.com.hackathon.dto.simulacao.RespostaSimulacaoDto;
import br.com.hackathon.dto.volume_produto_simulacao.RespostaVolumeProdutoSimulacaoDto;
import br.com.hackathon.services.SimulacaoService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.time.LocalDate;

@Path("/api/v1/simulacao")
@RequestScoped
public class SimulacaoResource {

    @Inject
    SimulacaoService simulacaoService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "201", description = "Criar nova simulacao", content = @Content(schema = @Schema(implementation = RespostaSimulacaoDto.class)))
    @APIResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorPayload.class)))
    @RequestBody(content = @Content(schema = @Schema(implementation = CriarSimulacaoDto.class)))
    public Response realizarSimulacaoCredito(@Valid CriarSimulacaoDto criarSimulacaoDto) {
        var simulacao = simulacaoService.criarSimulacao(criarSimulacaoDto);
        return Response.status(Response.Status.CREATED)
                .entity(simulacao)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "Listar todas as simulacoes ja realizadas", content = @Content(schema = @Schema(implementation = PaginaSimulacaoPayload.class)))
    public Response listarTodasSimulacoes(
            @QueryParam("pagina") @DefaultValue("1") short pagina,
            @QueryParam("tamanhoPagina") @DefaultValue("5") int tamanhoPagina) {
        var simulacoes = simulacaoService.listarSimulacoesPaginadas(pagina, tamanhoPagina);
        return Response.ok(simulacoes).build();
    }

    @GET
    @Path("/{dataReferencia}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "Listar volume simulacoes por produto e data", content = @Content(schema = @Schema(implementation = RespostaVolumeProdutoSimulacaoDto.class)))
    public Response listarVolumeSimuladoPorProduto(@PathParam("dataReferencia") LocalDate data) {
        var volumes = simulacaoService.calcularVolumeSimuladoData(data);
        return Response.ok(volumes).build();
    }
}
