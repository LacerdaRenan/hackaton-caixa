package br.com.hackathon.mapper;

import br.com.hackathon.dto.ProdutoDto;
import br.com.hackathon.model.sqlserver.Produto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface ProdutoMapper {
    ProdutoDto toDto(Produto produto);
}
