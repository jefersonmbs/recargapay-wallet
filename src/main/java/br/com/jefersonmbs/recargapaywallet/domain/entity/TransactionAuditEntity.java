package br.com.jefersonmbs.recargapaywallet.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_transaction_audit", indexes = {
    @Index(name = "idx_transaction_audit_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_transaction_audit_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_transaction_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_transaction_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_transaction_audit_correlation_id", columnList = "correlation_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @NotNull(message = "Wallet ID is required")
    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Operation type is required")
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    @NotNull(message = "Amount is required")
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Balance before is required")
    @Column(name = "balance_before", precision = 19, scale = 2, nullable = false)
    private BigDecimal balanceBefore;

    @NotNull(message = "Balance after is required")
    @Column(name = "balance_after", precision = 19, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction status is required")
    @Column(name = "transaction_status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull(message = "Created by is required")
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "origin_ip", length = 45) // Support IPv6
    private String originIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    public enum OperationType {

        CREDIT("Credit operation"),
        DEBIT("Debit operation"),
        TRANSFER_OUT("Transfer out operation"),
        TRANSFER_IN("Transfer in operation"),
        REFUND("Refund operation");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TransactionStatus {

        INITIATED("Transaction initiated"),
        COMPLETED("Transaction completed"),
        FAILED("Transaction failed"),
        ROLLED_BACK("Transaction rolled back");

        private final String description;

        TransactionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }


    public TransactionAuditEntity withTechnicalContext(
            String originIp,
            String userAgent,
            String sessionId,
            String correlationId,
            String metadata) {
        
        this.originIp = originIp;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.correlationId = correlationId;
        this.metadata = metadata;
        return this;
    }
}