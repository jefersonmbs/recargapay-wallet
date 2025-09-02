package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.repository.WalletRepository;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletBalanceServiceImpl implements WalletBalanceService {
    
    private final WalletRepository walletRepository;
    
    @Override
    public void updateBalance(WalletEntity wallet, BigDecimal newBalance) {
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
    }
    
    @Override
    public void creditAmount(WalletEntity wallet, BigDecimal amount) {
        BigDecimal newBalance = wallet.getBalance().add(amount);
        updateBalance(wallet, newBalance);
    }
    
    @Override
    public void debitAmount(WalletEntity wallet, BigDecimal amount) {
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        updateBalance(wallet, newBalance);
    }
}