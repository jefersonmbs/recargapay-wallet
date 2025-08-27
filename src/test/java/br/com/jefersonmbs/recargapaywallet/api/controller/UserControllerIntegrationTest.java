package br.com.jefersonmbs.recargapaywallet.api.controller;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
import br.com.jefersonmbs.recargapaywallet.domain.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private final UUID testUserId = UUID.randomUUID();

    @Test
    void createUser_ShouldReturnCreated_WhenValidInput() throws Exception {
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Carlos Silva"))
                .andExpect(jsonPath("$.email").value("carlos.silva@example.com"))
                .andExpect(jsonPath("$.active").value(true));
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
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(testUserId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/users/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.name").value("Carlos Silva"));
    }

    @Test
    void getUserById_ShouldReturnBadRequest_WhenUserNotFound() throws Exception {
        when(userService.getUserById(testUserId))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/v1/users/{id}", testUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        UserResponseDto user1 = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.getAllUsers()).thenReturn(Collections.singletonList(user1));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Carlos Silva"));
    }

    @Test
    void getAllUsers_ShouldReturnActiveUsersOnly_WhenActiveOnlyTrue() throws Exception {
        UserResponseDto activeUser = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.getAllActiveUsers()).thenReturn(Collections.singletonList(activeUser));

        mockMvc.perform(get("/api/v1/users?activeOnly=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void searchUsersByName_ShouldReturnMatchingUsers() throws Exception {
        UserResponseDto user = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.searchUsersByName("Carlos")).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/v1/users?name=Carlos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Carlos Silva"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenValidInput() throws Exception {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Ana Santos")
                .email("ana.santos@example.com")
                .phone("21987654321")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(testUserId)
                .name("Ana Santos")
                .email("ana.santos@example.com")
                .phone("21987654321")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.updateUser(any(UUID.class), any(UserUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/users/{id}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Santos"))
                .andExpect(jsonPath("$.email").value("ana.santos@example.com"));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {
        UserUpdateDto invalidDto = UserUpdateDto.builder()
                .email("invalid-email")
                .build();

        mockMvc.perform(put("/api/v1/users/{id}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenUserExists() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", testUserId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", testUserId))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/users/{id}/activate", testUserId))
                .andExpect(status().isNoContent());
    }

    @Test
    void checkEmailExists_ShouldReturnTrue_WhenEmailExists() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/v1/users/exists?email=carlos.silva@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkEmailExists_ShouldReturnFalse_WhenEmailDoesNotExist() throws Exception {
        when(userService.existsByEmail(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/v1/users/exists?email=nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}