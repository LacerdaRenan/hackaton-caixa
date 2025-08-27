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

import java.math.BigDecimal;
import java.util.ArrayList;
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
        return produtoDao.listarTodosProdutos();
    }

    public ProdutoDto buscarProdutoPorParametro(CriarSimulacaoDto criarSimulacaoDto) {

        List<Produto> produtos = produtoDao.listarTodosProdutos();
        List<Produto> produtosDisponiveis = new ArrayList<>();

        Short numeroParcelas = criarSimulacaoDto.getPrazo();
        BigDecimal valor = criarSimulacaoDto.getValorDesejado();

        for (Produto p : produtos) {

            Short numeroMinimoMeses = p.getNumeroMinimoMeses() == null ? 1 : p.getNumeroMinimoMeses();
            Short numeroMaximoMeses = p.getNumeroMaximoMeses() == null ? numeroParcelas : p.getNumeroMaximoMeses();
            BigDecimal valorMinimo = p.getValorMinimo() == null ? BigDecimal.ONE : p.getValorMinimo();
            BigDecimal valorMaximo = p.getValorMaximo() == null ? valor : p.getValorMaximo();

            if (numeroParcelas.compareTo(numeroMinimoMeses) >= 0
                    && numeroParcelas.compareTo(numeroMaximoMeses) <= 0
                    && valor.compareTo(valorMinimo) >= 0
                    && valor.compareTo(valorMaximo) <= 0)
                produtosDisponiveis.add(p);
        }

        if (produtosDisponiveis.isEmpty())
            throw new ProductNotFoundException(Mensagens.PRODUTO_NAO_ENCONTRADO);

        return produtosDisponiveis.stream()
                .min(Comparator.comparing(Produto::getTaxaJuros))
                .map(produtoMapper::toDto)
                .orElse(produtoMapper.toDto(produtos.getFirst()));
    }
}
