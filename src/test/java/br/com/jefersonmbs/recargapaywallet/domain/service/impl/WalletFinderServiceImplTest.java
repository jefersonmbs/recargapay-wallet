package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.domain.entity.UserEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.exception.WalletNotFoundException;
import br.com.jefersonmbs.recargapaywallet.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletFinderServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletFinderServiceImpl walletFinderService;

    private WalletEntity testWallet;
    private UserEntity testUser;
    private final UUID testWalletId = UUID.randomUUID();
    private final Long testAccountNumber = 9891L;
    private final String testUserCpf = "12345678901";

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .cpf(testUserCpf)
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
    }

    @Test
    void findWalletById_ShouldReturnWallet_WhenWalletExists() {
        when(walletRepository.findById(testWalletId)).thenReturn(Optional.of(testWallet));

        WalletEntity result = walletFinderService.findWalletById(testWalletId);

        
        assertThat(result).isEqualTo(testWallet);
        assertThat(result.getId()).isEqualTo(testWalletId);
        verify(walletRepository).findById(testWalletId);
    }

    @Test
    void findWalletById_ShouldThrowException_WhenWalletNotFound() {
        when(walletRepository.findById(testWalletId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletFinderService.findWalletById(testWalletId))
            .isInstanceOf(WalletNotFoundException.class)
            .hasMessageContaining("Wallet not found with ID: " + testWalletId);

        verify(walletRepository).findById(testWalletId);
    }

    @Test
    void findTargetWallet_ShouldReturnWallet_WhenTargetWalletIdProvided() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetWalletId(testWalletId)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findById(testWalletId)).thenReturn(Optional.of(testWallet));

        WalletEntity result = walletFinderService.findTargetWallet(request);

        
        assertThat(result).isEqualTo(testWallet);
        assertThat(result.getId()).isEqualTo(testWalletId);
        verify(walletRepository).findById(testWalletId);
    }

    @Test
    void findTargetWallet_ShouldThrowException_WhenTargetWalletIdNotFound() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetWalletId(testWalletId)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findById(testWalletId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletFinderService.findTargetWallet(request))
            .isInstanceOf(WalletNotFoundException.class)
            .hasMessageContaining("Wallet not found with ID: " + testWalletId);

        verify(walletRepository).findById(testWalletId);
    }

    @Test
    void findTargetWallet_ShouldReturnWallet_WhenTargetAccountNumberProvided() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetAccountNumber(testAccountNumber)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testWallet));

        WalletEntity result = walletFinderService.findTargetWallet(request);

        
        assertThat(result).isEqualTo(testWallet);
        assertThat(result.getAccountNumber()).isEqualTo(testAccountNumber);
        verify(walletRepository).findByAccountNumber(testAccountNumber);
    }

    @Test
    void findTargetWallet_ShouldThrowException_WhenTargetAccountNumberNotFound() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetAccountNumber(testAccountNumber)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletFinderService.findTargetWallet(request))
            .isInstanceOf(WalletNotFoundException.class)
            .hasMessageContaining("Target wallet not found for account number: " + testAccountNumber);

        verify(walletRepository).findByAccountNumber(testAccountNumber);
    }

    @Test
    void findTargetWallet_ShouldReturnWallet_WhenTargetUserCpfProvided() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetUserCpf(testUserCpf)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findByUserCpf(testUserCpf)).thenReturn(Optional.of(testWallet));

        WalletEntity result = walletFinderService.findTargetWallet(request);

        
        assertThat(result).isEqualTo(testWallet);
        assertThat(result.getUser().getCpf()).isEqualTo(testUserCpf);
        verify(walletRepository).findByUserCpf(testUserCpf);
    }

    @Test
    void findTargetWallet_ShouldThrowException_WhenTargetUserCpfNotFound() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetUserCpf(testUserCpf)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findByUserCpf(testUserCpf)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletFinderService.findTargetWallet(request))
            .isInstanceOf(WalletNotFoundException.class)
            .hasMessageContaining("Target wallet not found for user CPF: " + testUserCpf);

        verify(walletRepository).findByUserCpf(testUserCpf);
    }

    @Test
    void findTargetWallet_ShouldThrowException_WhenNoTargetIdentificationProvided() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .amount(BigDecimal.valueOf(100.00))
            .build();

        assertThatThrownBy(() -> walletFinderService.findTargetWallet(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Target wallet identification is required (wallet ID, account number, or user CPF)");
    }

    @Test
    void findTargetWallet_ShouldPrioritizeWalletId_WhenMultipleIdentifiersProvided() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetWalletId(testWalletId)
            .targetAccountNumber(testAccountNumber)
            .targetUserCpf(testUserCpf)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findById(testWalletId)).thenReturn(Optional.of(testWallet));

        WalletEntity result = walletFinderService.findTargetWallet(request);

        
        assertThat(result).isEqualTo(testWallet);
        verify(walletRepository).findById(testWalletId);
    }

    @Test
    void findTargetWallet_ShouldUseAccountNumber_WhenWalletIdNullButAccountNumberProvided() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetWalletId(null)
            .targetAccountNumber(testAccountNumber)
            .targetUserCpf(testUserCpf)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testWallet));

        WalletEntity result = walletFinderService.findTargetWallet(request);

        
        assertThat(result).isEqualTo(testWallet);
        verify(walletRepository).findByAccountNumber(testAccountNumber);
    }

    @Test
    void findTargetWallet_ShouldUseCpf_WhenWalletIdAndAccountNumberAreNull() {
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetWalletId(null)
            .targetAccountNumber(null)
            .targetUserCpf(testUserCpf)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findByUserCpf(testUserCpf)).thenReturn(Optional.of(testWallet));

        WalletEntity result = walletFinderService.findTargetWallet(request);

        
        assertThat(result).isEqualTo(testWallet);
        verify(walletRepository).findByUserCpf(testUserCpf);
    }

    @Test
    void findTargetWallet_ShouldHandleSpecialCharactersInCpf() {
        String cpfWithSpecialChars = "123.456.789-01";
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetUserCpf(cpfWithSpecialChars)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        when(walletRepository.findByUserCpf(cpfWithSpecialChars)).thenReturn(Optional.of(testWallet));

        WalletEntity result = walletFinderService.findTargetWallet(request);

        
        assertThat(result).isEqualTo(testWallet);
        verify(walletRepository).findByUserCpf(cpfWithSpecialChars);
    }

    @Test
    void findTargetWallet_ShouldHandleLargeAccountNumber() {
        Long largeAccountNumber = 989999999999L;
        TransactionRequestDto request = TransactionRequestDto.builder()
            .targetAccountNumber(largeAccountNumber)
            .amount(BigDecimal.valueOf(100.00))
            .build();

        WalletEntity walletWithLargeAccount = WalletEntity.builder()
            .id(UUID.randomUUID())
            .accountNumber(largeAccountNumber)
            .balance(BigDecimal.valueOf(500.00))
            .user(testUser)
            .active(true)
            .build();

        when(walletRepository.findByAccountNumber(largeAccountNumber)).thenReturn(Optional.of(walletWithLargeAccount));

        WalletEntity result = walletFinderService.findTargetWallet(request);

        
        assertThat(result.getAccountNumber()).isEqualTo(largeAccountNumber);
        verify(walletRepository).findByAccountNumber(largeAccountNumber);
    }

    @Test
    void findWalletById_ShouldHandleRandomUUID() {
        UUID randomUUID = UUID.randomUUID();
        WalletEntity randomWallet = WalletEntity.builder()
            .id(randomUUID)
            .accountNumber(9892L)
            .balance(BigDecimal.valueOf(750.00))
            .user(testUser)
            .active(true)
            .build();

        when(walletRepository.findById(randomUUID)).thenReturn(Optional.of(randomWallet));

        WalletEntity result = walletFinderService.findWalletById(randomUUID);

        
        assertThat(result.getId()).isEqualTo(randomUUID);
        verify(walletRepository).findById(randomUUID);
    }
}