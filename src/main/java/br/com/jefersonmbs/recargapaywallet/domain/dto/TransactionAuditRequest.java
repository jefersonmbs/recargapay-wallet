package br.com.jefersonmbs.recargapaywallet.domain.dto;

import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionAuditEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
public class TransactionAuditRequest {
    
    private final UUID transactionId;
    private final UUID walletId;
    private final Long userId;
    private final TransactionAuditEntity.OperationType operationType;
    private final BigDecimal amount;
    private final BigDecimal balanceBefore;
    private final BigDecimal balanceAfter;
    private final TransactionAuditEntity.TransactionStatus status;
    private final String description;


    public static TransactionAuditRequest successful(
            UUID transactionId,
            UUID walletId,
            Long userId,
            TransactionAuditEntity.OperationType operationType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String description) {
        
        return TransactionAuditRequest.builder()
                .transactionId(transactionId)
                .walletId(walletId)
                .userId(userId)
                .operationType(operationType)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(TransactionAuditEntity.TransactionStatus.COMPLETED)
                .description(description)
                .build();
    }


    public static TransactionAuditRequest failed(
            UUID transactionId,
            UUID walletId,
            Long userId,
            TransactionAuditEntity.OperationType operationType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            String description) {
        
        return TransactionAuditRequest.builder()
                .transactionId(transactionId)
                .walletId(walletId)
                .userId(userId)
                .operationType(operationType)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceBefore) 
                .status(TransactionAuditEntity.TransactionStatus.FAILED)
                .description(description)
                .build();
    }


    public static TransactionAuditRequest initiated(
            UUID transactionId,
            UUID walletId,
            Long userId,
            TransactionAuditEntity.OperationType operationType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            String description) {
        
        return TransactionAuditRequest.builder()
                .transactionId(transactionId)
                .walletId(walletId)
                .userId(userId)
                .operationType(operationType)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceBefore)
                .status(TransactionAuditEntity.TransactionStatus.INITIATED)
                .description(description)
                .build();
    }

}