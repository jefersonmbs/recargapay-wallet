package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.PagedTransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionHistoryRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.mapper.TransactionMapper;
import br.com.jefersonmbs.recargapaywallet.api.mapper.WalletMapper;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.UserEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.exception.WalletNotFoundException;
import br.com.jefersonmbs.recargapaywallet.domain.factory.TransactionStrategyFactory;
import br.com.jefersonmbs.recargapaywallet.domain.repository.TransactionHistoryRepository;
import br.com.jefersonmbs.recargapaywallet.domain.repository.UserRepository;
import br.com.jefersonmbs.recargapaywallet.domain.repository.WalletRepository;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletFinderService;
import br.com.jefersonmbs.recargapaywallet.domain.strategy.TransactionStrategy;
import br.com.jefersonmbs.recargapaywallet.domain.validator.WalletValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private WalletValidator walletValidator;

    @Mock
    private TransactionStrategyFactory transactionStrategyFactory;

    @Mock
    private WalletFinderService walletFinderService;

    @Mock
    private TransactionStrategy depositStrategy;

    @Mock
    private TransactionStrategy withdrawStrategy;

    @Mock
    private TransactionStrategy transferStrategy;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UserEntity testUser;
    private WalletEntity testWallet;
    private WalletResponseDto testWalletResponse;
    private TransactionRequestDto testTransactionRequest;
    private TransactionResponseDto testTransactionResponse;
    private TransactionHistoryRequestDto testHistoryRequest;
    private final Long testUserId = 1L;
    private final UUID testWalletId = UUID.randomUUID();
    private final Long testAccountNumber = 9891L;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
            .id(testUserId)
            .name("Test User")
            .email("test@example.com")
            .cpf("12345678901")
            .phone("11999999999")
            .active(true)
            .build();

        testWallet = WalletEntity.builder()
            .id(testWalletId)
            .accountNumber(testAccountNumber)
            .balance(BigDecimal.valueOf(1000.00))
            .user(testUser)
            .active(true)
            .build();

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

        testHistoryRequest = TransactionHistoryRequestDto.builder()
            .page(0)
            .size(20)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
    }

    @Test
    void createWallet_ShouldReturnWalletResponseDto_WhenValidInput() {
        doNothing().when(walletValidator).validateUserId(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(walletValidator).validateWalletDoesNotExist(testUserId);
        when(walletRepository.save(any(WalletEntity.class))).thenReturn(testWallet);
        when(walletMapper.toResponseDto(testWallet)).thenReturn(testWalletResponse);

        WalletResponseDto result = walletService.createWallet(testUserId);

        assertThat(result).isEqualTo(testWalletResponse);
        verify(walletValidator).validateUserId(testUserId);
        verify(walletValidator).validateWalletDoesNotExist(testUserId);
        verify(userRepository).findById(testUserId);
        verify(walletRepository).save(any(WalletEntity.class));
        verify(walletMapper).toResponseDto(testWallet);
    }

    @Test
    void createWallet_ShouldThrowException_WhenUserNotFound() {
        doNothing().when(walletValidator).validateUserId(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.createWallet(testUserId))
            .isInstanceOf(WalletNotFoundException.class)
            .hasMessageContaining("User not found with ID: " + testUserId);

        verify(walletValidator).validateUserId(testUserId);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void createWallet_ShouldHandleAccountNumberGeneration_WhenUserIdIsLarge() {
        Long largeUserId = 999999999L;
        Long expectedAccountNumber = 989999999999L;
        
        UserEntity largeIdUser = UserEntity.builder()
            .id(largeUserId)
            .name("Large ID User")
            .email("large@example.com")
            .cpf("12345678902")
            .active(true)
            .build();

        WalletEntity largeIdWallet = WalletEntity.builder()
            .id(testWalletId)
            .accountNumber(expectedAccountNumber)
            .balance(BigDecimal.ZERO)
            .user(largeIdUser)
            .active(true)
            .build();

        doNothing().when(walletValidator).validateUserId(largeUserId);
        when(userRepository.findById(largeUserId)).thenReturn(Optional.of(largeIdUser));
        doNothing().when(walletValidator).validateWalletDoesNotExist(largeUserId);
        when(walletRepository.save(any(WalletEntity.class))).thenReturn(largeIdWallet);
        when(walletMapper.toResponseDto(largeIdWallet)).thenReturn(testWalletResponse);

        WalletResponseDto result = walletService.createWallet(largeUserId);

        assertThat(result).isEqualTo(testWalletResponse);
        verify(walletRepository).save(any(WalletEntity.class));
    }

    @Test
    void getWalletByAccountNumber_ShouldReturnWalletResponseDto_WhenWalletExists() {
        doNothing().when(walletValidator).validateAccountNumber(testAccountNumber);
        when(walletRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testWallet));
        when(walletMapper.toResponseDto(testWallet)).thenReturn(testWalletResponse);

        WalletResponseDto result = walletService.getWalletByAccountNumber(testAccountNumber);

        assertThat(result).isEqualTo(testWalletResponse);
        verify(walletValidator).validateAccountNumber(testAccountNumber);
        verify(walletRepository).findByAccountNumber(testAccountNumber);
        verify(walletMapper).toResponseDto(testWallet);
    }

    @Test
    void getWalletByAccountNumber_ShouldThrowException_WhenWalletNotFound() {
        doNothing().when(walletValidator).validateAccountNumber(testAccountNumber);
        when(walletRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWalletByAccountNumber(testAccountNumber))
            .isInstanceOf(WalletNotFoundException.class)
            .hasMessageContaining("Wallet not found for account number: " + testAccountNumber);

        verify(walletValidator).validateAccountNumber(testAccountNumber);
        verify(walletRepository).findByAccountNumber(testAccountNumber);
    }

    @Test
    void getAllActiveWallets_ShouldReturnActiveWalletsList() {
        List<WalletEntity> activeWallets = Collections.singletonList(testWallet);
        List<WalletResponseDto> expectedResponse = Collections.singletonList(testWalletResponse);
        
        when(walletRepository.findActiveWalletsWithActiveUsers()).thenReturn(activeWallets);
        when(walletMapper.toResponseDtoList(activeWallets)).thenReturn(expectedResponse);

        List<WalletResponseDto> result = walletService.getAllActiveWallets();

        assertThat(result).isEqualTo(expectedResponse);
        verify(walletRepository).findActiveWalletsWithActiveUsers();
        verify(walletMapper).toResponseDtoList(activeWallets);
    }

    @Test
    void deposit_ShouldReturnTransactionResponseDto() {
        when(transactionStrategyFactory.getDepositStrategy()).thenReturn(depositStrategy);
        when(depositStrategy.execute(testTransactionRequest)).thenReturn(testTransactionResponse);

        TransactionResponseDto result = walletService.deposit(testTransactionRequest);

        assertThat(result).isEqualTo(testTransactionResponse);
        verify(transactionStrategyFactory).getDepositStrategy();
        verify(depositStrategy).execute(testTransactionRequest);
    }

    @Test
    void withdraw_ShouldReturnTransactionResponseDto() {
        when(transactionStrategyFactory.getWithdrawStrategy()).thenReturn(withdrawStrategy);
        when(withdrawStrategy.execute(testTransactionRequest)).thenReturn(testTransactionResponse);

        TransactionResponseDto result = walletService.withdraw(testTransactionRequest);

        assertThat(result).isEqualTo(testTransactionResponse);
        verify(transactionStrategyFactory).getWithdrawStrategy();
        verify(withdrawStrategy).execute(testTransactionRequest);
    }

    @Test
    void transfer_ShouldReturnTransactionResponseDto() {
        when(transactionStrategyFactory.getTransferStrategy()).thenReturn(transferStrategy);
        when(transferStrategy.execute(testTransactionRequest)).thenReturn(testTransactionResponse);

        TransactionResponseDto result = walletService.transfer(testTransactionRequest);

        assertThat(result).isEqualTo(testTransactionResponse);
        verify(transactionStrategyFactory).getTransferStrategy();
        verify(transferStrategy).execute(testTransactionRequest);
    }

    @Test
    void getTransactionHistoryPaginated_ShouldReturnPagedResponse_WithoutDateFilter() {
        TransactionHistoryEntity transaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.DEPOSIT)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        List<TransactionHistoryEntity> transactions = Arrays.asList(transaction);
        Page<TransactionHistoryEntity> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 20), 1);
        List<TransactionResponseDto> transactionDtos = Arrays.asList(testTransactionResponse);

        doNothing().when(walletValidator).validateWalletId(testWalletId);
        doNothing().when(walletValidator).validateUserId(testUserId);
        doNothing().when(walletValidator).validateUserExists(testUserId);
        when(walletFinderService.findWalletById(testWalletId)).thenReturn(testWallet);
        doNothing().when(walletValidator).validateWalletOwnership(testWallet, testUserId);
        doNothing().when(walletValidator).validateDateRange(testHistoryRequest);
        when(transactionHistoryRepository.findByWalletIdPageable(eq(testWalletId), any(Pageable.class)))
            .thenReturn(transactionPage);
        when(transactionMapper.toResponseDtoList(transactions)).thenReturn(transactionDtos);

        PagedTransactionResponseDto result = walletService.getTransactionHistoryPaginated(
            testWalletId, testUserId, testHistoryRequest);

        
        assertThat(result.getContent()).isEqualTo(transactionDtos);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();

        verify(walletValidator).validateWalletId(testWalletId);
        verify(walletValidator).validateUserId(testUserId);
        verify(walletValidator).validateUserExists(testUserId);
        verify(walletFinderService).findWalletById(testWalletId);
        verify(walletValidator).validateWalletOwnership(testWallet, testUserId);
        verify(walletValidator).validateDateRange(testHistoryRequest);
        verify(transactionHistoryRepository).findByWalletIdPageable(eq(testWalletId), any(Pageable.class));
        verify(transactionMapper).toResponseDtoList(transactions);
    }

    @Test
    void getTransactionHistoryPaginated_ShouldReturnPagedResponse_WithDateFilter() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        
        TransactionHistoryRequestDto requestWithDates = TransactionHistoryRequestDto.builder()
            .page(0)
            .size(20)
            .startDate(startDate)
            .endDate(endDate)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();

        TransactionHistoryEntity transaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.DEPOSIT)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        List<TransactionHistoryEntity> transactions = Arrays.asList(transaction);
        Page<TransactionHistoryEntity> transactionPage = new PageImpl<>(transactions, PageRequest.of(0, 20), 1);
        List<TransactionResponseDto> transactionDtos = Arrays.asList(testTransactionResponse);

        doNothing().when(walletValidator).validateWalletId(testWalletId);
        doNothing().when(walletValidator).validateUserId(testUserId);
        doNothing().when(walletValidator).validateUserExists(testUserId);
        when(walletFinderService.findWalletById(testWalletId)).thenReturn(testWallet);
        doNothing().when(walletValidator).validateWalletOwnership(testWallet, testUserId);
        doNothing().when(walletValidator).validateDateRange(requestWithDates);
        when(transactionHistoryRepository.findByWalletIdAndDateRangePageable(
            eq(testWalletId), 
            eq(startDate.atStartOfDay()),
            eq(endDate.atTime(LocalTime.MAX)),
            any(Pageable.class))).thenReturn(transactionPage);
        when(transactionMapper.toResponseDtoList(transactions)).thenReturn(transactionDtos);

        PagedTransactionResponseDto result = walletService.getTransactionHistoryPaginated(
            testWalletId, testUserId, requestWithDates);


        assertThat(result.getContent()).isEqualTo(transactionDtos);
        verify(transactionHistoryRepository).findByWalletIdAndDateRangePageable(
            eq(testWalletId), 
            eq(startDate.atStartOfDay()),
            eq(endDate.atTime(LocalTime.MAX)),
            any(Pageable.class));
    }

    @Test
    void toggleActiveWallet_ShouldToggleWalletActiveStatus_WhenWalletIsActive() {
        doNothing().when(walletValidator).validateWalletId(testWalletId);
        when(walletFinderService.findWalletById(testWalletId)).thenReturn(testWallet);
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletService.toggleActiveWallet(testWalletId);

        assertThat(testWallet.getActive()).isFalse();
        verify(walletValidator).validateWalletId(testWalletId);
        verify(walletFinderService).findWalletById(testWalletId);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void toggleActiveWallet_ShouldToggleWalletActiveStatus_WhenWalletIsInactive() {
        testWallet.setActive(false);
        doNothing().when(walletValidator).validateWalletId(testWalletId);
        when(walletFinderService.findWalletById(testWalletId)).thenReturn(testWallet);
        when(walletRepository.save(testWallet)).thenReturn(testWallet);

        walletService.toggleActiveWallet(testWalletId);

        assertThat(testWallet.getActive()).isTrue();
        verify(walletValidator).validateWalletId(testWalletId);
        verify(walletFinderService).findWalletById(testWalletId);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void getTransactionHistoryPaginated_ShouldUseSortingCorrectly() {
        TransactionHistoryRequestDto customSortRequest = TransactionHistoryRequestDto.builder()
            .page(1)
            .size(10)
            .sortBy("amount")
            .sortDirection("ASC")
            .build();

        TransactionHistoryEntity transaction = TransactionHistoryEntity.builder()
            .id(UUID.randomUUID())
            .type(TransactionHistoryEntity.TransactionType.DEPOSIT)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        List<TransactionHistoryEntity> transactions = Arrays.asList(transaction);
        Page<TransactionHistoryEntity> transactionPage = new PageImpl<>(
            transactions, 
            PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "amount")), 
            1
        );
        List<TransactionResponseDto> transactionDtos = Collections.singletonList(testTransactionResponse);

        doNothing().when(walletValidator).validateWalletId(testWalletId);
        doNothing().when(walletValidator).validateUserId(testUserId);
        doNothing().when(walletValidator).validateUserExists(testUserId);
        when(walletFinderService.findWalletById(testWalletId)).thenReturn(testWallet);
        doNothing().when(walletValidator).validateWalletOwnership(testWallet, testUserId);
        doNothing().when(walletValidator).validateDateRange(customSortRequest);
        when(transactionHistoryRepository.findByWalletIdPageable(eq(testWalletId), any(Pageable.class)))
            .thenReturn(transactionPage);
        when(transactionMapper.toResponseDtoList(transactions)).thenReturn(transactionDtos);

        PagedTransactionResponseDto result = walletService.getTransactionHistoryPaginated(
            testWalletId, testUserId, customSortRequest);


        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        verify(transactionHistoryRepository).findByWalletIdPageable(eq(testWalletId), any(Pageable.class));
    }
}