package br.com.hackathon.filter;

import br.com.hackathon.producer.EventhubProducer;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
public class EventHubResponseFilter implements ContainerResponseFilter {

    @Inject
    EventhubProducer eventhubProducer;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        if (method.equals("POST")
                && path.equals("/api/v1/simulacao")
                && responseContext.getStatusInfo().getFamily() == jakarta.ws.rs.core.Response.Status.Family.SUCCESSFUL
                && responseContext.hasEntity()) {

            Object entity = responseContext.getEntity();
            log.info("Interceptando resposta {} para enviar ao Event Hub.", entity);
            eventhubProducer.sendEvent(entity);
        }
    }
}