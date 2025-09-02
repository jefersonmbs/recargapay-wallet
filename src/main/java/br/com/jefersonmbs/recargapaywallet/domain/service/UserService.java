package br.com.jefersonmbs.recargapaywallet.domain.service;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserUpdateDto;

import java.util.List;

public interface UserService {

    UserResponseDto createUser(UserCreateDto userCreateDto);

    UserResponseDto getUserById(Long id);

    List<UserResponseDto> getAllUsers();

    List<UserResponseDto> getAllActiveUsers();

    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);

    void deleteUser(Long id);

    void toggleActiveUser(Long id);

    List<UserResponseDto> searchUsersByName(String name);

}