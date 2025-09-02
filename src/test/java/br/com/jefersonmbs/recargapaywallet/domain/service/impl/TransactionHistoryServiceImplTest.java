package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.domain.dto.TransactionCreationRequest;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.repository.TransactionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryServiceImplTest {

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @InjectMocks
    private TransactionHistoryServiceImpl transactionHistoryService;

    private TransactionCreationRequest testRequest;
    private WalletEntity sourceWallet;
    private WalletEntity targetWallet;
    private TransactionHistoryEntity savedTransaction;

    @BeforeEach
    void setUp() {
        sourceWallet = WalletEntity.builder()
            .id(UUID.randomUUID())
            .accountNumber(9891L)
            .balance(BigDecimal.valueOf(1000.00))
            .active(true)
            .build();

        targetWallet = WalletEntity.builder()
            .id(UUID.randomUUID())
            .accountNumber(9892L)
            .balance(BigDecimal.valueOf(500.00))
            .active(true)
            .build();

        testRequest = new TransactionCreationRequest(
            TransactionHistoryEntity.TransactionType.TRANSFER,
            BigDecimal.valueOf(100.00),
            sourceWallet,
            targetWallet,
            "Test transfer transaction",
            BigDecimal.valueOf(1000.00),
            BigDecimal.valueOf(900.00)
        );

        savedTransaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.TRANSFER)
            .amount(BigDecimal.valueOf(100.00))
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .description("Test transfer transaction")
            .balanceBeforeTransaction(BigDecimal.valueOf(1000.00))
            .balanceAfterTransaction(BigDecimal.valueOf(900.00))
            .status(TransactionHistoryEntity.TransactionStatus.COMPLETED)
            .build();
    }

    @Test
    void createTransaction_ShouldCreateAndSaveTransaction_WhenValidRequest() {
        when(transactionHistoryRepository.save(any(TransactionHistoryEntity.class))).thenReturn(savedTransaction);

        TransactionHistoryEntity result = transactionHistoryService.createTransaction(testRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(savedTransaction);

        ArgumentCaptor<TransactionHistoryEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionHistoryEntity.class);
        verify(transactionHistoryRepository).save(transactionCaptor.capture());

        TransactionHistoryEntity capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getType()).isEqualTo(TransactionHistoryEntity.TransactionType.TRANSFER);
        assertThat(capturedTransaction.getBalanceBeforeTransaction()).isEqualTo(BigDecimal.valueOf(1000.00));
        assertThat(capturedTransaction.getBalanceAfterTransaction()).isEqualTo(BigDecimal.valueOf(900.00));
    }

    @Test
    void createTransaction_ShouldCreateDepositTransaction_WhenDepositRequest() {
        TransactionCreationRequest depositRequest = new TransactionCreationRequest(
            TransactionHistoryEntity.TransactionType.DEPOSIT,
            BigDecimal.valueOf(200.00),
            null,
            targetWallet,
            "Deposit transaction",
            BigDecimal.valueOf(500.00),
            BigDecimal.valueOf(700.00)
        );

        TransactionHistoryEntity depositTransaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.DEPOSIT)
            .amount(BigDecimal.valueOf(200.00))
            .sourceWallet(null)
            .targetWallet(targetWallet)
            .description("Deposit transaction")
            .balanceBeforeTransaction(BigDecimal.valueOf(500.00))
            .balanceAfterTransaction(BigDecimal.valueOf(700.00))
            .status(TransactionHistoryEntity.TransactionStatus.COMPLETED)
            .build();

        when(transactionHistoryRepository.save(any(TransactionHistoryEntity.class))).thenReturn(depositTransaction);

        TransactionHistoryEntity result = transactionHistoryService.createTransaction(depositRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(depositTransaction);

        ArgumentCaptor<TransactionHistoryEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionHistoryEntity.class);
        verify(transactionHistoryRepository).save(transactionCaptor.capture());

        TransactionHistoryEntity capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getType()).isEqualTo(TransactionHistoryEntity.TransactionType.DEPOSIT);
        assertThat(capturedTransaction.getBalanceBeforeTransaction()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(capturedTransaction.getBalanceAfterTransaction()).isEqualTo(BigDecimal.valueOf(700.00));
    }

    @Test
    void createTransaction_ShouldCreateWithdrawalTransaction_WhenWithdrawRequest() {
        TransactionCreationRequest withdrawRequest = new TransactionCreationRequest(
            TransactionHistoryEntity.TransactionType.WITHDRAWAL,
            BigDecimal.valueOf(150.00),
            sourceWallet,
            null,
            "Withdrawal transaction",
            BigDecimal.valueOf(1000.00),
            BigDecimal.valueOf(850.00)
        );

        TransactionHistoryEntity withdrawTransaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.WITHDRAWAL)
            .amount(BigDecimal.valueOf(150.00))
            .sourceWallet(sourceWallet)
            .targetWallet(null)
            .description("Withdrawal transaction")
            .balanceBeforeTransaction(BigDecimal.valueOf(1000.00))
            .balanceAfterTransaction(BigDecimal.valueOf(850.00))
            .status(TransactionHistoryEntity.TransactionStatus.COMPLETED)
            .build();

        when(transactionHistoryRepository.save(any(TransactionHistoryEntity.class))).thenReturn(withdrawTransaction);

        TransactionHistoryEntity result = transactionHistoryService.createTransaction(withdrawRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(withdrawTransaction);

        ArgumentCaptor<TransactionHistoryEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionHistoryEntity.class);
        verify(transactionHistoryRepository).save(transactionCaptor.capture());

        TransactionHistoryEntity capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getType()).isEqualTo(TransactionHistoryEntity.TransactionType.WITHDRAWAL);
        assertThat(capturedTransaction.getBalanceAfterTransaction()).isEqualTo(BigDecimal.valueOf(850.00));
    }

    @Test
    void createTransaction_ShouldCreateTransactionWithNullDescription_WhenDescriptionNotProvided() {
        TransactionCreationRequest requestWithoutDescription = new TransactionCreationRequest(
            TransactionHistoryEntity.TransactionType.TRANSFER,
            BigDecimal.valueOf(100.00),
            sourceWallet,
            targetWallet,
            null,
            BigDecimal.valueOf(1000.00),
            BigDecimal.valueOf(900.00)
        );

        TransactionHistoryEntity transactionWithoutDescription = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.TRANSFER)
            .amount(BigDecimal.valueOf(100.00))
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .description(null)
            .balanceBeforeTransaction(BigDecimal.valueOf(1000.00))
            .balanceAfterTransaction(BigDecimal.valueOf(900.00))
            .status(TransactionHistoryEntity.TransactionStatus.COMPLETED)
            .build();

        when(transactionHistoryRepository.save(any(TransactionHistoryEntity.class))).thenReturn(transactionWithoutDescription);

        TransactionHistoryEntity result = transactionHistoryService.createTransaction(requestWithoutDescription);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(transactionWithoutDescription);

        ArgumentCaptor<TransactionHistoryEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionHistoryEntity.class);
        verify(transactionHistoryRepository).save(transactionCaptor.capture());

        TransactionHistoryEntity capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getDescription()).isNull();
    }

    @Test
    void createTransaction_ShouldCreateTransferOutTransaction_WhenTransferOutRequest() {
        TransactionCreationRequest transferOutRequest = new TransactionCreationRequest(
            TransactionHistoryEntity.TransactionType.TRANSFER_OUT,
            BigDecimal.valueOf(250.00),
            sourceWallet,
            targetWallet,
            "Transfer out transaction",
            BigDecimal.valueOf(1000.00),
            BigDecimal.valueOf(750.00)
        );

        TransactionHistoryEntity transferOutTransaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.TRANSFER_OUT)
            .amount(BigDecimal.valueOf(250.00))
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .description("Transfer out transaction")
            .balanceBeforeTransaction(BigDecimal.valueOf(1000.00))
            .balanceAfterTransaction(BigDecimal.valueOf(750.00))
            .status(TransactionHistoryEntity.TransactionStatus.COMPLETED)
            .build();

        when(transactionHistoryRepository.save(any(TransactionHistoryEntity.class))).thenReturn(transferOutTransaction);

        TransactionHistoryEntity result = transactionHistoryService.createTransaction(transferOutRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(transferOutTransaction);

        ArgumentCaptor<TransactionHistoryEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionHistoryEntity.class);
        verify(transactionHistoryRepository).save(transactionCaptor.capture());

        TransactionHistoryEntity capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getType()).isEqualTo(TransactionHistoryEntity.TransactionType.TRANSFER_OUT);
    }

    @Test
    void createTransaction_ShouldCreateTransferInTransaction_WhenTransferInRequest() {
        TransactionCreationRequest transferInRequest = new TransactionCreationRequest(
            TransactionHistoryEntity.TransactionType.TRANSFER_IN,
            BigDecimal.valueOf(300.00),
            sourceWallet,
            targetWallet,
            "Transfer in transaction",
            BigDecimal.valueOf(500.00),
            BigDecimal.valueOf(800.00)
        );

        TransactionHistoryEntity transferInTransaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.TRANSFER_IN)
            .amount(BigDecimal.valueOf(300.00))
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .description("Transfer in transaction")
            .balanceBeforeTransaction(BigDecimal.valueOf(500.00))
            .balanceAfterTransaction(BigDecimal.valueOf(800.00))
            .status(TransactionHistoryEntity.TransactionStatus.COMPLETED)
            .build();

        when(transactionHistoryRepository.save(any(TransactionHistoryEntity.class))).thenReturn(transferInTransaction);

        TransactionHistoryEntity result = transactionHistoryService.createTransaction(transferInRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(transferInTransaction);

        ArgumentCaptor<TransactionHistoryEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionHistoryEntity.class);
        verify(transactionHistoryRepository).save(transactionCaptor.capture());

        TransactionHistoryEntity capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getType()).isEqualTo(TransactionHistoryEntity.TransactionType.TRANSFER_IN);
    }

    @Test
    void createTransaction_ShouldHandleZeroBalances_WhenBalancesAreZero() {
        TransactionCreationRequest zeroBalanceRequest = new TransactionCreationRequest(
            TransactionHistoryEntity.TransactionType.WITHDRAWAL,
            BigDecimal.valueOf(1000.00),
            sourceWallet,
            null,
            "Complete withdrawal",
            BigDecimal.valueOf(1000.00),
            BigDecimal.ZERO
        );

        TransactionHistoryEntity zeroBalanceTransaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.WITHDRAWAL)
            .amount(BigDecimal.valueOf(1000.00))
            .sourceWallet(sourceWallet)
            .targetWallet(null)
            .description("Complete withdrawal")
            .balanceBeforeTransaction(BigDecimal.valueOf(1000.00))
            .balanceAfterTransaction(BigDecimal.ZERO)
            .status(TransactionHistoryEntity.TransactionStatus.COMPLETED)
            .build();

        when(transactionHistoryRepository.save(any(TransactionHistoryEntity.class))).thenReturn(zeroBalanceTransaction);

        TransactionHistoryEntity result = transactionHistoryService.createTransaction(zeroBalanceRequest);

        assertThat(result).isNotNull();
        assertThat(result.getBalanceAfterTransaction()).isEqualTo(BigDecimal.ZERO);

        ArgumentCaptor<TransactionHistoryEntity> transactionCaptor = ArgumentCaptor.forClass(TransactionHistoryEntity.class);
        verify(transactionHistoryRepository).save(transactionCaptor.capture());

        TransactionHistoryEntity capturedTransaction = transactionCaptor.getValue();
        assertThat(capturedTransaction.getBalanceAfterTransaction()).isEqualTo(BigDecimal.ZERO);
    }
}