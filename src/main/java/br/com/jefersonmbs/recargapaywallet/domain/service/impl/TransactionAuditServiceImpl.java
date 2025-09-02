package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.domain.dto.AuditContext;
import br.com.jefersonmbs.recargapaywallet.domain.dto.TransactionAuditRequest;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionAuditEntity;
import br.com.jefersonmbs.recargapaywallet.domain.exception.AuditException;
import br.com.jefersonmbs.recargapaywallet.domain.repository.TransactionAuditRepository;
import br.com.jefersonmbs.recargapaywallet.domain.service.TransactionAuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionAuditServiceImpl implements TransactionAuditService {

    private final TransactionAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionAuditEntity audit(TransactionAuditRequest request) {
        
        AuditContext context = AuditContext.capture();
        
        setupMDC(request, context);
        
        try {
            validateAuditRequest(request);
            
            TransactionAuditEntity auditEntity = buildAuditEntity(request, context);
            
            TransactionAuditEntity savedAudit = auditRepository.save(auditEntity);
            
            log.info("Transaction audit recorded - TxnId: {}, Wallet: {}, Operation: {}, Status: {}, Amount: {}",
                    request.getTransactionId(), 
                    request.getWalletId(), 
                    request.getOperationType(),
                    request.getStatus(),
                    request.getAmount());
            
            return savedAudit;
            
        } catch (Exception e) {
            log.error("Failed to record transaction audit - TxnId: {}, Error: {}", 
                     request.getTransactionId(), e.getMessage(), e);
            throw new AuditException("Failed to record transaction audit", e);
        } finally {
            clearMDC();
        }
    }

    @Override
    @Transactional
    public void auditTransactionStart(
            TransactionAuditRequest request,
            AuditContext context) {
        
        validateAuditRequest(request);
        validateAuditContext(context);
        
        log.debug("Creating transaction start audit: transactionId={}, operationType={}", 
                request.getTransactionId(), request.getOperationType());
        
         var auditEntity = createAuditEntity(request, context);
         var savedAudit = auditRepository.save(auditEntity);
        
        log.info("Transaction start audit created: auditId={}, transactionId={}, correlationId={}", 
                savedAudit.getId(), request.getTransactionId(), context.getCorrelationId());
        
    }

    @Override
    @Transactional
    public void auditSuccessful(
            TransactionAuditRequest request,
            AuditContext context) {
        
        validateAuditRequest(request);
        validateAuditContext(context);
        
        log.debug("Creating successful transaction audit: transactionId={}, operationType={}", 
                request.getTransactionId(), request.getOperationType());
        
         var auditEntity = createAuditEntity(request, context);
         var savedAudit = auditRepository.save(auditEntity);
        
        log.info("Successful transaction audit created: auditId={}, transactionId={}, correlationId={}", 
                savedAudit.getId(), request.getTransactionId(), context.getCorrelationId());
        
    }

    @Override
    @Transactional
    public void auditFailed(
            TransactionAuditRequest request,
            AuditContext context) {
        
        validateAuditRequest(request);
        validateAuditContext(context);
        
        log.debug("Creating failed transaction audit: transactionId={}, reason={}", 
                request.getTransactionId(), request.getDescription());
        
        final var auditEntity = createAuditEntity(request, context);
        final var savedAudit = auditRepository.save(auditEntity);
        
        log.warn("Failed transaction audit created: auditId={}, transactionId={}, reason={}", 
                savedAudit.getId(), request.getTransactionId(), request.getDescription());
        
    }


    private TransactionAuditEntity createAuditEntity(TransactionAuditRequest request, AuditContext context) {
        return TransactionAuditEntity.builder()
                .transactionId(request.getTransactionId())
                .walletId(request.getWalletId())
                .userId(request.getUserId())
                .operationType(request.getOperationType())
                .amount(request.getAmount())
                .balanceBefore(request.getBalanceBefore())
                .balanceAfter(request.getBalanceAfter())
                .status(request.getStatus())
                .createdBy(context.getCreatedBy())
                .description(request.getDescription())
                .build()
                .withTechnicalContext(
                        context.getOriginIp(),
                        context.getUserAgent(),
                        context.getSessionId(),
                        context.getCorrelationId(),
                        context.getMetadata()
                );
    }

    private void validateAuditRequest(TransactionAuditRequest request) {
        Objects.requireNonNull(request, "Audit request cannot be null");
        Objects.requireNonNull(request.getTransactionId(), "Transaction ID cannot be null");
        Objects.requireNonNull(request.getWalletId(), "Wallet ID cannot be null");
        Objects.requireNonNull(request.getUserId(), "User ID cannot be null");
        Objects.requireNonNull(request.getOperationType(), "Operation type cannot be null");
        Objects.requireNonNull(request.getAmount(), "Amount cannot be null");
        Objects.requireNonNull(request.getBalanceBefore(), "Balance before cannot be null");
        Objects.requireNonNull(request.getStatus(), "Status cannot be null");
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        
        if (request.getBalanceBefore().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance before cannot be negative");
        }
    }

    private void validateAuditContext(AuditContext context) {
        Objects.requireNonNull(context, "Audit context cannot be null");
        Objects.requireNonNull(context.getCreatedBy(), "Created by cannot be null");
    }

    private TransactionAuditEntity buildAuditEntity(TransactionAuditRequest request, AuditContext context) {
        return TransactionAuditEntity.builder()
                .transactionId(request.getTransactionId())
                .walletId(request.getWalletId())
                .userId(request.getUserId())
                .operationType(request.getOperationType())
                .amount(request.getAmount())
                .balanceBefore(request.getBalanceBefore())
                .balanceAfter(request.getBalanceAfter())
                .status(request.getStatus())
                .createdBy(context.getCreatedBy())
                .description(request.getDescription())
                .build()
                .withTechnicalContext(
                        context.getOriginIp(),
                        context.getUserAgent(),
                        context.getSessionId(),
                        context.getCorrelationId(),
                        serializeMetadata(null)
                );
    }
    
    private void setupMDC(TransactionAuditRequest request, AuditContext context) {
        MDC.put("correlationId", context.getCorrelationId());
        MDC.put("transactionId", request.getTransactionId().toString());
        MDC.put("walletId", request.getWalletId().toString());
    }
    
    private void clearMDC() {
        MDC.clear();
    }
    
    private String serializeMetadata(Object metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Failed to serialize metadata: {}", e.getMessage());
            return null;
        }
    }

}