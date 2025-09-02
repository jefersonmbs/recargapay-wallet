package br.com.jefersonmbs.recargapaywallet.api.controller;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserUpdateDto;
import br.com.jefersonmbs.recargapaywallet.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserControllerApi {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        log.info("REST request to create user with email: {}", userCreateDto.getEmail());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userCreateDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("REST request to get user by ID: {}", id);
        
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(required = false) String name) {
        
        List<UserResponseDto> users;
        
        if (name != null && !name.trim().isEmpty()) {
            log.info("REST request to search users by name: {}", name);
            users = userService.searchUsersByName(name.trim());
        } else if (activeOnly) {
            log.info("REST request to get all active users");
            users = userService.getAllActiveUsers();
        } else {
            log.info("REST request to get all users");
            users = userService.getAllUsers();
        }
        
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto userUpdateDto) {
        
        log.info("REST request to update user with ID: {}", id);
        
        return ResponseEntity.ok(userService.updateUser(id, userUpdateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user with ID: {}", id);
        
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActiveUser(@PathVariable Long id) {
        log.info("REST request to toggle active status for user with ID: {}", id);
        
        userService.toggleActiveUser(id);
        return ResponseEntity.noContent().build();
    }

}