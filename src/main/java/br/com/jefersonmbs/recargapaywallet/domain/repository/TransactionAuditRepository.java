package br.com.jefersonmbs.recargapaywallet.domain.repository;

import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionAuditRepository extends JpaRepository<TransactionAuditEntity, UUID> {

}