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
public class TransferStrategy implements TransactionStrategy {
    
    private static final String SOURCE_WALLET_INACTIVE_MESSAGE = "Source wallet is not active for transfers";
    private static final String TARGET_WALLET_INACTIVE_MESSAGE = "Target wallet is not active for transfers";
    
    private final WalletFinderService walletFinderService;
    private final WalletBalanceService walletBalanceService;
    private final TransactionHistoryService transactionHistoryService;
    private final WalletValidator walletValidator;
    private final TransactionMapper transactionMapper;
    
    @Override
    public TransactionResponseDto execute(TransactionRequestDto request) {
        walletValidator.validateTransactionRequest(request);
        walletValidator.validateWalletId(request.getSourceWalletId());
        
        log.info("Processing transfer of {} from wallet ID: {} to target", 
            request.getAmount(), request.getSourceWalletId());
        
        WalletEntity sourceWallet = walletFinderService.findWalletById(request.getSourceWalletId());
        WalletEntity targetWallet = walletFinderService.findTargetWallet(request);
        
        validateTransferWallets(sourceWallet, targetWallet);
        
        BigDecimal sourceBalanceBefore = sourceWallet.getBalance();
        walletValidator.validateSufficientBalance(sourceBalanceBefore, request.getAmount());
        
        BigDecimal targetBalanceBefore = targetWallet.getBalance();
        BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(request.getAmount());
        BigDecimal targetBalanceAfter = targetBalanceBefore.add(request.getAmount());
        
        executeTransfer(sourceWallet, targetWallet, sourceBalanceAfter, targetBalanceAfter);
        
        TransactionHistoryEntity transferOut = createTransferOutTransaction(
            request, sourceWallet, targetWallet, sourceBalanceBefore, sourceBalanceAfter);
        
        createTransferInTransaction(
            request, sourceWallet, targetWallet, targetBalanceBefore, targetBalanceAfter);
        
        log.info("Transfer completed successfully. Transaction ID: {}", transferOut.getId());
        return transactionMapper.toResponseDto(transferOut);
    }
    
    private void validateTransferWallets(WalletEntity sourceWallet, WalletEntity targetWallet) {
        walletValidator.validateWalletForTransaction(sourceWallet, SOURCE_WALLET_INACTIVE_MESSAGE);
        walletValidator.validateWalletForTransaction(targetWallet, TARGET_WALLET_INACTIVE_MESSAGE);
        walletValidator.validateDifferentWallets(sourceWallet, targetWallet);
    }
    
    private void executeTransfer(WalletEntity sourceWallet, WalletEntity targetWallet, 
                                BigDecimal sourceBalanceAfter, BigDecimal targetBalanceAfter) {
        walletBalanceService.updateBalance(sourceWallet, sourceBalanceAfter);
        walletBalanceService.updateBalance(targetWallet, targetBalanceAfter);
    }
    
    private TransactionHistoryEntity createTransferOutTransaction(TransactionRequestDto request,
                                                                 WalletEntity sourceWallet, 
                                                                 WalletEntity targetWallet,
                                                                 BigDecimal sourceBalanceBefore,
                                                                 BigDecimal sourceBalanceAfter) {
        TransactionCreationRequest transactionRequest = TransactionCreationRequest.builder()
            .type(TransactionType.TRANSFER_OUT)
            .amount(request.getAmount())
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .description(request.getDescription())
            .balanceBefore(sourceBalanceBefore)
            .balanceAfter(sourceBalanceAfter)
            .build();
        
        return transactionHistoryService.createTransaction(transactionRequest);
    }
    
    private void createTransferInTransaction(TransactionRequestDto request,
                                           WalletEntity sourceWallet,
                                           WalletEntity targetWallet,
                                           BigDecimal targetBalanceBefore,
                                           BigDecimal targetBalanceAfter) {
        TransactionCreationRequest transactionRequest = TransactionCreationRequest.builder()
            .type(TransactionType.TRANSFER_IN)
            .amount(request.getAmount())
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .description(request.getDescription())
            .balanceBefore(targetBalanceBefore)
            .balanceAfter(targetBalanceAfter)
            .build();
        
        transactionHistoryService.createTransaction(transactionRequest);
    }
}