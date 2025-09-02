package br.com.jefersonmbs.recargapaywallet.domain.validator;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionHistoryRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.exception.InactiveWalletException;
import br.com.jefersonmbs.recargapaywallet.domain.exception.InsufficientBalanceException;
import br.com.jefersonmbs.recargapaywallet.domain.exception.WalletAlreadyExistsException;
import br.com.jefersonmbs.recargapaywallet.domain.exception.WalletNotFoundException;
import br.com.jefersonmbs.recargapaywallet.domain.exception.WalletValidationException;
import br.com.jefersonmbs.recargapaywallet.domain.repository.UserRepository;
import br.com.jefersonmbs.recargapaywallet.domain.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletValidator {
    
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    
    public void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new WalletValidationException("User ID must be a positive number");
        }
    }
    
    public void validateWalletId(UUID walletId) {
        if (walletId == null) {
            throw new WalletValidationException("Wallet ID cannot be null");
        }
    }
    
    public void validateCpf(String cpf) {
        if (!StringUtils.hasText(cpf)) {
            throw new WalletValidationException("CPF cannot be null or empty");
        }
    }
    
    public void validateAccountNumber(Long accountNumber) {
        if (accountNumber == null || accountNumber <= 0) {
            throw new WalletValidationException("Account number must be a positive number");
        }
    }
    
    public void validateTransactionRequest(TransactionRequestDto request) {
        if (request == null) {
            throw new WalletValidationException("Transaction request cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletValidationException("Transaction amount must be positive");
        }
    }
    
    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new WalletNotFoundException("User not found with ID: " + userId);
        }
    }
    
    public void validateWalletDoesNotExist(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new WalletAlreadyExistsException(userId);
        }
    }
    
    public void validateWalletForTransaction(WalletEntity wallet, String errorMessage) {
        if (!wallet.getActive() || !wallet.getUser().getActive()) {
            throw new InactiveWalletException(errorMessage);
        }
    }
    
    public void validateSufficientBalance(BigDecimal currentBalance, BigDecimal requestedAmount) {
        if (currentBalance.compareTo(requestedAmount) < 0) {
            throw new InsufficientBalanceException(currentBalance, requestedAmount);
        }
    }
    
    public void validateWalletOwnership(WalletEntity wallet, Long userId) {
        if (!Objects.equals(wallet.getUser().getId(), userId)) {
            throw new WalletValidationException(
                String.format("Wallet ID: %s does not belong to user ID: %d", wallet.getId(), userId));
        }
    }
    
    public void validateDifferentWallets(WalletEntity sourceWallet, WalletEntity targetWallet) {
        if (Objects.equals(sourceWallet.getId(), targetWallet.getId())) {
            throw new WalletValidationException("Cannot transfer to the same wallet");
        }
    }

    public void validateDateRange(TransactionHistoryRequestDto request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
            if (daysBetween > 90) {
                throw new IllegalArgumentException("Date range cannot exceed 90 days for security reasons");
            }
        } else if (request.getStartDate() != null || request.getEndDate() != null) {
            throw new IllegalArgumentException("Both start date and end date must be provided");
        }
    }
}