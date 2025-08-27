package br.com.jefersonmbs.recargapaywallet.domain.service;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserUpdateDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDto createUser(UserCreateDto userCreateDto);

    UserResponseDto getUserById(UUID id);

    List<UserResponseDto> getAllUsers();

    List<UserResponseDto> getAllActiveUsers();

    UserResponseDto updateUser(UUID id, UserUpdateDto userUpdateDto);

    void deleteUser(UUID id);

    void deactivateUser(UUID id);

    void activateUser(UUID id);

    List<UserResponseDto> searchUsersByName(String name);

    boolean existsByEmail(String email);
}