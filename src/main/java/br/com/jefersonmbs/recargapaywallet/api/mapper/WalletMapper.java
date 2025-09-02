package br.com.jefersonmbs.recargapaywallet.api.mapper;

import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userCpf", source = "user.cpf")
    @Mapping(target = "userName", source = "user.name")
    WalletResponseDto toResponseDto(WalletEntity walletEntity);

    List<WalletResponseDto> toResponseDtoList(List<WalletEntity> walletEntities);
}