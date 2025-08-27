package br.com.hackathon.api.resources;

import br.com.hackathon.dto.telemetria.TelemetriaDto;
import br.com.hackathon.services.TelemetriaService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.time.LocalDate;

@RequestScoped
@Path("/api/v1/telemetria")
public class TelemetriaResource {

    @Inject
    TelemetriaService telemetriaService;

    @GET
    @Path("/{data}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "Listar dados de telemetria das endpoints do servico", content = @Content(schema = @Schema(implementation = TelemetriaDto.class)))
    public Response getDadosTelemetria(@PathParam("data") LocalDate data) {
        var dadosTelemetria = telemetriaService.consultaDadosTelemetria(data);
        return Response.ok(dadosTelemetria).build();
    }
}
