package br.com.hackathon.services;

import br.com.hackathon.dao.ProdutoDao;
import br.com.hackathon.dto.ProdutoDto;
import br.com.hackathon.model.sqlserver.Produto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class ProdutoService {

    @Inject
    ProdutoDao produtoDao;

    @Inject
    TelemetriaService telemetriaService;

    public List<Produto> listarProdutos() {
        try {
            Long inicioProcessamentoTelemetria = System.nanoTime();

            List<Produto> produtos = produtoDao.listAll();

            Long fimProcessamentoTelemetria = System.nanoTime();
            Long duracaoProcessamentoTelemetria = (fimProcessamentoTelemetria - inicioProcessamentoTelemetria) / 1_000_000;

            telemetriaService.registrarDadosApi("Listar Produtos", duracaoProcessamentoTelemetria, (short) 200);

            return produtos;
        } catch (Exception e) {
            log.error("Nao foi possivel consultar os produtos no banco {}", e.getMessage());
            return new ArrayList<>();
        }
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
}
