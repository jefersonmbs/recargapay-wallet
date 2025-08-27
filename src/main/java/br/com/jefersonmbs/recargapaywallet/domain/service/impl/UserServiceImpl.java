package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserUpdateDto;
import br.com.jefersonmbs.recargapaywallet.api.mapper.UserMapper;
import br.com.jefersonmbs.recargapaywallet.domain.entity.UserEntity;
import br.com.jefersonmbs.recargapaywallet.domain.repository.UserRepository;
import br.com.jefersonmbs.recargapaywallet.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        log.info("Creating user with email: {}", userCreateDto.getEmail());
        
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new IllegalArgumentException("User with email " + userCreateDto.getEmail() + " already exists");
        }

        UserEntity userEntity = userMapper.toEntity(userCreateDto);

        UserEntity savedUserEntity = userRepository.save(userEntity);
        log.info("User created successfully with ID: {}", savedUserEntity.getId());
        
        return userMapper.toResponseDto(savedUserEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        log.info("Fetching user with ID: {}", id);
        
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        return userMapper.toResponseDto(userEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");
        
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllActiveUsers() {
        log.info("Fetching all active users");
        
        return userRepository.findAllActive()
                .stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(UUID id, UserUpdateDto userUpdateDto) {
        log.info("Updating user with ID: {}", id);
        
        UserEntity existingUserEntity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        if (userUpdateDto.getEmail() != null &&
            !userUpdateDto.getEmail().equals(existingUserEntity.getEmail()) &&
            userRepository.existsByEmail(userUpdateDto.getEmail())) {
            throw new IllegalArgumentException("User with email " + userUpdateDto.getEmail() + " already exists");
        }

        userMapper.updateUserFromDto(userUpdateDto, existingUserEntity);
        UserEntity updatedUserEntity = userRepository.save(existingUserEntity);
        
        log.info("User updated successfully with ID: {}", updatedUserEntity.getId());
        return userMapper.toResponseDto(updatedUserEntity);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public void deactivateUser(UUID id) {
        log.info("Deactivating user with ID: {}", id);
        
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        userEntity.setActive(false);
        userRepository.save(userEntity);
        
        log.info("User deactivated successfully with ID: {}", id);
    }

    @Override
    public void activateUser(UUID id) {
        log.info("Activating user with ID: {}", id);
        
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        userEntity.setActive(true);
        userRepository.save(userEntity);
        
        log.info("User activated successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> searchUsersByName(String name) {
        log.info("Searching users by name: {}", name);
        
        return userRepository.findActiveByNameContainingIgnoreCase(name)
                .stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}