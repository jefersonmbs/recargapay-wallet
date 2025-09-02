package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletBalanceServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletBalanceServiceImpl walletBalanceService;

    private WalletEntity testWallet;

    @BeforeEach
    void setUp() {
        testWallet = WalletEntity.builder()
            .id(UUID.randomUUID())
            .accountNumber(9891L)
            .balance(BigDecimal.valueOf(1000.00))
            .active(true)
            .build();
    }

    @Test
    void updateBalance_ShouldUpdateWalletBalanceAndSave() {
        BigDecimal newBalance = BigDecimal.valueOf(1500.00);
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.updateBalance(testWallet, newBalance);

        assertThat(testWallet.getBalance()).isEqualTo(newBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void updateBalance_ShouldHandleZeroBalance() {
        BigDecimal zeroBalance = BigDecimal.ZERO;
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.updateBalance(testWallet, zeroBalance);

        assertThat(testWallet.getBalance()).isEqualTo(BigDecimal.ZERO);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void updateBalance_ShouldHandleLargeBalance() {
        BigDecimal largeBalance = new BigDecimal("999999999.99");
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.updateBalance(testWallet, largeBalance);

        assertThat(testWallet.getBalance()).isEqualTo(largeBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void creditAmount_ShouldAddAmountToCurrentBalance() {
        BigDecimal currentBalance = testWallet.getBalance(); // 1000.00
        BigDecimal creditAmount = BigDecimal.valueOf(250.00);
        BigDecimal expectedBalance = currentBalance.add(creditAmount); // 1250.00
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.creditAmount(testWallet, creditAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void creditAmount_ShouldHandleZeroCreditAmount() {
        BigDecimal originalBalance = testWallet.getBalance(); // 1000.00
        BigDecimal creditAmount = BigDecimal.ZERO;
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.creditAmount(testWallet, creditAmount);

        assertThat(testWallet.getBalance()).isEqualTo(originalBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void creditAmount_ShouldHandleSmallDecimalAmount() {
        BigDecimal originalBalance = testWallet.getBalance(); // 1000.00
        BigDecimal creditAmount = new BigDecimal("0.01");
        BigDecimal expectedBalance = originalBalance.add(creditAmount); // 1000.01
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.creditAmount(testWallet, creditAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void creditAmount_ShouldHandleLargeCreditAmount() {
        BigDecimal originalBalance = testWallet.getBalance(); // 1000.00
        BigDecimal creditAmount = new BigDecimal("50000.00");
        BigDecimal expectedBalance = originalBalance.add(creditAmount); // 51000.00
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.creditAmount(testWallet, creditAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void debitAmount_ShouldSubtractAmountFromCurrentBalance() {
        BigDecimal currentBalance = testWallet.getBalance(); // 1000.00
        BigDecimal debitAmount = BigDecimal.valueOf(300.00);
        BigDecimal expectedBalance = currentBalance.subtract(debitAmount); // 700.00
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.debitAmount(testWallet, debitAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void debitAmount_ShouldHandleZeroDebitAmount() {
        BigDecimal originalBalance = testWallet.getBalance(); // 1000.00
        BigDecimal debitAmount = BigDecimal.ZERO;
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.debitAmount(testWallet, debitAmount);

        assertThat(testWallet.getBalance()).isEqualTo(originalBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void debitAmount_ShouldHandleSmallDecimalAmount() {
        BigDecimal originalBalance = testWallet.getBalance(); // 1000.00
        BigDecimal debitAmount = new BigDecimal("0.01");
        BigDecimal expectedBalance = originalBalance.subtract(debitAmount); // 999.99
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.debitAmount(testWallet, debitAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void debitAmount_ShouldAllowNegativeBalanceResult() {
        BigDecimal originalBalance = testWallet.getBalance(); // 1000.00
        BigDecimal debitAmount = BigDecimal.valueOf(1500.00);
        BigDecimal expectedBalance = originalBalance.subtract(debitAmount); // -500.00
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.debitAmount(testWallet, debitAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        assertThat(testWallet.getBalance()).isNegative();
        verify(walletRepository).save(testWallet);
    }

    @Test
    void debitAmount_ShouldHandleCompleteWithdrawal() {
        BigDecimal debitAmount = testWallet.getBalance();
        BigDecimal expectedBalance = BigDecimal.valueOf(0.0);
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.debitAmount(testWallet, debitAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void creditAmount_ShouldWorkWithZeroInitialBalance() {
        testWallet.setBalance(BigDecimal.ZERO);
        BigDecimal creditAmount = BigDecimal.valueOf(500.00);
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.creditAmount(testWallet, creditAmount);

        assertThat(testWallet.getBalance()).isEqualTo(creditAmount);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void debitAmount_ShouldWorkWithZeroInitialBalance() {
        testWallet.setBalance(BigDecimal.ZERO);
        BigDecimal debitAmount = BigDecimal.valueOf(100.00);
        BigDecimal expectedBalance = BigDecimal.ZERO.subtract(debitAmount); // -100.00
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.debitAmount(testWallet, debitAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        assertThat(testWallet.getBalance()).isNegative();
        verify(walletRepository).save(testWallet);
    }

    @Test
    void updateBalance_ShouldPreservePrecision() {
        BigDecimal preciseBalance = new BigDecimal("1234.567890");
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.updateBalance(testWallet, preciseBalance);

        assertThat(testWallet.getBalance()).isEqualTo(preciseBalance);
        assertThat(testWallet.getBalance().scale()).isEqualTo(6);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void creditAmount_ShouldMaintainPrecisionInCalculation() {
        testWallet.setBalance(new BigDecimal("999.99"));
        BigDecimal creditAmount = new BigDecimal("0.01");
        BigDecimal expectedBalance = new BigDecimal("1000.00");
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.creditAmount(testWallet, creditAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void debitAmount_ShouldMaintainPrecisionInCalculation() {
        testWallet.setBalance(new BigDecimal("1000.00"));
        BigDecimal debitAmount = new BigDecimal("0.01");
        BigDecimal expectedBalance = new BigDecimal("999.99");
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletBalanceService.debitAmount(testWallet, debitAmount);

        assertThat(testWallet.getBalance()).isEqualTo(expectedBalance);
        verify(walletRepository).save(testWallet);
    }
}