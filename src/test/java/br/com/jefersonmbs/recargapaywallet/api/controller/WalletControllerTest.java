package br.com.jefersonmbs.recargapaywallet.api.controller;

import br.com.jefersonmbs.recargapaywallet.api.dto.PagedTransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionHistoryRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private WalletResponseDto testWalletResponse;
    private TransactionRequestDto testTransactionRequest;
    private TransactionResponseDto testTransactionResponse;
    private PagedTransactionResponseDto testPagedResponse;
    private final Long testUserId = 1L;
    private final UUID testWalletId = UUID.randomUUID();
    private final Long testAccountNumber = 9891L;

    @BeforeEach
    void setUp() {
        testWalletResponse = WalletResponseDto.builder()
            .id(testWalletId)
            .accountNumber(testAccountNumber)
            .balance(BigDecimal.valueOf(1000.00))
            .userId(testUserId)
            .active(true)
            .build();

        testTransactionRequest = TransactionRequestDto.builder()
            .amount(BigDecimal.valueOf(100.00))
            .targetWalletId(testWalletId)
            .description("Test transaction")
            .build();

        testTransactionResponse = TransactionResponseDto.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(100.00))
            .targetWalletId(testWalletId)
            .description("Test transaction")
            .build();

        testPagedResponse = PagedTransactionResponseDto.builder()
            .content(Collections.singletonList(testTransactionResponse))
            .page(0)
            .size(20)
            .totalPages(1)
            .totalElements(1L)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();
    }

    @Test
    void createWallet_ShouldReturnCreatedStatus_WhenWalletCreatedSuccessfully() {
        when(walletService.createWallet(testUserId)).thenReturn(testWalletResponse);

        ResponseEntity<WalletResponseDto> response = walletController.createWallet(testUserId);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(testWalletResponse);
        verify(walletService).createWallet(testUserId);
    }

    @Test
    void createWallet_ShouldHandleLargeUserId() {
        Long largeUserId = 999999999L;
        WalletResponseDto largeUserWalletResponse = WalletResponseDto.builder()
            .id(UUID.randomUUID())
            .accountNumber(989999999999L)
            .balance(BigDecimal.ZERO)
            .userId(largeUserId)
            .active(true)
            .build();

        when(walletService.createWallet(largeUserId)).thenReturn(largeUserWalletResponse);

        ResponseEntity<WalletResponseDto> response = walletController.createWallet(largeUserId);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(largeUserWalletResponse);
        verify(walletService).createWallet(largeUserId);
    }

    @Test
    void getWalletByAccountNumber_ShouldReturnOkStatus_WhenWalletExists() {
        when(walletService.getWalletByAccountNumber(testAccountNumber)).thenReturn(testWalletResponse);

        ResponseEntity<WalletResponseDto> response = walletController.getWalletByAccountNumber(testAccountNumber);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testWalletResponse);
        verify(walletService).getWalletByAccountNumber(testAccountNumber);
    }

    @Test
    void getWalletByAccountNumber_ShouldHandleLargeAccountNumber() {
        Long largeAccountNumber = 989999999999L;
        WalletResponseDto largeAccountWalletResponse = WalletResponseDto.builder()
            .id(testWalletId)
            .accountNumber(largeAccountNumber)
            .balance(BigDecimal.valueOf(5000.00))
            .userId(testUserId)
            .active(true)
            .build();

        when(walletService.getWalletByAccountNumber(largeAccountNumber)).thenReturn(largeAccountWalletResponse);

        ResponseEntity<WalletResponseDto> response = walletController.getWalletByAccountNumber(largeAccountNumber);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(largeAccountWalletResponse);
        verify(walletService).getWalletByAccountNumber(largeAccountNumber);
    }

    @Test
    void getAllActiveWallets_ShouldReturnOkStatus_WhenWalletsExist() {
        List<WalletResponseDto> activeWallets = Collections.singletonList(testWalletResponse);
        when(walletService.getAllActiveWallets()).thenReturn(activeWallets);

        ResponseEntity<List<WalletResponseDto>> response = walletController.getAllActiveWallets();

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(activeWallets);
        assertThat(response.getBody()).hasSize(1);
        verify(walletService).getAllActiveWallets();
    }

    @Test
    void getAllActiveWallets_ShouldReturnEmptyList_WhenNoActiveWallets() {
        List<WalletResponseDto> emptyList = Collections.emptyList();
        when(walletService.getAllActiveWallets()).thenReturn(emptyList);

        ResponseEntity<List<WalletResponseDto>> response = walletController.getAllActiveWallets();

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(emptyList);
        assertThat(response.getBody()).isEmpty();
        verify(walletService).getAllActiveWallets();
    }

    @Test
    void deposit_ShouldReturnOkStatus_WhenDepositSuccessful() {
        when(walletService.deposit(testTransactionRequest)).thenReturn(testTransactionResponse);

        ResponseEntity<TransactionResponseDto> response = walletController.deposit(testTransactionRequest);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testTransactionResponse);
        verify(walletService).deposit(testTransactionRequest);
    }

    @Test
    void deposit_ShouldHandleLargeAmount() {
        TransactionRequestDto largeAmountRequest = TransactionRequestDto.builder()
            .amount(new BigDecimal("999999.99"))
            .targetWalletId(testWalletId)
            .description("Large deposit")
            .build();

        TransactionResponseDto largeAmountResponse = TransactionResponseDto.builder()
            .id(UUID.randomUUID())
            .amount(new BigDecimal("999999.99"))
            .targetWalletId(testWalletId)
            .description("Large deposit")
            .build();

        when(walletService.deposit(largeAmountRequest)).thenReturn(largeAmountResponse);

        ResponseEntity<TransactionResponseDto> response = walletController.deposit(largeAmountRequest);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(largeAmountResponse);
        verify(walletService).deposit(largeAmountRequest);
    }

    @Test
    void withdraw_ShouldReturnOkStatus_WhenWithdrawSuccessful() {
        TransactionRequestDto withdrawRequest = TransactionRequestDto.builder()
            .amount(BigDecimal.valueOf(150.00))
            .sourceWalletId(testWalletId)
            .description("Test withdrawal")
            .build();

        TransactionResponseDto withdrawResponse = TransactionResponseDto.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(150.00))
            .sourceWalletId(testWalletId)
            .description("Test withdrawal")
            .build();

        when(walletService.withdraw(withdrawRequest)).thenReturn(withdrawResponse);

        ResponseEntity<TransactionResponseDto> response = walletController.withdraw(withdrawRequest);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(withdrawResponse);
        verify(walletService).withdraw(withdrawRequest);
    }

    @Test
    void withdraw_ShouldHandleSmallAmount() {
        TransactionRequestDto smallAmountRequest = TransactionRequestDto.builder()
            .amount(new BigDecimal("0.01"))
            .sourceWalletId(testWalletId)
            .description("Small withdrawal")
            .build();

        TransactionResponseDto smallAmountResponse = TransactionResponseDto.builder()
            .id(UUID.randomUUID())
            .amount(new BigDecimal("0.01"))
            .sourceWalletId(testWalletId)
            .description("Small withdrawal")
            .build();

        when(walletService.withdraw(smallAmountRequest)).thenReturn(smallAmountResponse);

        ResponseEntity<TransactionResponseDto> response = walletController.withdraw(smallAmountRequest);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(smallAmountResponse);
        verify(walletService).withdraw(smallAmountRequest);
    }

    @Test
    void transfer_ShouldReturnOkStatus_WhenTransferSuccessful() {
        UUID targetWalletId = UUID.randomUUID();
        TransactionRequestDto transferRequest = TransactionRequestDto.builder()
            .amount(BigDecimal.valueOf(200.00))
            .sourceWalletId(testWalletId)
            .targetWalletId(targetWalletId)
            .description("Test transfer")
            .build();

        TransactionResponseDto transferResponse = TransactionResponseDto.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(200.00))
            .sourceWalletId(testWalletId)
            .targetWalletId(targetWalletId)
            .description("Test transfer")
            .build();

        when(walletService.transfer(transferRequest)).thenReturn(transferResponse);

        ResponseEntity<TransactionResponseDto> response = walletController.transfer(transferRequest);

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(transferResponse);
        verify(walletService).transfer(transferRequest);
    }

    @Test
    void getTransactionHistory_ShouldReturnOkStatus_WithDefaultParameters() {
        when(walletService.getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class)))
            .thenReturn(testPagedResponse);

        ResponseEntity<PagedTransactionResponseDto> response = walletController.getTransactionHistory(
            testWalletId, testUserId, 0, 20, null, null, "createdAt", "DESC");

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testPagedResponse);
        verify(walletService).getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class));
    }

    @Test
    void getTransactionHistory_ShouldReturnOkStatus_WithDateRange() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        when(walletService.getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class)))
            .thenReturn(testPagedResponse);

        ResponseEntity<PagedTransactionResponseDto> response = walletController.getTransactionHistory(
            testWalletId, testUserId, 0, 20, startDate, endDate, "createdAt", "DESC");

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testPagedResponse);
        verify(walletService).getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class));
    }

    @Test
    void getTransactionHistory_ShouldReturnOkStatus_WithCustomPagination() {
        PagedTransactionResponseDto customPagedResponse = PagedTransactionResponseDto.builder()
            .content(Collections.emptyList())
            .page(2)
            .size(50)
            .totalPages(3)
            .totalElements(100L)
            .first(false)
            .last(false)
            .hasNext(true)
            .hasPrevious(true)
            .build();

        when(walletService.getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class)))
            .thenReturn(customPagedResponse);

        ResponseEntity<PagedTransactionResponseDto> response = walletController.getTransactionHistory(
            testWalletId, testUserId, 2, 50, null, null, "amount", "ASC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(customPagedResponse);
        verify(walletService).getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class));
    }

    @Test
    void getTransactionHistory_ShouldHandleAllSortingOptions() {
        when(walletService.getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class)))
            .thenReturn(testPagedResponse);

        ResponseEntity<PagedTransactionResponseDto> response = walletController.getTransactionHistory(
            testWalletId, testUserId, 0, 10, null, null, "type", "ASC");

        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testPagedResponse);
        verify(walletService).getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class));
    }

    @Test
    void toggleActiveWallet_ShouldReturnNoContentStatus_WhenToggleSuccessful() {
        doNothing().when(walletService).toggleActiveWallet(testWalletId);

        ResponseEntity<Void> response = walletController.toggleActiveWallet(testWalletId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(walletService).toggleActiveWallet(testWalletId);
    }

    @Test
    void toggleActiveWallet_ShouldHandleRandomUUID() {
        UUID randomWalletId = UUID.randomUUID();
        doNothing().when(walletService).toggleActiveWallet(randomWalletId);

        ResponseEntity<Void> response = walletController.toggleActiveWallet(randomWalletId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(walletService).toggleActiveWallet(randomWalletId);
    }

    @Test
    void deposit_ShouldHandleNullDescription() {
        TransactionRequestDto requestWithoutDescription = TransactionRequestDto.builder()
            .amount(BigDecimal.valueOf(100.00))
            .targetWalletId(testWalletId)
            .build();

        TransactionResponseDto responseWithoutDescription = TransactionResponseDto.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(100.00))
            .targetWalletId(testWalletId)
            .build();

        when(walletService.deposit(requestWithoutDescription)).thenReturn(responseWithoutDescription);

        ResponseEntity<TransactionResponseDto> response = walletController.deposit(requestWithoutDescription);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseWithoutDescription);
        verify(walletService).deposit(requestWithoutDescription);
    }

    @Test
    void getTransactionHistory_ShouldHandleZeroPage() {
        PagedTransactionResponseDto firstPageResponse = PagedTransactionResponseDto.builder()
            .content(Collections.singletonList(testTransactionResponse))
            .page(0)
            .size(20)
            .totalPages(1)
            .totalElements(1L)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        when(walletService.getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class)))
            .thenReturn(firstPageResponse);

        ResponseEntity<PagedTransactionResponseDto> response = walletController.getTransactionHistory(
            testWalletId, testUserId, 0, 20, null, null, "createdAt", "DESC");


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(walletService).getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class));
    }

    @Test
    void getTransactionHistory_ShouldHandleEmptyResults() {
        PagedTransactionResponseDto emptyPageResponse = PagedTransactionResponseDto.builder()
            .content(Collections.emptyList())
            .page(0)
            .size(20)
            .totalPages(0)
            .totalElements(0L)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        when(walletService.getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class)))
            .thenReturn(emptyPageResponse);

        ResponseEntity<PagedTransactionResponseDto> response = walletController.getTransactionHistory(
            testWalletId, testUserId, 0, 20, null, null, "createdAt", "DESC");


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0L);
        verify(walletService).getTransactionHistoryPaginated(eq(testWalletId), eq(testUserId), any(TransactionHistoryRequestDto.class));
    }
}