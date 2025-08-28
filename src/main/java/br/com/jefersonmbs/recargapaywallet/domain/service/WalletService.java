package br.com.jefersonmbs.recargapaywallet.domain.service;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;

import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletResponseDto createWallet(Long userId);

    WalletResponseDto getWalletById(UUID walletId);

    WalletResponseDto getWalletByUserId(Long userId);

    WalletResponseDto getWalletByUserCpf(String cpf);

    WalletResponseDto getWalletByAccountNumber(Long accountNumber);

    List<WalletResponseDto> getAllActiveWallets();

    TransactionResponseDto deposit(TransactionRequestDto transactionRequest);

    TransactionResponseDto withdraw(TransactionRequestDto transactionRequest);

    TransactionResponseDto transfer(TransactionRequestDto transactionRequest);

    List<TransactionResponseDto> getTransactionHistory(UUID walletId, Long userId);

    List<TransactionResponseDto> getTransactionHistoryByCpf(String cpf);

    void toggleActiveWallet(UUID walletId);

}