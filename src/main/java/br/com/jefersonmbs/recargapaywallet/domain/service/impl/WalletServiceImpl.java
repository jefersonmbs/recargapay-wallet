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
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletService;
import br.com.jefersonmbs.recargapaywallet.domain.validator.WalletValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private static final String ACCOUNT_NUMBER_PREFIX = "989";

    private final WalletRepository walletRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;
    private final WalletValidator walletValidator;
    private final TransactionStrategyFactory transactionStrategyFactory;
    private final WalletFinderService walletFinderService;

    @Override
    public WalletResponseDto createWallet(Long userId) {
        walletValidator.validateUserId(userId);
        log.info("Creating wallet for user ID: {}", userId);
        
        UserEntity user = findUserByIdOrThrow(userId);
        walletValidator.validateWalletDoesNotExist(userId);

        Long accountNumber = generateAccountNumber(userId);
        WalletEntity wallet = buildWallet(user, accountNumber);
        WalletEntity savedWallet = walletRepository.save(wallet);
        
        log.info("Wallet created successfully with ID: {} and account number: {} for user ID: {}", 
            savedWallet.getId(), savedWallet.getAccountNumber(), userId);
        
        return walletMapper.toResponseDto(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByAccountNumber(Long accountNumber) {
        walletValidator.validateAccountNumber(accountNumber);
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
        return transactionStrategyFactory.getDepositStrategy().execute(transactionRequest);
    }

    @Override
    public TransactionResponseDto withdraw(TransactionRequestDto transactionRequest) {
        return transactionStrategyFactory.getWithdrawStrategy().execute(transactionRequest);
    }

    @Override
    public TransactionResponseDto transfer(TransactionRequestDto transactionRequest) {
        return transactionStrategyFactory.getTransferStrategy().execute(transactionRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedTransactionResponseDto getTransactionHistoryPaginated(UUID walletId, Long userId, TransactionHistoryRequestDto request) {
        walletValidator.validateWalletId(walletId);
        walletValidator.validateUserId(userId);
        
        log.info("Fetching paginated transaction history for wallet ID: {} and user ID: {} with filters: {}", walletId, userId, request);

        walletValidator.validateUserExists(userId);
        WalletEntity wallet = walletFinderService.findWalletById(walletId);
        walletValidator.validateWalletOwnership(wallet, userId);

        walletValidator.validateDateRange(request);
        
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        Page<TransactionHistoryEntity> transactionPage;
        
        if (request.getStartDate() != null && request.getEndDate() != null) {
            LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
            LocalDateTime endDateTime = request.getEndDate().atTime(LocalTime.MAX);
            transactionPage = transactionHistoryRepository.findByWalletIdAndDateRangePageable(walletId, startDateTime, endDateTime, pageable);
        } else {
            transactionPage = transactionHistoryRepository.findByWalletIdPageable(walletId, pageable);
        }

        List<TransactionResponseDto> transactions = transactionMapper.toResponseDtoList(transactionPage.getContent());
        
        return PagedTransactionResponseDto.builder()
            .content(transactions)
            .page(transactionPage.getNumber())
            .size(transactionPage.getSize())
            .totalPages(transactionPage.getTotalPages())
            .totalElements(transactionPage.getTotalElements())
            .first(transactionPage.isFirst())
            .last(transactionPage.isLast())
            .hasNext(transactionPage.hasNext())
            .hasPrevious(transactionPage.hasPrevious())
            .build();
    }


    @Override
    public void toggleActiveWallet(UUID walletId) {
        walletValidator.validateWalletId(walletId);
        WalletEntity wallet = walletFinderService.findWalletById(walletId);
        boolean newActiveStatus = !wallet.getActive();
        wallet.setActive(newActiveStatus);
        walletRepository.save(wallet);
        log.info("Wallet ID: {} active status toggled to: {}", walletId, newActiveStatus);
    }

    private Long generateAccountNumber(Long userId) {
        try {
            return Long.parseLong(ACCOUNT_NUMBER_PREFIX + userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID for account number generation: " + userId, e);
        }
    }
    
    private UserEntity findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new WalletNotFoundException("User not found with ID: " + userId));
    }
    
    private WalletEntity buildWallet(UserEntity user, Long accountNumber) {
        return WalletEntity.builder()
            .user(user)
            .accountNumber(accountNumber)
            .build();
    }
}