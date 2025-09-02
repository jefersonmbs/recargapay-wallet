package br.com.jefersonmbs.recargapaywallet.domain.repository;

import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistoryEntity, UUID> {


    @Query("SELECT t FROM TransactionHistoryEntity t WHERE " +
           "(t.sourceWallet.id = :walletId AND t.type IN ('WITHDRAWAL', 'TRANSFER_OUT')) OR " +
           "(t.targetWallet.id = :walletId AND t.type IN ('DEPOSIT', 'TRANSFER_IN')) " +
           "ORDER BY t.createdAt DESC")
    Page<TransactionHistoryEntity> findByWalletIdPageable(@Param("walletId") UUID walletId, Pageable pageable);


    @Query("SELECT t FROM TransactionHistoryEntity t WHERE " +
           "((t.sourceWallet.id = :walletId AND t.type IN ('WITHDRAWAL', 'TRANSFER_OUT')) OR " +
           "(t.targetWallet.id = :walletId AND t.type IN ('DEPOSIT', 'TRANSFER_IN'))) AND " +
           "t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<TransactionHistoryEntity> findByWalletIdAndDateRangePageable(@Param("walletId") UUID walletId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}