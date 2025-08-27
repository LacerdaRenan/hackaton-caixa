package br.com.hackathon.filter;

import br.com.hackathon.services.TelemetriaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TelemetriaFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    TelemetriaService telemetriaService;

    private static final String INICIO_PROCESSAMENTO = "inicio-processamento-telemetria";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setProperty(INICIO_PROCESSAMENTO, System.nanoTime());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Long fimProcessamentoTelemetria = System.nanoTime();
        Long inicioProcessamentoTelemetria = (Long) requestContext.getProperty(INICIO_PROCESSAMENTO);

        String method = requestContext.getMethod();

        String[] segmentos = requestContext.getUriInfo().getPath().split("/");
        String path = String.format("%s %s/%s/%s/%s", method, segmentos[0], segmentos[1], segmentos[2], segmentos[3]);

        Integer status = responseContext.getStatus();

        Long duracaoProcessamentoTelemetria = (fimProcessamentoTelemetria - inicioProcessamentoTelemetria) / 1_000_000;

        telemetriaService.registrarDadosApi(path, duracaoProcessamentoTelemetria, status);
    }
}
