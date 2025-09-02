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
public class TransferStrategy implements TransactionStrategy {
    
    private static final String SOURCE_WALLET_INACTIVE_MESSAGE = "Source wallet is not active for transfers";
    private static final String TARGET_WALLET_INACTIVE_MESSAGE = "Target wallet is not active for transfers";
    
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
        
        log.info("Processing transfer of {} from wallet ID: {} to target", 
            request.getAmount(), request.getSourceWalletId());
        
        WalletEntity sourceWallet = walletFinderService.findWalletById(request.getSourceWalletId());
        WalletEntity targetWallet = walletFinderService.findTargetWallet(request);
        
        validateTransferWallets(sourceWallet, targetWallet);
        
        BigDecimal sourceBalanceBefore = sourceWallet.getBalance();
        walletValidator.validateSufficientBalance(sourceBalanceBefore, request.getAmount());
        
        BigDecimal targetBalanceBefore = targetWallet.getBalance();
        UUID transferOutTransactionId = UUID.randomUUID(); 
        UUID transferInTransactionId = UUID.randomUUID();  
        
        TransactionAuditRequest transferOutRequest = TransactionAuditRequest.initiated(
            transferOutTransactionId,
            sourceWallet.getId(),
            sourceWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.TRANSFER_OUT,
            request.getAmount(),
            sourceBalanceBefore,
            "Transfer out initiated"
        );
        AuditContext auditContext = AuditContext.capture();
        
        transactionAuditService.auditTransactionStart(transferOutRequest, auditContext);
        
        TransactionAuditRequest transferInRequest = TransactionAuditRequest.initiated(
            transferInTransactionId,
            targetWallet.getId(),
            targetWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.TRANSFER_IN,
            request.getAmount(),
            targetBalanceBefore,
            "Transfer in initiated"
        );
        
        transactionAuditService.auditTransactionStart(transferInRequest, auditContext);
        
        try {
            BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(request.getAmount());
            BigDecimal targetBalanceAfter = targetBalanceBefore.add(request.getAmount());
            
            executeTransfer(sourceWallet, targetWallet, sourceBalanceAfter, targetBalanceAfter);
            
            TransactionHistoryEntity transferOut = createTransferOutTransaction(
                request, sourceWallet, targetWallet, sourceBalanceBefore, sourceBalanceAfter, request.getCorrelationId());

            TransactionAuditRequest transferOutSuccessRequest = getAuditRequest(request, transferOutTransactionId, sourceWallet, sourceBalanceBefore, sourceBalanceAfter);

            transactionAuditService.auditSuccessful(transferOutSuccessRequest, auditContext);

            TransactionAuditRequest transferInSuccessRequest = getTransactionAuditRequest(request, transferInTransactionId, targetWallet, targetBalanceBefore, targetBalanceAfter);

            transactionAuditService.auditSuccessful(transferInSuccessRequest, auditContext);
            
            log.info("Transfer completed successfully. Transfer-out ID: {}, Transfer-in ID: {}", 
                transferOutTransactionId, transferInTransactionId);
            return transactionMapper.toResponseDto(transferOut);
            
        } catch (Exception ex) {
            log.error("Transfer failed from wallet ID: {} to target, amount: {}, error: {}", 
                request.getSourceWalletId(), request.getAmount(), ex.getMessage(), ex);

            TransactionAuditRequest transferOutFailedRequest = getAuditRequest(request, ex, transferOutTransactionId, sourceWallet, sourceBalanceBefore);

            transactionAuditService.auditFailed(transferOutFailedRequest, auditContext);

            TransactionAuditRequest transferInFailedRequest = getTransactionAuditRequest(request, ex, transferInTransactionId, targetWallet, targetBalanceBefore);

            transactionAuditService.auditFailed(transferInFailedRequest, auditContext);
            
            throw ex;
        }
    }

    private static TransactionAuditRequest getAuditRequest(TransactionRequestDto request, UUID transferOutTransactionId, WalletEntity sourceWallet, BigDecimal sourceBalanceBefore, BigDecimal sourceBalanceAfter) {
        return TransactionAuditRequest.successful(
                transferOutTransactionId,
            sourceWallet.getId(),
            sourceWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.TRANSFER_OUT,
            request.getAmount(),
                sourceBalanceBefore,
                sourceBalanceAfter,
            "Transfer out completed successfully"
        );
    }

    private static TransactionAuditRequest getTransactionAuditRequest(TransactionRequestDto request, UUID transferInTransactionId, WalletEntity targetWallet, BigDecimal targetBalanceBefore, BigDecimal targetBalanceAfter) {
        return TransactionAuditRequest.successful(
                transferInTransactionId,
            targetWallet.getId(),
            targetWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.TRANSFER_IN,
            request.getAmount(),
                targetBalanceBefore,
                targetBalanceAfter,
            "Transfer in completed successfully"
        );
    }

    private static TransactionAuditRequest getAuditRequest(TransactionRequestDto request, Exception ex, UUID transferOutTransactionId, WalletEntity sourceWallet, BigDecimal sourceBalanceBefore) {
        return TransactionAuditRequest.failed(
                transferOutTransactionId,
            sourceWallet.getId(),
            sourceWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.TRANSFER_OUT,
            request.getAmount(),
                sourceBalanceBefore,
            "Transfer out failed: " + ex.getMessage()
        );
    }

    private static TransactionAuditRequest getTransactionAuditRequest(TransactionRequestDto request, Exception ex, UUID transferInTransactionId, WalletEntity targetWallet, BigDecimal targetBalanceBefore) {
        return TransactionAuditRequest.failed(
                transferInTransactionId,
            targetWallet.getId(),
            targetWallet.getUser().getId(),
            TransactionAuditEntity.OperationType.TRANSFER_IN,
            request.getAmount(),
                targetBalanceBefore,
            "Transfer in failed: " + ex.getMessage()
        );
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
                                                                 BigDecimal sourceBalanceAfter,
                                                                 String correlationId) {
        TransactionCreationRequest transactionRequest = TransactionCreationRequest.builder()
            .type(TransactionType.TRANSFER_OUT)
            .amount(request.getAmount())
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .description(request.getDescription())
            .balanceBefore(sourceBalanceBefore)
            .balanceAfter(sourceBalanceAfter)
            .correlationId(correlationId)
            .build();
        
        return transactionHistoryService.createTransaction(transactionRequest);
    }

}