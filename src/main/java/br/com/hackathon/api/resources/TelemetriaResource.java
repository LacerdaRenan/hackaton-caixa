package br.com.hackathon.api.resources;

import br.com.hackathon.services.TelemetriaService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;

@RequestScoped
@Path("/api/v1/telemetria")
public class TelemetriaResource {

    @Inject
    TelemetriaService telemetriaService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDadosTelemetria(@QueryParam("data") LocalDate data) {
        var dadosTelemetria = telemetriaService.consultaDadosTelemetria(data);
        return Response.ok(dadosTelemetria).build();
    }
}
