package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserUpdateDto;
import br.com.jefersonmbs.recargapaywallet.api.mapper.UserMapper;
import br.com.jefersonmbs.recargapaywallet.domain.entity.User;
import br.com.jefersonmbs.recargapaywallet.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userCreateDto = UserCreateDto.builder()
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .build();

        userUpdateDto = UserUpdateDto.builder()
                .name("Ana Santos")
                .email("ana.santos@example.com")
                .phone("21987654321")
                .active(true)
                .build();
    }

    @Test
    void createUser_ShouldReturnUserResponseDto_WhenValidInput() {
        UserResponseDto expectedResponse = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .active(true)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserCreateDto.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(expectedResponse);

        UserResponseDto result = userService.createUser(userCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Carlos Silva");
        assertThat(result.getEmail()).isEqualTo("carlos.silva@example.com");
        assertThat(result.getActive()).isTrue();
        verify(userRepository).existsByEmail("carlos.silva@example.com");
        verify(userMapper).toEntity(userCreateDto);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponseDto(testUser);
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userCreateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).existsByEmail("carlos.silva@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUserResponseDto_WhenUserExists() {
        UserResponseDto expectedResponse = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .active(true)
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDto(testUser)).thenReturn(expectedResponse);

        UserResponseDto result = userService.getUserById(testUserId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getName()).isEqualTo("Carlos Silva");
        verify(userRepository).findById(testUserId);
        verify(userMapper).toResponseDto(testUser);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(testUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(testUserId);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = Collections.singletonList(testUser);
        UserResponseDto expectedResponse = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .active(true)
                .build();

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponseDto(testUser)).thenReturn(expectedResponse);

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Carlos Silva");
        verify(userRepository).findAll();
        verify(userMapper).toResponseDto(testUser);
    }

    @Test
    void getAllActiveUsers_ShouldReturnOnlyActiveUsers() {
        List<User> activeUsers = Collections.singletonList(testUser);
        UserResponseDto expectedResponse = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .active(true)
                .build();

        when(userRepository.findAllActive()).thenReturn(activeUsers);
        when(userMapper.toResponseDto(testUser)).thenReturn(expectedResponse);

        List<UserResponseDto> result = userService.getAllActiveUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getActive()).isTrue();
        verify(userRepository).findAllActive();
        verify(userMapper).toResponseDto(testUser);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenValidInput() {
        UserResponseDto expectedResponse = UserResponseDto.builder()
                .id(testUserId)
                .name("Ana Santos")
                .email("ana.santos@example.com")
                .phone("21987654321")
                .active(true)
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("ana.santos@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDto(testUser)).thenReturn(expectedResponse);

        UserResponseDto result = userService.updateUser(testUserId, userUpdateDto);

        assertThat(result).isNotNull();
        verify(userRepository).findById(testUserId);
        verify(userMapper).updateUserFromDto(userUpdateDto, testUser);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toResponseDto(testUser);
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(testUserId, userUpdateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        when(userRepository.existsById(testUserId)).thenReturn(true);

        userService.deleteUser(testUserId);

        verify(userRepository).existsById(testUserId);
        verify(userRepository).deleteById(testUserId);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.existsById(testUserId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(testUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).existsById(testUserId);
        verify(userRepository, never()).deleteById(testUserId);
    }

    @Test
    void deactivateUser_ShouldDeactivateUser_WhenUserExists() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deactivateUser(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void activateUser_ShouldActivateUser_WhenUserExists() {
        testUser.setActive(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.activateUser(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void searchUsersByName_ShouldReturnMatchingUsers() {
        List<User> matchingUsers = Collections.singletonList(testUser);
        UserResponseDto expectedResponse = UserResponseDto.builder()
                .id(testUserId)
                .name("Carlos Silva")
                .email("carlos.silva@example.com")
                .phone("11987654321")
                .active(true)
                .build();

        when(userRepository.findActiveByNameContainingIgnoreCase("Carlos")).thenReturn(matchingUsers);
        when(userMapper.toResponseDto(testUser)).thenReturn(expectedResponse);

        List<UserResponseDto> result = userService.searchUsersByName("Carlos");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).contains("Carlos");
        verify(userRepository).findActiveByNameContainingIgnoreCase("Carlos");
        verify(userMapper).toResponseDto(testUser);
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        when(userRepository.existsByEmail("carlos.silva@example.com")).thenReturn(true);

        boolean result = userService.existsByEmail("carlos.silva@example.com");

        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("carlos.silva@example.com");
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        boolean result = userService.existsByEmail("nonexistent@example.com");

        assertThat(result).isFalse();
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }
}