package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.mapper.TransactionMapper;
import br.com.jefersonmbs.recargapaywallet.api.mapper.WalletMapper;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.UserEntity;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.repository.TransactionHistoryRepository;
import br.com.jefersonmbs.recargapaywallet.domain.repository.UserRepository;
import br.com.jefersonmbs.recargapaywallet.domain.repository.WalletRepository;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    @Override
    public WalletResponseDto createWallet(Long userId) {
        log.info("Creating wallet for user ID: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Wallet already exists for user ID: " + userId);
        }

        Long accountNumber = generateAccountNumber(userId);

        WalletEntity wallet = WalletEntity.builder()
            .user(user)
            .accountNumber(accountNumber)
            .build();

        WalletEntity savedWallet = walletRepository.save(wallet);
        log.info("Wallet created successfully with ID: {} and account number: {} for user ID: {}", 
            savedWallet.getId(), savedWallet.getAccountNumber(), userId);
        
        return walletMapper.toResponseDto(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletById(UUID walletId) {
        WalletEntity wallet = findWalletById(walletId);
        return walletMapper.toResponseDto(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByUserId(Long userId) {
        WalletEntity wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user ID: " + userId));
        return walletMapper.toResponseDto(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByUserCpf(String cpf) {
        WalletEntity wallet = walletRepository.findByUserCpf(cpf)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user CPF: " + cpf));
        return walletMapper.toResponseDto(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByAccountNumber(Long accountNumber) {
        WalletEntity wallet = walletRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found for account number: " + accountNumber));
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
        log.info("Processing deposit of {} to wallet ID: {}", 
            transactionRequest.getAmount(), transactionRequest.getTargetWalletId());
        
        WalletEntity targetWallet = findWalletById(transactionRequest.getTargetWalletId());
        validateWalletForTransaction(targetWallet, "Target wallet is not active for deposits");
        
        BigDecimal balanceBefore = targetWallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(transactionRequest.getAmount());
        
        targetWallet.setBalance(balanceAfter);
        walletRepository.save(targetWallet);
        
        TransactionHistoryEntity transaction = createTransaction(
            TransactionHistoryEntity.TransactionType.DEPOSIT,
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
        log.info("Processing withdrawal of {} from wallet ID: {}", 
            transactionRequest.getAmount(), transactionRequest.getSourceWalletId());
        
        WalletEntity sourceWallet = findWalletById(transactionRequest.getSourceWalletId());
        validateWalletForTransaction(sourceWallet, "Source wallet is not active for withdrawals");
        
        BigDecimal balanceBefore = sourceWallet.getBalance();
        
        if (balanceBefore.compareTo(transactionRequest.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance for withdrawal. Available: " + 
                balanceBefore + ", Requested: " + transactionRequest.getAmount());
        }
        
        BigDecimal balanceAfter = balanceBefore.subtract(transactionRequest.getAmount());
        sourceWallet.setBalance(balanceAfter);
        walletRepository.save(sourceWallet);
        
        TransactionHistoryEntity transaction = createTransaction(
            TransactionHistoryEntity.TransactionType.WITHDRAWAL,
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
        log.info("Processing transfer of {} from wallet ID: {} to target", 
            transactionRequest.getAmount(), transactionRequest.getSourceWalletId());
        
        WalletEntity sourceWallet = findWalletById(transactionRequest.getSourceWalletId());
        WalletEntity targetWallet = findTargetWallet(transactionRequest);
        
        validateWalletForTransaction(sourceWallet, "Source wallet is not active for transfers");
        validateWalletForTransaction(targetWallet, "Target wallet is not active for transfers");
        
        if (sourceWallet.getId().equals(targetWallet.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }
        
        BigDecimal sourceBalanceBefore = sourceWallet.getBalance();
        
        if (sourceBalanceBefore.compareTo(transactionRequest.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance for transfer. Available: " + 
                sourceBalanceBefore + ", Requested: " + transactionRequest.getAmount());
        }
        
        BigDecimal targetBalanceBefore = targetWallet.getBalance();
        BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(transactionRequest.getAmount());
        BigDecimal targetBalanceAfter = targetBalanceBefore.add(transactionRequest.getAmount());
        
        sourceWallet.setBalance(sourceBalanceAfter);
        targetWallet.setBalance(targetBalanceAfter);
        
        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);
        
        TransactionHistoryEntity transferOut = createTransaction(
            TransactionHistoryEntity.TransactionType.TRANSFER_OUT,
            transactionRequest.getAmount(),
            sourceWallet,
            targetWallet,
            transactionRequest.getDescription(),
            sourceBalanceBefore,
            sourceBalanceAfter
        );
        
        createTransaction(
            TransactionHistoryEntity.TransactionType.TRANSFER_IN,
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
        log.info("Fetching transaction history for wallet ID: {} and user ID: {}", walletId, userId);

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        WalletEntity wallet = this.findWalletById(walletId);
        if (!wallet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Wallet ID: " + walletId + " does not belong to user ID: " + userId);
        }

        List<TransactionHistoryEntity> transactions = transactionHistoryRepository.findByWalletId(walletId);
        return transactionMapper.toResponseDtoList(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionHistoryByCpf(String cpf) {
        List<TransactionHistoryEntity> transactions = transactionHistoryRepository.findByUserCpf(cpf);
        return transactionMapper.toResponseDtoList(transactions);
    }

    @Override
    public void toggleActiveWallet(UUID walletId) {
        WalletEntity wallet = findWalletById(walletId);
        wallet.setActive(!wallet.getActive());
        walletRepository.save(wallet);
        log.info("Wallet ID: {} active status toggled to: {}", walletId, wallet.getActive());
    }

    private WalletEntity findWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found with ID: " + walletId));
    }

    private WalletEntity findTargetWallet(TransactionRequestDto transactionRequest) {
        if (transactionRequest.getTargetWalletId() != null) {
            return findWalletById(transactionRequest.getTargetWalletId());
        } else if (transactionRequest.getTargetAccountNumber() != null) {
            return walletRepository.findByAccountNumber(transactionRequest.getTargetAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Target wallet not found for account number: " + 
                    transactionRequest.getTargetAccountNumber()));
        } else if (transactionRequest.getTargetUserCpf() != null) {
            return walletRepository.findByUserCpf(transactionRequest.getTargetUserCpf())
                .orElseThrow(() -> new IllegalArgumentException("Target wallet not found for user CPF: " + 
                    transactionRequest.getTargetUserCpf()));
        } else {
            throw new IllegalArgumentException("Target wallet identification is required (wallet ID, account number, or user CPF)");
        }
    }

    private void validateWalletForTransaction(WalletEntity wallet, String errorMessage) {
        if (!wallet.getActive() || !wallet.getUser().getActive()) {
            throw new IllegalStateException(errorMessage);
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
        return Long.parseLong("989" + userId);
    }
}