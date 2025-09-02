package br.com.jefersonmbs.recargapaywallet.api.controller;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.WalletResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
import br.com.jefersonmbs.recargapaywallet.domain.entity.TransactionHistoryEntity;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletService;
import br.com.jefersonmbs.recargapaywallet.domain.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Usa H2 em memória
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Test
    void createWallet_ShouldReturnCreatedStatus_WhenValidUserId() throws Exception {
        UserCreateDto createUserDto = UserCreateDto.builder()
                .name("Test User")
                .email("test@example.com")
                .phone("11987654321")
                .cpf("12345678901")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createUserDto);

        mockMvc.perform(post("/api/v1/wallets")
                .param("userId", createdUser.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(createdUser.getId()))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.balance").value(0.00));
    }

    @Test
    void createWallet_ShouldReturnInternalServerError_WhenUserIdMissing() throws Exception {
        mockMvc.perform(post("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getWalletByAccountNumber_ShouldReturnOkStatus_WhenValidAccountNumber() throws Exception {
        UserCreateDto createUserDto = UserCreateDto.builder()
                .name("Test User")
                .email("wallet.test@example.com")
                .phone("11987654321")
                .cpf("98765432100")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createUserDto);
        WalletResponseDto createdWallet = walletService.createWallet(createdUser.getId());

        mockMvc.perform(get("/api/v1/wallets/account/{accountNumber}", createdWallet.getAccountNumber())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value(createdWallet.getAccountNumber()))
                .andExpect(jsonPath("$.userId").value(createdUser.getId()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getAllActiveWallets_ShouldReturnOkStatus_WhenWalletsExist() throws Exception {
        UserCreateDto createUserDto = UserCreateDto.builder()
                .name("Active Wallet User")
                .email("active@example.com")
                .phone("11987654321")
                .cpf("11122233344")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createUserDto);
        walletService.createWallet(createdUser.getId());

        mockMvc.perform(get("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deposit_ShouldReturnOkStatus_WhenValidTransactionRequest() throws Exception {
        UserCreateDto createUserDto = UserCreateDto.builder()
                .name("Deposit User")
                .email("deposit@example.com")
                .phone("11987654321")
                .cpf("55566677788")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createUserDto);
        WalletResponseDto createdWallet = walletService.createWallet(createdUser.getId());

        TransactionRequestDto depositRequest = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionHistoryEntity.TransactionType.DEPOSIT)
                .targetWalletId(createdWallet.getId())
                .description("Test deposit")
                .build();

        mockMvc.perform(post("/api/v1/wallets/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.targetWalletId").value(createdWallet.getId().toString()))
                .andExpect(jsonPath("$.description").value("Test deposit"));
    }

    @Test
    void deposit_ShouldReturnBadRequest_WhenInvalidAmount() throws Exception {
        TransactionRequestDto invalidRequest = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(-100.00)) // Negative amount
                .targetWalletId(UUID.randomUUID())
                .description("Invalid deposit")
                .build();

        mockMvc.perform(post("/api/v1/wallets/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_ShouldReturnOkStatus_WhenValidTransactionRequest() throws Exception {
        UserCreateDto createUserDto = UserCreateDto.builder()
                .name("Withdraw User")
                .email("withdraw@example.com")
                .phone("11987654321")
                .cpf("99988877766")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createUserDto);
        WalletResponseDto createdWallet = walletService.createWallet(createdUser.getId());

        TransactionRequestDto depositRequest = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(200.00))
                .type(TransactionHistoryEntity.TransactionType.DEPOSIT)
                .targetWalletId(createdWallet.getId())
                .description("Initial deposit")
                .build();
        walletService.deposit(depositRequest);

        TransactionRequestDto withdrawRequest = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(50.00))
                .type(TransactionHistoryEntity.TransactionType.WITHDRAWAL)
                .sourceWalletId(createdWallet.getId())
                .description("Test withdrawal")
                .build();

        mockMvc.perform(post("/api/v1/wallets/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.sourceWalletId").value(createdWallet.getId().toString()))
                .andExpect(jsonPath("$.description").value("Test withdrawal"));
    }

    @Test
    void transfer_ShouldReturnOkStatus_WhenValidTransactionRequest() throws Exception {
        UserCreateDto sourceUserDto = UserCreateDto.builder()
                .name("Source User")
                .email("source@example.com")
                .phone("11987654321")
                .cpf("12312312312")
                .build();
        
        UserResponseDto sourceUser = userService.createUser(sourceUserDto);
        WalletResponseDto sourceWallet = walletService.createWallet(sourceUser.getId());

        UserCreateDto targetUserDto = UserCreateDto.builder()
                .name("Target User")
                .email("target@example.com")
                .phone("11987654321")
                .cpf("32132132132")
                .build();
        
        UserResponseDto targetUser = userService.createUser(targetUserDto);
        WalletResponseDto targetWallet = walletService.createWallet(targetUser.getId());

        TransactionRequestDto depositRequest = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(300.00))
                .type(TransactionHistoryEntity.TransactionType.DEPOSIT)
                .targetWalletId(sourceWallet.getId())
                .description("Initial deposit")
                .build();
        walletService.deposit(depositRequest);

        TransactionRequestDto transferRequest = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(100.00))
                .type(TransactionHistoryEntity.TransactionType.TRANSFER)
                .sourceWalletId(sourceWallet.getId())
                .targetWalletId(targetWallet.getId())
                .description("Test transfer")
                .build();

        mockMvc.perform(post("/api/v1/wallets/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.sourceWalletId").value(sourceWallet.getId().toString()))
                .andExpect(jsonPath("$.targetWalletId").value(targetWallet.getId().toString()))
                .andExpect(jsonPath("$.description").value("Test transfer"));
    }

    @Test
    void getTransactionHistory_ShouldReturnOkStatus_WithDefaultParameters() throws Exception {
        UserCreateDto createUserDto = UserCreateDto.builder()
                .name("History User")
                .email("history@example.com")
                .phone("11987654321")
                .cpf("44455566677")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createUserDto);
        WalletResponseDto createdWallet = walletService.createWallet(createdUser.getId());

        TransactionRequestDto depositRequest = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(150.00))
                .targetWalletId(createdWallet.getId())
                .description("History deposit")
                .build();
        walletService.deposit(depositRequest);

        mockMvc.perform(get("/api/v1/wallets/{walletId}/{userId}/transactions", 
                        createdWallet.getId(), createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void toggleActiveWallet_ShouldReturnNoContentStatus_WhenValidWalletId() throws Exception {
        UserCreateDto createUserDto = UserCreateDto.builder()
                .name("Toggle User")
                .email("toggle@example.com")
                .phone("11987654321")
                .cpf("77788899900")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createUserDto);
        WalletResponseDto createdWallet = walletService.createWallet(createdUser.getId());

        mockMvc.perform(patch("/api/v1/wallets/{id}/toggle-active", createdWallet.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}