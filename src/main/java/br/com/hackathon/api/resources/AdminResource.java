package br.com.hackathon.api.resources;

import io.quarkus.cache.CacheInvalidateAll;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/api/v1/admin")
public class AdminResource {

    @POST
    @Path("/produto/refresh")
    @CacheInvalidateAll(cacheName = "produtos")
    @APIResponse(responseCode = "200", description = "Forcar atualizacao do cache de produtos", content = @Content(schema = @Schema(implementation = String.class)))
    public Response atualizarCacheManualmente() {
        return Response.ok("Cache de produtos atualizado com sucesso").build();
    }
}
