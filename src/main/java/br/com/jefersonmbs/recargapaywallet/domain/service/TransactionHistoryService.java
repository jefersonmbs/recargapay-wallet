package br.com.jefersonmbs.recargapaywallet.domain.service;

import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.dto.TransactionCreationRequest;

public interface TransactionHistoryService {

    TransactionHistoryEntity createTransaction(TransactionCreationRequest request);
}