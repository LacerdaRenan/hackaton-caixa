package br.com.hackathon.services;

import br.com.hackathon.api.common.Mensagens;
import br.com.hackathon.dao.ProdutoDao;
import br.com.hackathon.dto.simulacao.CriarSimulacaoDto;
import br.com.hackathon.dto.ProdutoDto;
import br.com.hackathon.exceptions.ProductNotFoundException;
import br.com.hackathon.mapper.ProdutoMapper;
import br.com.hackathon.model.sqlserver.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import java.util.Comparator;
import java.util.List;

@Slf4j
@ApplicationScoped
public class ProdutoService {

    @Inject
    ProdutoDao produtoDao;

    @Inject
    ProdutoMapper produtoMapper;

    public List<Produto> listarProdutos() {
        return produtoDao.listAll();
    }

    public ProdutoDto buscarProdutoPorId(Integer id) {
        Produto produto = produtoDao.getById(id);

        if(produto!=null)
            return ProdutoDto.builder()
                    .codigoProduto(produto.getCodigoProduto())
                    .nomeProduto(produto.getNomeProduto())
                    .taxaJuros(produto.getTaxaJuros())
                    .build();

        return null;
    }

    public ProdutoDto buscarProdutoPorParametro(CriarSimulacaoDto criarSimulacaoDto) {
        List<Produto> produtos = produtoDao.buscarProdutoPorParametros(criarSimulacaoDto);

        if (produtos.isEmpty())
            throw new ProductNotFoundException(Mensagens.PRODUTO_NAO_ENCONTRADO);

        return produtos.stream()
                .min(Comparator.comparing(Produto::getTaxaJuros))
                .map(produtoMapper::toDto)
                .orElse(produtoMapper.toDto(produtos.getFirst()));
    }
}
