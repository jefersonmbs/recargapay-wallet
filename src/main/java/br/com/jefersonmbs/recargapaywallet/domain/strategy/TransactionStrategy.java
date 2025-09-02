package br.com.jefersonmbs.recargapaywallet.domain.strategy;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;


public interface TransactionStrategy {

    TransactionResponseDto execute(TransactionRequestDto request);
}