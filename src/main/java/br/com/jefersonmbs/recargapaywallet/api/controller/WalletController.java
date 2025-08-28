package br.com.jefersonmbs.recargapaywallet.api.controller;

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

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponseDto> getWalletById(@PathVariable UUID id) {
        log.info("REST request to get wallet by ID: {}", id);

        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponseDto> getWalletByUserId(@PathVariable Long userId) {
        log.info("REST request to get wallet by user ID: {}", userId);

        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<WalletResponseDto> getWalletByUserCpf(@PathVariable String cpf) {
        log.info("REST request to get wallet by user CPF: {}", cpf);

        return ResponseEntity.ok(walletService.getWalletByUserCpf(cpf));
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
        log.info("REST request to process deposit of {} to wallet ID: {}", 
            transactionRequest.getAmount(), transactionRequest.getTargetWalletId());
        
        return ResponseEntity.ok(walletService.deposit(transactionRequest));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDto> withdraw(@Valid @RequestBody TransactionRequestDto transactionRequest) {
        log.info("REST request to process withdrawal of {} from wallet ID: {}", 
            transactionRequest.getAmount(), transactionRequest.getSourceWalletId());
        
        return ResponseEntity.ok(walletService.withdraw(transactionRequest));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transfer(@Valid @RequestBody TransactionRequestDto transactionRequest) {
        log.info("REST request to process transfer of {} from wallet ID: {} to target", 
            transactionRequest.getAmount(), transactionRequest.getSourceWalletId());
        
        return ResponseEntity.ok(walletService.transfer(transactionRequest));
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionHistory(@PathVariable UUID walletId, @PathVariable Long userId) {
        log.info("REST request to get transaction history for wallet ID and User ID: {} , {}", walletId,userId);
        
        return ResponseEntity.ok(walletService.getTransactionHistory(walletId,userId));
    }

    @GetMapping("/transactions/cpf/{cpf}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionHistoryByCpf(@PathVariable String cpf) {
        log.info("REST request to get transaction history for user CPF: {}", cpf);
        
        return ResponseEntity.ok(walletService.getTransactionHistoryByCpf(cpf));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActiveWallet(@PathVariable UUID id) {
        log.info("REST request to toggle active status for wallet ID: {}", id);
        
        walletService.toggleActiveWallet(id);
        return ResponseEntity.noContent().build();
    } 
}