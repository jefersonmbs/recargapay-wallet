package br.com.jefersonmbs.recargapaywallet.domain.service;

import br.com.jefersonmbs.recargapaywallet.domain.dto.AuditContext;
import br.com.jefersonmbs.recargapaywallet.domain.dto.TransactionAuditRequest;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionAuditEntity;


public interface TransactionAuditService {

    TransactionAuditEntity audit(TransactionAuditRequest request);

    void auditTransactionStart(
            TransactionAuditRequest request,
            AuditContext context);

    void auditSuccessful(
            TransactionAuditRequest request,
            AuditContext context);

    void auditFailed(
            TransactionAuditRequest request,
            AuditContext context);


}