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
public class DepositStrategy implements TransactionStrategy {
    
    private static final String TARGET_WALLET_INACTIVE_MESSAGE = "Target wallet is not active for deposits";
    
    private final WalletFinderService walletFinderService;
    private final WalletBalanceService walletBalanceService;
    private final TransactionHistoryService transactionHistoryService;
    private final WalletValidator walletValidator;
    private final TransactionMapper transactionMapper;
    
    @Override
    public TransactionResponseDto execute(TransactionRequestDto request) {
        walletValidator.validateTransactionRequest(request);
        walletValidator.validateWalletId(request.getTargetWalletId());
        
        log.info("Processing deposit of {} to wallet ID: {}", 
            request.getAmount(), request.getTargetWalletId());
        
        WalletEntity targetWallet = walletFinderService.findWalletById(request.getTargetWalletId());
        walletValidator.validateWalletForTransaction(targetWallet, TARGET_WALLET_INACTIVE_MESSAGE);
        
        BigDecimal balanceBefore = targetWallet.getBalance();
        walletBalanceService.creditAmount(targetWallet, request.getAmount());
        BigDecimal balanceAfter = targetWallet.getBalance();
        
        TransactionCreationRequest transactionRequest = TransactionCreationRequest.builder()
            .type(TransactionType.DEPOSIT)
            .amount(request.getAmount())
            .targetWallet(targetWallet)
            .description(request.getDescription())
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .build();
        
        TransactionHistoryEntity transaction = transactionHistoryService.createTransaction(transactionRequest);
        
        log.info("Deposit completed successfully. Transaction ID: {}", transaction.getId());
        return transactionMapper.toResponseDto(transaction);
    }
}