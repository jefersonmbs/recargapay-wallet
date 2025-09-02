package br.com.jefersonmbs.recargapaywallet.domain.service;

import br.com.jefersonmbs.recargapaywallet.api.dto.PagedTransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionHistoryRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;

import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletResponseDto createWallet(Long userId);

    WalletResponseDto getWalletByAccountNumber(Long accountNumber);

    List<WalletResponseDto> getAllActiveWallets();

    TransactionResponseDto deposit(TransactionRequestDto transactionRequest);

    TransactionResponseDto withdraw(TransactionRequestDto transactionRequest);

    TransactionResponseDto transfer(TransactionRequestDto transactionRequest);

    PagedTransactionResponseDto getTransactionHistoryPaginated(UUID walletId, Long userId, TransactionHistoryRequestDto request);
    
    void toggleActiveWallet(UUID walletId);

}