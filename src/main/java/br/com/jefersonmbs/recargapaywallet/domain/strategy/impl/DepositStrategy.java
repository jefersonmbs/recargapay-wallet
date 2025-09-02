package br.com.jefersonmbs.recargapaywallet.domain.strategy.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.mapper.TransactionMapper;
import br.com.jefersonmbs.recargapaywallet.domain.dto.AuditContext;
import br.com.jefersonmbs.recargapaywallet.domain.dto.TransactionAuditRequest;
import br.com.jefersonmbs.recargapaywallet.domain.dto.TransactionCreationRequest;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionAuditEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity.TransactionType;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.service.TransactionAuditService;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletFinderService;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletBalanceService;
import br.com.jefersonmbs.recargapaywallet.domain.service.TransactionHistoryService;
import br.com.jefersonmbs.recargapaywallet.domain.strategy.TransactionStrategy;
import br.com.jefersonmbs.recargapaywallet.domain.validator.WalletValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositStrategy implements TransactionStrategy {
    
    private static final String TARGET_WALLET_INACTIVE_MESSAGE = "Target wallet is not active for deposits";
    
    private final WalletFinderService walletFinderService;
    private final WalletBalanceService walletBalanceService;
    private final TransactionHistoryService transactionHistoryService;
    private final TransactionAuditService transactionAuditService;
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
        UUID transactionId = UUID.randomUUID();

        TransactionAuditRequest auditRequest = getTransactionAuditRequest(request, transactionId, targetWallet, balanceBefore);
        AuditContext auditContext = AuditContext.capture();
        
        transactionAuditService.auditTransactionStart(auditRequest, auditContext);
        
        try {
            walletBalanceService.creditAmount(targetWallet, request.getAmount());
            BigDecimal balanceAfter = targetWallet.getBalance();

            TransactionCreationRequest transactionRequest = getTransactionCreationRequest(request, targetWallet, balanceBefore, balanceAfter);

            TransactionHistoryEntity transaction = transactionHistoryService.createTransaction(transactionRequest);

            TransactionAuditRequest successRequest = getTransactionAuditRequest(request, transactionId, targetWallet, balanceBefore, balanceAfter);

            transactionAuditService.auditSuccessful(successRequest, auditContext);
            
            log.info("Deposit completed successfully. Transaction ID: {}", transactionId);
            return transactionMapper.toResponseDto(transaction);
            
        } catch (Exception ex) {
            log.error("Deposit failed for wallet ID: {}, amount: {}, error: {}", 
                request.getTargetWalletId(), request.getAmount(), ex.getMessage(), ex);
            
            TransactionAuditRequest failedRequest = TransactionAuditRequest.failed(
                transactionId,
                targetWallet.getId(),
                targetWallet.getUser().getId(),
                TransactionAuditEntity.OperationType.CREDIT,
                request.getAmount(),
                balanceBefore,
                "Deposit operation failed: " + ex.getMessage()
            );
            
            transactionAuditService.auditFailed(failedRequest, auditContext);
            
            throw ex;
        }
    }

    private static TransactionAuditRequest getTransactionAuditRequest(TransactionRequestDto request, UUID transactionId, WalletEntity targetWallet, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return TransactionAuditRequest.successful(
                transactionId,
            targetWallet.getId(),
            targetWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.CREDIT,
            request.getAmount(),
                balanceBefore,
                balanceAfter,
            "Deposit completed successfully"
        );
    }

    private static TransactionCreationRequest getTransactionCreationRequest(TransactionRequestDto request, WalletEntity targetWallet, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return TransactionCreationRequest.builder()
            .type(TransactionType.DEPOSIT)
            .amount(request.getAmount())
            .targetWallet(targetWallet)
            .description(request.getDescription())
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .correlationId(request.getCorrelationId())
            .build();
    }

    private static TransactionAuditRequest getTransactionAuditRequest(TransactionRequestDto request, UUID transactionId, WalletEntity targetWallet, BigDecimal balanceBefore) {
        return TransactionAuditRequest.initiated(
                transactionId,
            targetWallet.getId(),
            targetWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.CREDIT,
            request.getAmount(),
                balanceBefore,
            "Deposit initiated"
        );
    }
}