package br.com.jefersonmbs.recargapaywallet.api.controller;

import br.com.jefersonmbs.recargapaywallet.api.dto.PagedTransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionHistoryRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController implements WalletControllerApi {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponseDto> createWallet(@RequestParam Long userId) {
        log.info("REST request to create wallet for user ID: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(userId));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<WalletResponseDto> getWalletByAccountNumber(@PathVariable Long accountNumber) {
        log.info("REST request to get wallet by account number: {}", accountNumber);
        
        return ResponseEntity.ok(walletService.getWalletByAccountNumber(accountNumber));
    }

    @GetMapping
    public ResponseEntity<List<WalletResponseDto>> getAllActiveWallets() {
        log.info("REST request to get all active wallets");
        
        return ResponseEntity.ok(walletService.getAllActiveWallets());
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDto> deposit(@Valid @RequestBody TransactionRequestDto transactionRequest) {
        String correlationId = UUID.randomUUID().toString();
        transactionRequest.setCorrelationId(correlationId);
        
        log.info("REST request to process deposit of {} to wallet ID: {} [correlationId={}]", 
            transactionRequest.getAmount(), transactionRequest.getTargetWalletId(), correlationId);
        
        return ResponseEntity.ok(walletService.deposit(transactionRequest));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDto> withdraw(@Valid @RequestBody TransactionRequestDto transactionRequest) {
        String correlationId = UUID.randomUUID().toString();
        transactionRequest.setCorrelationId(correlationId);
        
        log.info("REST request to process withdrawal of {} from wallet ID: {} [correlationId={}]", 
            transactionRequest.getAmount(), transactionRequest.getSourceWalletId(), correlationId);
        
        return ResponseEntity.ok(walletService.withdraw(transactionRequest));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transfer(@Valid @RequestBody TransactionRequestDto transactionRequest) {
        String correlationId = UUID.randomUUID().toString();
        transactionRequest.setCorrelationId(correlationId);
        
        log.info("REST request to process transfer of {} from wallet ID: {} to target [correlationId={}]", 
            transactionRequest.getAmount(), transactionRequest.getSourceWalletId(), correlationId);
        
        return ResponseEntity.ok(walletService.transfer(transactionRequest));
    }

    @GetMapping("/{walletId}/{userId}/transactions")
    public ResponseEntity<PagedTransactionResponseDto> getTransactionHistory(
            @PathVariable UUID walletId, 
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        TransactionHistoryRequestDto request = TransactionHistoryRequestDto.builder()
                .page(page)
                .size(size)
                .startDate(startDate)
                .endDate(endDate)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
                
        log.info("REST request to get paginated transaction history for wallet ID and User ID: {} , {} with filters: {}", walletId, userId, request);
        
        return ResponseEntity.ok(walletService.getTransactionHistoryPaginated(walletId, userId, request));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActiveWallet(@PathVariable UUID id) {
        log.info("REST request to toggle active status for wallet ID: {}", id);
        
        walletService.toggleActiveWallet(id);
        return ResponseEntity.noContent().build();
    } 
}