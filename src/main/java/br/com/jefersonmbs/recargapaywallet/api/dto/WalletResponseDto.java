package br.com.jefersonmbs.recargapaywallet.api.dto;

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
public class WalletResponseDto {

    private UUID id;
    private Long accountNumber;
    private BigDecimal balance;
    private Long userId;
    private String userCpf;
    private String userName;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}