package br.com.jefersonmbs.recargapaywallet.api.dto;

import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDto {

    @NotNull(message = "Transaction type is required")
    private TransactionHistoryEntity.TransactionType type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private UUID sourceWalletId;

    private UUID targetWalletId;

    private Long targetAccountNumber;

    private String targetUserCpf;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String correlationId;
}