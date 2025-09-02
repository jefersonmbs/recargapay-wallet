package br.com.jefersonmbs.recargapaywallet.api.controller;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserUpdateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
class UserEntityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    void createUser_ShouldReturnCreated_WhenValidInput() throws Exception {
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .cpf("11111111111")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Carlos Silva"))
                .andExpect(jsonPath("$.email").value("carlos.silva@example.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.cpf").value("11111111111"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenInvalidInput() throws Exception {
        UserCreateDto invalidDto = UserCreateDto.builder()
                .name("") 
                .email("invalid-email") 
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        // First create a user
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Carlos Silva")
                .email("carlos.test@example.com")
                .phone("11987654321")
                .cpf("12345678901")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createDto);

        mockMvc.perform(get("/api/v1/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.name").value("Carlos Silva"));
    }

    @Test
    void getUserById_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999999L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        // First create a user
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Test User")
                .email("test.user@example.com")
                .phone("11987654321")
                .cpf("98765432100")
                .build();
        
        userService.createUser(createDto);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenValidInput() throws Exception {
        // First create a user
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Original Name")
                .email("original@example.com")
                .phone("11987654321")
                .cpf("11122233344")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createDto);
        
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .phone("21987654321")
                .build();

        mockMvc.perform(put("/api/v1/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenUserExists() throws Exception {
        // First create a user
        UserCreateDto createDto = UserCreateDto.builder()
                .name("To Delete")
                .email("delete@example.com")
                .phone("11987654321")
                .cpf("55566677788")
                .build();
        
        UserResponseDto createdUser = userService.createUser(createDto);

        mockMvc.perform(delete("/api/v1/users/{id}", createdUser.getId()))
                .andExpect(status().isNoContent());
    }
}