package br.com.jefersonmbs.recargapaywallet.api.controller;

import br.com.jefersonmbs.recargapaywallet.api.dto.UserCreateDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserResponseDto;
import br.com.jefersonmbs.recargapaywallet.api.dto.UserUpdateDto;
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

import java.util.List;

@Tag(name = "Users", description = "API for digital wallet system user management")
public interface UserControllerApi {

    @Operation(summary = "Create user", description = "Creates a new user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or email already exists",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto userCreateDto);

    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "User not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable Long id);

    @Operation(summary = "List users", description = "Lists all system users with filter options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User list returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<List<UserResponseDto>> getAllUsers(
            @Parameter(description = "Filter only active users")
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @Parameter(description = "Search users by name")
            @RequestParam(required = false) String name);

    @Operation(summary = "Update user", description = "Updates data of an existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data, user not found or email already exists",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto userUpdateDto);

    @Operation(summary = "Delete user", description = "Permanently removes a user from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "User not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id);

    @Operation(summary = "Toggle user active status", description = "Toggles the active/inactive status of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User status changed successfully"),
            @ApiResponse(responseCode = "400", description = "User not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    ResponseEntity<Void> toggleActiveUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id);
}