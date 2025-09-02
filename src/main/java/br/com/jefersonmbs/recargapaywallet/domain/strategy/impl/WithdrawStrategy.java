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
public class WithdrawStrategy implements TransactionStrategy {
    
    private static final String SOURCE_WALLET_INACTIVE_MESSAGE = "Source wallet is not active for withdrawals";
    
    private final WalletFinderService walletFinderService;
    private final WalletBalanceService walletBalanceService;
    private final TransactionHistoryService transactionHistoryService;
    private final TransactionAuditService transactionAuditService;
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
        
        UUID transactionId = UUID.randomUUID();

        TransactionAuditRequest auditRequest = getTransactionAuditRequest(request, transactionId, sourceWallet, balanceBefore);
        AuditContext auditContext = AuditContext.capture();
        
        transactionAuditService.auditTransactionStart(auditRequest, auditContext);
        
        try {
            walletBalanceService.debitAmount(sourceWallet, request.getAmount());
            BigDecimal balanceAfter = sourceWallet.getBalance();

            TransactionCreationRequest transactionRequest = getTransactionCreationRequest(request, sourceWallet, balanceBefore, balanceAfter);

            TransactionHistoryEntity transaction = transactionHistoryService.createTransaction(transactionRequest);

            TransactionAuditRequest successRequest = getTransactionAuditRequest(request, transactionId, sourceWallet, balanceBefore, balanceAfter);

            transactionAuditService.auditSuccessful(successRequest, auditContext);
            
            log.info("Withdrawal completed successfully. Transaction ID: {}", transactionId);
            return transactionMapper.toResponseDto(transaction);
            
        } catch (Exception ex) {
            log.error("Withdrawal failed for wallet ID: {}, amount: {}, error: {}", 
                request.getSourceWalletId(), request.getAmount(), ex.getMessage(), ex);

            TransactionAuditRequest failedRequest = getTransactionAuditRequest(request, ex, transactionId, sourceWallet, balanceBefore);

            transactionAuditService.auditFailed(failedRequest, auditContext);
            
            throw ex;
        }
    }

    private static TransactionAuditRequest getTransactionAuditRequest(TransactionRequestDto request, Exception ex, UUID transactionId, WalletEntity sourceWallet, BigDecimal balanceBefore) {
        return TransactionAuditRequest.failed(
                transactionId,
            sourceWallet.getId(),
            sourceWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.DEBIT,
            request.getAmount(),
                balanceBefore,
            "Withdrawal operation failed: " + ex.getMessage()
        );
    }

    private static TransactionAuditRequest getTransactionAuditRequest(TransactionRequestDto request, UUID transactionId, WalletEntity sourceWallet, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return TransactionAuditRequest.successful(
                transactionId,
            sourceWallet.getId(),
            sourceWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.DEBIT,
            request.getAmount(),
                balanceBefore,
                balanceAfter,
            "Withdrawal completed successfully"
        );
    }

    private static TransactionCreationRequest getTransactionCreationRequest(TransactionRequestDto request, WalletEntity sourceWallet, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return TransactionCreationRequest.builder()
            .type(TransactionType.WITHDRAWAL)
            .amount(request.getAmount())
            .sourceWallet(sourceWallet)
            .description(request.getDescription())
            .balanceBefore(balanceBefore)
            .balanceAfter(balanceAfter)
            .correlationId(request.getCorrelationId())
            .build();
    }

    private static TransactionAuditRequest getTransactionAuditRequest(TransactionRequestDto request, UUID transactionId, WalletEntity sourceWallet, BigDecimal balanceBefore) {
        return TransactionAuditRequest.initiated(
                transactionId,
            sourceWallet.getId(),
            sourceWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.DEBIT,
            request.getAmount(),
                balanceBefore,
            "Withdrawal initiated"
        );
    }
}