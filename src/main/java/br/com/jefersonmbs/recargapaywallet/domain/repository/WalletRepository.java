package br.com.jefersonmbs.recargapaywallet.domain.repository;

import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {

    Optional<WalletEntity> findByAccountNumber(Long accountNumber);

    Optional<WalletEntity> findByUserId(Long userId);

    Optional<WalletEntity> findByUserCpf(String cpf);

    @Query("SELECT w FROM WalletEntity w WHERE w.active = :active")
    List<WalletEntity> findByActive(@Param("active") Boolean active);

    @Query("SELECT w FROM WalletEntity w WHERE w.user.active = true AND w.active = true")
    List<WalletEntity> findActiveWalletsWithActiveUsers();

    boolean existsByUserId(Long userId);

}