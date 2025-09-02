package br.com.jefersonmbs.recargapaywallet.domain.repository;

import br.com.jefersonmbs.recargapaywallet.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    @Query("SELECT u FROM UserEntity u WHERE u.active = true")
    List<UserEntity> findAllActive();

    @Query("SELECT u FROM UserEntity u WHERE u.active = true AND u.id = :id")
    Optional<UserEntity> findActiveById(@Param("id") Long id);

    @Query("SELECT u FROM UserEntity u WHERE u.active = true AND LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserEntity> findActiveByNameContainingIgnoreCase(@Param("name") String name);
}