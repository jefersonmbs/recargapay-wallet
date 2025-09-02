package br.com.jefersonmbs.recargapaywallet.domain.service;

import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;

import java.math.BigDecimal;

public interface WalletBalanceService {

    void updateBalance(WalletEntity wallet, BigDecimal newBalance);

    void creditAmount(WalletEntity wallet, BigDecimal amount);

    void debitAmount(WalletEntity wallet, BigDecimal amount);
}