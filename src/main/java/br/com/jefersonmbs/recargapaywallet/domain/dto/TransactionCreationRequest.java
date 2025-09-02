package br.com.jefersonmbs.recargapaywallet.domain.dto;

import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity.TransactionType;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionCreationRequest(TransactionType type, BigDecimal amount, WalletEntity sourceWallet,
                                         WalletEntity targetWallet, String description, BigDecimal balanceBefore,
                                         BigDecimal balanceAfter, String correlationId) {
}