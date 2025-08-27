package br.com.hackathon.api.resources;

import br.com.hackathon.model.sqlserver.Produto;
import br.com.hackathon.services.ProdutoService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/api/v1/produto")
@RequestScoped
public class ProdutoResource {

    @Inject
    ProdutoService produtoService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "Listar todos os produtos disponiveis", content = @Content(schema = @Schema(implementation = Produto[].class)))
    public Response listarProdutos() {
        var produtos = produtoService.listarProdutos();
        return Response.ok(produtos).build();
    }
}
