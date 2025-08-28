package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.mapper.TransactionMapper;
import br.com.jefersonmbs.recargapaywallet.api.mapper.WalletMapper;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity.TransactionType;
import br.com.jefersonmbs.recargapaywallet.domain.entity.UserEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.exception.InactiveWalletException;
import br.com.jefersonmbs.recargapaywallet.domain.exception.InsufficientBalanceException;
import br.com.jefersonmbs.recargapaywallet.domain.exception.WalletAlreadyExistsException;
import br.com.jefersonmbs.recargapaywallet.domain.exception.WalletNotFoundException;
import br.com.jefersonmbs.recargapaywallet.domain.repository.TransactionHistoryRepository;
import br.com.jefersonmbs.recargapaywallet.domain.repository.UserRepository;
import br.com.jefersonmbs.recargapaywallet.domain.repository.WalletRepository;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private static final String ACCOUNT_NUMBER_PREFIX = "989";
    private static final String SOURCE_WALLET_INACTIVE_MESSAGE = "Source wallet is not active for %s";
    private static final String TARGET_WALLET_INACTIVE_MESSAGE = "Target wallet is not active for %s";

    private final WalletRepository walletRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    @Override
    public WalletResponseDto createWallet(Long userId) {
        validateUserId(userId);
        log.info("Creating wallet for user ID: {}", userId);
        
        UserEntity user = findUserByIdOrThrow(userId);
        validateWalletDoesNotExist(userId);

        Long accountNumber = generateAccountNumber(userId);
        WalletEntity wallet = buildWallet(user, accountNumber);
        WalletEntity savedWallet = walletRepository.save(wallet);
        
        log.info("Wallet created successfully with ID: {} and account number: {} for user ID: {}", 
            savedWallet.getId(), savedWallet.getAccountNumber(), userId);
        
        return walletMapper.toResponseDto(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletById(UUID walletId) {
        validateWalletId(walletId);
        WalletEntity wallet = findWalletById(walletId);
        return walletMapper.toResponseDto(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByUserId(Long userId) {
        validateUserId(userId);
        WalletEntity wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user ID: " + userId));
        return walletMapper.toResponseDto(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByUserCpf(String cpf) {
        validateCpf(cpf);
        WalletEntity wallet = walletRepository.findByUserCpf(cpf)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user CPF: " + cpf));
        return walletMapper.toResponseDto(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByAccountNumber(Long accountNumber) {
        validateAccountNumber(accountNumber);
        WalletEntity wallet = walletRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for account number: " + accountNumber));
        return walletMapper.toResponseDto(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponseDto> getAllActiveWallets() {
        List<WalletEntity> activeWallets = walletRepository.findActiveWalletsWithActiveUsers();
        return walletMapper.toResponseDtoList(activeWallets);
    }

    @Override
    public TransactionResponseDto deposit(TransactionRequestDto transactionRequest) {
        validateTransactionRequest(transactionRequest);
        validateWalletId(transactionRequest.getTargetWalletId());
        
        log.info("Processing deposit of {} to wallet ID: {}", 
            transactionRequest.getAmount(), transactionRequest.getTargetWalletId());
        
        WalletEntity targetWallet = findWalletById(transactionRequest.getTargetWalletId());
        validateWalletForTransaction(targetWallet, String.format(TARGET_WALLET_INACTIVE_MESSAGE, "deposits"));
        
        BigDecimal balanceBefore = targetWallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(transactionRequest.getAmount());
        
        updateWalletBalance(targetWallet, balanceAfter);
        
        TransactionHistoryEntity transaction = createTransaction(
            TransactionType.DEPOSIT,
            transactionRequest.getAmount(),
            null,
            targetWallet,
            transactionRequest.getDescription(),
            balanceBefore,
            balanceAfter
        );
        
        log.info("Deposit completed successfully. Transaction ID: {}", transaction.getId());
        return transactionMapper.toResponseDto(transaction);
    }

    @Override
    public TransactionResponseDto withdraw(TransactionRequestDto transactionRequest) {
        validateTransactionRequest(transactionRequest);
        validateWalletId(transactionRequest.getSourceWalletId());
        
        log.info("Processing withdrawal of {} from wallet ID: {}", 
            transactionRequest.getAmount(), transactionRequest.getSourceWalletId());
        
        WalletEntity sourceWallet = findWalletById(transactionRequest.getSourceWalletId());
        validateWalletForTransaction(sourceWallet, String.format(SOURCE_WALLET_INACTIVE_MESSAGE, "withdrawals"));
        
        BigDecimal balanceBefore = sourceWallet.getBalance();
        validateSufficientBalance(balanceBefore, transactionRequest.getAmount());
        
        BigDecimal balanceAfter = balanceBefore.subtract(transactionRequest.getAmount());
        updateWalletBalance(sourceWallet, balanceAfter);
        
        TransactionHistoryEntity transaction = createTransaction(
            TransactionType.WITHDRAWAL,
            transactionRequest.getAmount(),
            sourceWallet,
            null,
            transactionRequest.getDescription(),
            balanceBefore,
            balanceAfter
        );
        
        log.info("Withdrawal completed successfully. Transaction ID: {}", transaction.getId());
        return transactionMapper.toResponseDto(transaction);
    }

    @Override
    public TransactionResponseDto transfer(TransactionRequestDto transactionRequest) {
        validateTransactionRequest(transactionRequest);
        validateWalletId(transactionRequest.getSourceWalletId());
        
        log.info("Processing transfer of {} from wallet ID: {} to target", 
            transactionRequest.getAmount(), transactionRequest.getSourceWalletId());
        
        WalletEntity sourceWallet = findWalletById(transactionRequest.getSourceWalletId());
        WalletEntity targetWallet = findTargetWallet(transactionRequest);
        
        validateWalletForTransaction(sourceWallet, String.format(SOURCE_WALLET_INACTIVE_MESSAGE, "transfers"));
        validateWalletForTransaction(targetWallet, String.format(TARGET_WALLET_INACTIVE_MESSAGE, "transfers"));
        validateDifferentWallets(sourceWallet, targetWallet);
        
        BigDecimal sourceBalanceBefore = sourceWallet.getBalance();
        validateSufficientBalance(sourceBalanceBefore, transactionRequest.getAmount());
        
        BigDecimal targetBalanceBefore = targetWallet.getBalance();
        BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(transactionRequest.getAmount());
        BigDecimal targetBalanceAfter = targetBalanceBefore.add(transactionRequest.getAmount());
        
        updateWalletBalance(sourceWallet, sourceBalanceAfter);
        updateWalletBalance(targetWallet, targetBalanceAfter);
        
        TransactionHistoryEntity transferOut = createTransaction(
            TransactionType.TRANSFER_OUT,
            transactionRequest.getAmount(),
            sourceWallet,
            targetWallet,
            transactionRequest.getDescription(),
            sourceBalanceBefore,
            sourceBalanceAfter
        );
        
        createTransaction(
            TransactionType.TRANSFER_IN,
            transactionRequest.getAmount(),
            sourceWallet,
            targetWallet,
            transactionRequest.getDescription(),
            targetBalanceBefore,
            targetBalanceAfter
        );
        
        log.info("Transfer completed successfully. Transaction ID: {}", transferOut.getId());
        return transactionMapper.toResponseDto(transferOut);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionHistory(UUID walletId, Long userId) {
        validateWalletId(walletId);
        validateUserId(userId);
        
        log.info("Fetching transaction history for wallet ID: {} and user ID: {}", walletId, userId);

        validateUserExists(userId);
        WalletEntity wallet = findWalletById(walletId);
        validateWalletOwnership(wallet, userId);

        List<TransactionHistoryEntity> transactions = transactionHistoryRepository.findByWalletId(walletId);
        return transactionMapper.toResponseDtoList(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionHistoryByCpf(String cpf) {
        validateCpf(cpf);
        List<TransactionHistoryEntity> transactions = transactionHistoryRepository.findByUserCpf(cpf);
        return transactionMapper.toResponseDtoList(transactions);
    }

    @Override
    public void toggleActiveWallet(UUID walletId) {
        validateWalletId(walletId);
        WalletEntity wallet = findWalletById(walletId);
        boolean newActiveStatus = !wallet.getActive();
        wallet.setActive(newActiveStatus);
        walletRepository.save(wallet);
        log.info("Wallet ID: {} active status toggled to: {}", walletId, newActiveStatus);
    }

    private WalletEntity findWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + walletId));
    }

    private WalletEntity findTargetWallet(TransactionRequestDto transactionRequest) {
        if (transactionRequest.getTargetWalletId() != null) {
            return findWalletById(transactionRequest.getTargetWalletId());
        } else if (transactionRequest.getTargetAccountNumber() != null) {
            return walletRepository.findByAccountNumber(transactionRequest.getTargetAccountNumber())
                .orElseThrow(() -> new WalletNotFoundException("Target wallet not found for account number: " + 
                    transactionRequest.getTargetAccountNumber()));
        } else if (transactionRequest.getTargetUserCpf() != null) {
            return walletRepository.findByUserCpf(transactionRequest.getTargetUserCpf())
                .orElseThrow(() -> new WalletNotFoundException("Target wallet not found for user CPF: " + 
                    transactionRequest.getTargetUserCpf()));
        } else {
            throw new IllegalArgumentException("Target wallet identification is required (wallet ID, account number, or user CPF)");
        }
    }

    private void validateWalletForTransaction(WalletEntity wallet, String errorMessage) {
        if (!wallet.getActive() || !wallet.getUser().getActive()) {
            throw new InactiveWalletException(errorMessage);
        }
    }

    private TransactionHistoryEntity createTransaction(
            TransactionHistoryEntity.TransactionType type,
            BigDecimal amount,
            WalletEntity sourceWallet,
            WalletEntity targetWallet,
            String description,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        
        TransactionHistoryEntity transaction = TransactionHistoryEntity.builder()
            .type(type)
            .amount(amount)
            .sourceWallet(sourceWallet)
            .targetWallet(targetWallet)
            .description(description)
            .balanceBeforeTransaction(balanceBefore)
            .balanceAfterTransaction(balanceAfter)
            .build();
        
        return transactionHistoryRepository.save(transaction);
    }

    private Long generateAccountNumber(Long userId) {
        try {
            return Long.parseLong(ACCOUNT_NUMBER_PREFIX + userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID for account number generation: " + userId, e);
        }
    }
    
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
    }
    
    private void validateWalletId(UUID walletId) {
        if (walletId == null) {
            throw new IllegalArgumentException("Wallet ID cannot be null");
        }
    }
    
    private void validateCpf(String cpf) {
        if (!StringUtils.hasText(cpf)) {
            throw new IllegalArgumentException("CPF cannot be null or empty");
        }
    }
    
    private void validateAccountNumber(Long accountNumber) {
        if (accountNumber == null || accountNumber <= 0) {
            throw new IllegalArgumentException("Account number must be a positive number");
        }
    }
    
    private void validateTransactionRequest(TransactionRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Transaction request cannot be null");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }
    
    private UserEntity findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new WalletNotFoundException("User not found with ID: " + userId));
    }
    
    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new WalletNotFoundException("User not found with ID: " + userId);
        }
    }
    
    private void validateWalletDoesNotExist(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new WalletAlreadyExistsException(userId);
        }
    }
    
    private WalletEntity buildWallet(UserEntity user, Long accountNumber) {
        return WalletEntity.builder()
            .user(user)
            .accountNumber(accountNumber)
            .build();
    }
    
    private void validateSufficientBalance(BigDecimal currentBalance, BigDecimal requestedAmount) {
        if (currentBalance.compareTo(requestedAmount) < 0) {
            throw new InsufficientBalanceException(currentBalance, requestedAmount);
        }
    }
    
    private void validateWalletOwnership(WalletEntity wallet, Long userId) {
        if (!Objects.equals(wallet.getUser().getId(), userId)) {
            throw new IllegalArgumentException(
                String.format("Wallet ID: %s does not belong to user ID: %d", wallet.getId(), userId));
        }
    }
    
    private void validateDifferentWallets(WalletEntity sourceWallet, WalletEntity targetWallet) {
        if (Objects.equals(sourceWallet.getId(), targetWallet.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }
    }
    
    private void updateWalletBalance(WalletEntity wallet, BigDecimal newBalance) {
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
    }
}