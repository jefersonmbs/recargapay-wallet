package br.com.jefersonmbs.recargapaywallet.api.controller;

import br.com.jefersonmbs.recargapaywallet.api.dto.PagedTransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionHistoryRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Wallets", description = "API for digital wallet and transaction management")
public interface WalletControllerApi {

    @Operation(summary = "Create wallet", description = "Creates a new wallet for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wallet created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "User not found or wallet already exists",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<WalletResponseDto> createWallet(
            @Parameter(description = "User ID", required = true) @RequestParam Long userId);

    @Operation(summary = "Get wallet by ID", description = "Retrieves a specific wallet by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Wallet not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })

    ResponseEntity<WalletResponseDto> getWalletByAccountNumber(
            @Parameter(description = "Account number", required = true) @PathVariable Long accountNumber);

    @Operation(summary = "List active wallets", description = "Lists all active wallets in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet list returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<List<WalletResponseDto>> getAllActiveWallets();

    @Operation(summary = "Make deposit", description = "Deposits an amount into a wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or inactive wallet",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<TransactionResponseDto> deposit(@Valid @RequestBody TransactionRequestDto transactionRequest);

    @Operation(summary = "Make withdrawal", description = "Withdraws an amount from a wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data, insufficient balance or inactive wallet",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<TransactionResponseDto> withdraw(@Valid @RequestBody TransactionRequestDto transactionRequest);

    @Operation(summary = "Make transfer", description = "Transfers an amount between wallets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data, insufficient balance or inactive wallets",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<TransactionResponseDto> transfer(@Valid @RequestBody TransactionRequestDto transactionRequest);

    @Operation(summary = "Transaction history", description = "Retrieves the paginated transaction history of a wallet with optional date filtering (max 90 days range)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated history returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedTransactionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range or parameters",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<PagedTransactionResponseDto> getTransactionHistory(
            @Parameter(description = "Wallet ID", required = true) @PathVariable UUID walletId,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Page number", required = false) @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size", required = false) @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "Start date filter", required = false) @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date filter", required = false) @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Sort field", required = false) @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", required = false) @RequestParam(defaultValue = "DESC") String sortDirection);

    @Operation(summary = "History by CPF", description = "Retrieves a user's transaction history by CPF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })

    ResponseEntity<Void> toggleActiveWallet(
            @Parameter(description = "Wallet ID", required = true) @PathVariable UUID id);
}