package br.com.hackathon.services;

import br.com.hackathon.dao.ProdutoDao;
import br.com.hackathon.model.sqlserver.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ProdutoService {

    @Inject
    ProdutoDao produtoDao;

    public List<Produto> listarProdutos() {
        return produtoDao.listAll();
    }
}
