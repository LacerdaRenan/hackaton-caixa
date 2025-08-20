package br.com.hackathon.resources;

import br.com.hackathon.services.ProdutoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/produto")
public class ProdutoResource {

    @Inject
    ProdutoService produtoService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarProdutos() {
        var produtos = produtoService.listarProdutos();
        return Response.ok(produtos).build();
    }
}
