package br.com.jefersonmbs.recargapaywallet.domain.strategy.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.mapper.TransactionMapper;
import br.com.jefersonmbs.recargapaywallet.domain.dto.TransactionCreationRequest;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity.TransactionType;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletFinderService;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletBalanceService;
import br.com.jefersonmbs.recargapaywallet.domain.service.TransactionHistoryService;
import br.com.jefersonmbs.recargapaywallet.domain.strategy.TransactionStrategy;
import br.com.jefersonmbs.recargapaywallet.domain.validator.WalletValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawStrategy implements TransactionStrategy {
    
    private static final String SOURCE_WALLET_INACTIVE_MESSAGE = "Source wallet is not active for withdrawals";
    
    private final WalletFinderService walletFinderService;
    private final WalletBalanceService walletBalanceService;
    private final TransactionHistoryService transactionHistoryService;
    private final WalletValidator walletValidator;
    private final TransactionMapper transactionMapper;
    
    @Override
    public TransactionResponseDto execute(TransactionRequestDto request) {
        walletValidator.validateTransactionRequest(request);
        walletValidator.validateWalletId(request.getSourceWalletId());
        
        log.info("Processing withdrawal of {} from wallet ID: {}", 
            request.getAmount(), request.getSourceWalletId());
        
        WalletEntity sourceWallet = walletFinderService.findWalletById(request.getSourceWalletId());
        walletValidator.validateWalletForTransaction(sourceWallet, SOURCE_WALLET_INACTIVE_MESSAGE);
        
        BigDecimal balanceBefore = sourceWallet.getBalance();
        walletValidator.validateSufficientBalance(balanceBefore, request.getAmount());
        
        walletBalanceService.debitAmount(sourceWallet, request.getAmount());
        BigDecimal balanceAfter = sourceWallet.getBalance();
        
        TransactionCreationRequest transactionRequest = TransactionCreationRequest.builder()
            .type(TransactionType.WITHDRAWAL)
            .amount(request.getAmount())
            .sourceWallet(sourceWallet)
            .description(request.getDescription())
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .build();
        
        TransactionHistoryEntity transaction = transactionHistoryService.createTransaction(transactionRequest);
        
        log.info("Withdrawal completed successfully. Transaction ID: {}", transaction.getId());
        return transactionMapper.toResponseDto(transaction);
    }
}