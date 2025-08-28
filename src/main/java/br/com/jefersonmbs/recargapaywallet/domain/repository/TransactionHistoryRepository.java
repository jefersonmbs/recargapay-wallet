package br.com.jefersonmbs.recargapaywallet.domain.repository;

import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistoryEntity, UUID> {

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<TransactionHistoryEntity> findByWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE (t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId) ORDER BY t.createdAt DESC")
    Page<TransactionHistoryEntity> findByWalletIdPageable(@Param("walletId") UUID walletId, Pageable pageable);

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE t.sourceWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<TransactionHistoryEntity> findBySourceWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE t.targetWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<TransactionHistoryEntity> findByTargetWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE (t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId) AND t.type = :type ORDER BY t.createdAt DESC")
    List<TransactionHistoryEntity> findByWalletIdAndType(@Param("walletId") UUID walletId, @Param("type") TransactionHistoryEntity.TransactionType type);

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE (t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId) AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<TransactionHistoryEntity> findByWalletIdAndDateRange(@Param("walletId") UUID walletId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE (t.sourceWallet.id = :walletId OR t.targetWallet.id = :walletId) AND t.status = :status ORDER BY t.createdAt DESC")
    List<TransactionHistoryEntity> findByWalletIdAndStatus(@Param("walletId") UUID walletId, @Param("status") TransactionHistoryEntity.TransactionStatus status);

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE t.sourceWallet.user.cpf = :cpf OR t.targetWallet.user.cpf = :cpf ORDER BY t.createdAt DESC")
    List<TransactionHistoryEntity> findByUserCpf(@Param("cpf") String cpf);

    @Query("SELECT t FROM TransactionHistoryEntity t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<TransactionHistoryEntity> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}