package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.domain.dto.TransactionCreationRequest;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.repository.TransactionHistoryRepository;
import br.com.jefersonmbs.recargapaywallet.domain.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {
    
    private final TransactionHistoryRepository transactionHistoryRepository;
    
    @Override
    public TransactionHistoryEntity createTransaction(TransactionCreationRequest request) {
        TransactionHistoryEntity transaction = TransactionHistoryEntity.builder()
            .type(request.type())
            .amount(request.amount())
            .sourceWallet(request.sourceWallet())
            .targetWallet(request.targetWallet())
            .description(request.description())
            .balanceBeforeTransaction(request.balanceBefore())
            .balanceAfterTransaction(request.balanceAfter())
            .correlationId(request.correlationId())
            .build();
        
        return transactionHistoryRepository.save(transaction);
    }
}