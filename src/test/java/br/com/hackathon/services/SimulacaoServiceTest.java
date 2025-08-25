package br.com.hackathon.services;

import br.com.hackathon.api.common.Mensagens;
import br.com.hackathon.api.payload.PaginaPayload;
import br.com.hackathon.dao.SimulacaoDao;
import br.com.hackathon.dto.ProdutoDto;
import br.com.hackathon.dto.simulacao.CriarSimulacaoDto;
import br.com.hackathon.dto.simulacao.RespostaSimulacaoDto;
import br.com.hackathon.exceptions.ProductNotFoundException;
import br.com.hackathon.model.h2.Simulacao;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

@QuarkusTest
class SimulacaoServiceTest {

    @Inject
    SimulacaoService simulacaoService;

    @InjectMock
    SimulacaoDao simulacaoDao;

    @InjectMock
    TelemetriaService telemetriaService;

    @InjectMock
    ProdutoService produtoService;

    @Test
    void testProdutoNaoEncontrado() {

        Assertions.assertTrue(true);

        Mockito.when(produtoService.buscarProdutoPorParametro(Mockito.any()))
                .thenThrow(new ProductNotFoundException(Mensagens.PRODUTO_NAO_ENCONTRADO));

        CriarSimulacaoDto criarSimulacaoDto = CriarSimulacaoDto.builder().build();

        Exception e = Assertions.assertThrows(ProductNotFoundException.class, () -> simulacaoService.criarSimulacao(criarSimulacaoDto));

        Assertions.assertEquals(Mensagens.PRODUTO_NAO_ENCONTRADO, e.getMessage());
    }

    @ParameterizedTest
    @MethodSource("productProvider")
    void testSimulacoesValidasParaProduto(CriarSimulacaoDto criarSimulacaoDto, ProdutoDto produtoDto, BigDecimal valorTotalParcelas) {

        Mockito.when(produtoService.buscarProdutoPorParametro(criarSimulacaoDto))
                .thenReturn(produtoDto);

        RespostaSimulacaoDto respostaSimulacaoDto = simulacaoService.criarSimulacao(criarSimulacaoDto);

        Mockito.verify(produtoService, Mockito.times(1))
                .buscarProdutoPorParametro(criarSimulacaoDto);

        ArgumentCaptor<Simulacao> simulacaoCaptor = ArgumentCaptor.forClass(Simulacao.class);
        Mockito.verify(simulacaoDao, Mockito.times(1)).save(simulacaoCaptor.capture());

        Assertions.assertEquals(produtoDto.getCodigoProduto(), simulacaoCaptor.getValue().getCodigoProduto());
        Assertions.assertEquals(produtoDto.getTaxaJuros().setScale(4, RoundingMode.HALF_UP), respostaSimulacaoDto.getTaxaJuros());
        Assertions.assertEquals(valorTotalParcelas, simulacaoCaptor.getValue().getValorTotalParcelas());
    }

    private static Stream<Arguments> productProvider() {
        return Stream.of(
                Arguments.of(
                        CriarSimulacaoDto.builder().valorDesejado(new BigDecimal("900.00")).prazo((short) 5).build(),
                        ProdutoDto.builder().codigoProduto(1).taxaJuros(new BigDecimal("0.017900000")).build(),
                        new BigDecimal("948.33")
                ),
                Arguments.of(
                        CriarSimulacaoDto.builder().valorDesejado(new BigDecimal("100000.00")).prazo((short) 25).build(),
                        ProdutoDto.builder().codigoProduto(2).taxaJuros(new BigDecimal("0.017500000")).build(),
                        new BigDecimal("122750.00")
                ),
                Arguments.of(
                        CriarSimulacaoDto.builder().valorDesejado(new BigDecimal("1000000.00")).prazo((short) 49).build(),
                        ProdutoDto.builder().codigoProduto(3).taxaJuros(new BigDecimal("0.018200000")).build(),
                        new BigDecimal("1454999.98")
                ),
                Arguments.of(
                        CriarSimulacaoDto.builder().valorDesejado(new BigDecimal("10000000.00")).prazo((short) 97).build(),
                        ProdutoDto.builder().codigoProduto(4).taxaJuros(new BigDecimal("0.015100000")).build(),
                        new BigDecimal("17399000.00")
                )
        );
    }

    @Test
    void testListarSimulacoesPaginadas() {

        Short pagina = 1;
        Integer tamanhoPagina = 10;
        Long totalRegistros = 1L;

        List<Simulacao> registros = List.of(Simulacao.builder().build());

        Mockito.when(simulacaoDao.listarPaginadas(pagina, tamanhoPagina))
                .thenReturn(registros);

        Mockito.when(simulacaoDao.contarTotalRegistros())
                .thenReturn(totalRegistros);

        PaginaPayload<Simulacao> paginaPayload = simulacaoService.listarSimulacoesPaginadas(pagina, tamanhoPagina);

        Mockito.verify(simulacaoDao, Mockito.times(1))
                .listarPaginadas(pagina, tamanhoPagina);

        Mockito.verify(simulacaoDao, Mockito.times(1))
                .contarTotalRegistros();

        Assertions.assertEquals(pagina, paginaPayload.getPagina());
        Assertions.assertEquals(totalRegistros, paginaPayload.getQtdRegistros());
        Assertions.assertEquals(1, paginaPayload.getQtdRegistrosPagina());
        Assertions.assertEquals(registros, paginaPayload.getRegistros());

    }

}
