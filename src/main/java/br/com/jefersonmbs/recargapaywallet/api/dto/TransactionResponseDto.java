package br.com.jefersonmbs.recargapaywallet.api.dto;

import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {

    private UUID id;
    private TransactionHistoryEntity.TransactionType type;
    private BigDecimal amount;
    private UUID sourceWalletId;
    private Long sourceAccountNumber;
    private String sourceUserCpf;
    private String sourceUserName;
    private UUID targetWalletId;
    private Long targetAccountNumber;
    private String targetUserCpf;
    private String targetUserName;
    private String description;
    private BigDecimal balanceBeforeTransaction;
    private BigDecimal balanceAfterTransaction;
    private TransactionHistoryEntity.TransactionStatus status;
    private LocalDateTime createdAt;
}