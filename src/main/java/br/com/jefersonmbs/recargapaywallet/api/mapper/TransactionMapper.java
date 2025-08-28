package br.com.jefersonmbs.recargapaywallet.api.mapper;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "sourceWalletId", source = "sourceWallet.id")
    @Mapping(target = "sourceAccountNumber", source = "sourceWallet.accountNumber")
    @Mapping(target = "sourceUserCpf", source = "sourceWallet.user.cpf")
    @Mapping(target = "sourceUserName", source = "sourceWallet.user.name")
    @Mapping(target = "targetWalletId", source = "targetWallet.id")
    @Mapping(target = "targetAccountNumber", source = "targetWallet.accountNumber")
    @Mapping(target = "targetUserCpf", source = "targetWallet.user.cpf")
    @Mapping(target = "targetUserName", source = "targetWallet.user.name")
    TransactionResponseDto toResponseDto(TransactionHistoryEntity transactionEntity);

    List<TransactionResponseDto> toResponseDtoList(List<TransactionHistoryEntity> transactionEntities);
}