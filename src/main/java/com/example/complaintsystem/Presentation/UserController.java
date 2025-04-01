package com.example.complaintsystem.Presentation;


import com.example.complaintsystem.DTO.Users.*;
import com.example.complaintsystem.Entity.User;
import com.example.complaintsystem.Exceptions.BadRequestException;
import com.example.complaintsystem.Service.RoleAssignmentService;
import com.example.complaintsystem.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "APIs for managing users (Admin access often required)")
@SecurityRequirement(name = "Bearer Authentication")
// Apply JWT requirement to all methods in this controller by default since most of them requires it
public class UserController {

    private final UserService userService;

    private final RoleAssignmentService roleAssignmentService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @Autowired
    public UserController(UserService userService, RoleAssignmentService roleAssignmentService) {
        this.userService = userService;
        this.roleAssignmentService = roleAssignmentService;
    }

    @Operation(summary = "Get User Details", description = "Retrieves specific user details including their associated tickets and comments. Requires authentication (Admin or fetching own details).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user details",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    // This for getting the *SOME* details of specific user (username & department along with all of his tickets)
    @GetMapping("/{id}/details")
//    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<UserDetailsDTO> getUserDetails(@Parameter(description = "ID of the user to retrieve details for", required = true, example = "1")
                                                         @PathVariable Integer id) {
        UserDetailsDTO userDetails = userService.getUserDetails(id);
        return ResponseEntity.ok(userDetails);
    }

    @Operation(summary = "Get All Users (Admin Only)", description = "Retrieves a list of all users. Requires ADMIN privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))), // Describe list response
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only Admins should get all users
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        log.info("Returning all Users Successfully");
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get User by ID", description = "Retrieves basic details for a specific user. Requires authentication (Admin or fetching own profile).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<UserDTO> getUserById(@Parameter(description = "ID of the user to retrieve", required = true, example = "1")
                                               @PathVariable Integer id) {
        log.info("Received request to get user by ID: {}", id);
        UserDTO user = userService.getUserById(id);
        log.debug("Returning user details for ID: {}", id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get User Profile", description = "Retrieves profile information (excluding sensitive data) for a specific user. Requires authentication (Admin or fetching own profile).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserProfileDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{id}/profile")
//    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<UserProfileDTO> getUserProfile(@Parameter(description = "ID of the user whose profile to retrieve", required = true, example = "1")
                                                         @PathVariable Integer id) {
        log.info("Request received for user profile, ID: {}", id);
        UserProfileDTO profileDTO = userService.getUserProfile(id);
        return ResponseEntity.ok(profileDTO);
    }

    @Operation(summary = "Create New User (Admin Only)", description = "Creates a new user account.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details for the new user", required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserDTO.class))
            ),
            security = {} // this is for removing the security we made on the controller level for all methods.
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or username/email already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
//            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        log.info("Received request to register user with username: '{}'", createUserDTO.getUsername()); // Logging the incoming request
        UserDTO savedUser = userService.createUser(createUserDTO);
        log.info("Successfully registered user '{}'", savedUser.getUsername());
        return ResponseEntity.created(URI.create("/api/users/" + savedUser.getUserId())).body(savedUser);
    }

    @Operation(summary = "Update User", description = "Updates details for an existing user. Requires ADMIN role or user updating their own profile.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user details", required = true,
                    content = @Content(schema = @Schema(implementation = UpdateUserDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content),
            @ApiResponse(responseCode = "404", description = "User, Role, or Department not found", content = @Content)
    })
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<UserDTO> updateUser(@Parameter(description = "ID of the user to update", required = true, example = "1")
                                              @PathVariable Integer id,
                                              @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        UserDTO updatedUser = userService.updateUser(id, updateUserDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete User (Admin Only)", description = "Deletes a user by ID. Requires ADMIN privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "ID of the user to delete", required = true, example = "1")
                                           @PathVariable Integer id) {
        userService.deleteUser(id);
        log.info("Successfully Deleted user with id '{}'", id);
        return ResponseEntity.noContent().build();
    }

    // changeUsername
    @Operation(summary = "Change Username (Admin Only)", description = "Changes the username for a specified user. Requires ADMIN privileges. Returns a message and signals client to re-login.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The new username", required = true,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string", example = "new_username"))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username changed successfully", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)),
            @ApiResponse(responseCode = "400", description = "Bad Request - Username already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PatchMapping("/{id}/change-username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeUsername(@PathVariable Integer id, @RequestBody String newUsername) {
        userService.changeUsername(id, newUsername);
        try {
            userService.changeUsername(id, newUsername);
            return ResponseEntity.ok("Username changed successfully. Please log in again."); // Signal re-login to the front end
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // (changePassword)
    @Operation(summary = "Change Password", description = "Changes the password for a specified user. Requires ADMIN role or user changing their own password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Old and new password details", required = true,
                    content = @Content(schema = @Schema(implementation = ChangePasswordDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - Incorrect old password or invalid new password", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PatchMapping("/{id}/change-password")
//    @PreAuthorize("hasRole('ADMIN') or #id == principal.userId")
    public ResponseEntity<Void> changePassword(@Parameter(description = "ID of the user whose password to change", required = true, example = "1")
                                               @PathVariable Integer id,
                                               @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        log.info("Request received to change password for user ID: {}", id);
        userService.changePassword(id, changePasswordDTO);
        log.info("Successfully changed password for user ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    // Change User Role
    @Operation(summary = "Change User Role (Admin Only)", description = "Assigns a new role to a specified user. Requires ADMIN privileges.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The new role name for the user", required = true,
                    content = @Content(schema = @Schema(implementation = ChangeRoleRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role changed successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid or missing role name in body", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "User or Role Name not found", content = @Content)
    })
    @PatchMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can change roles
    public ResponseEntity<Void> changeUserRole(@Parameter(description = "ID of the user whose role is being changed", required = true, example = "5")
                                               @PathVariable Integer userId,
                                               @Valid @RequestBody ChangeRoleRequestDTO changeRoleRequest) {

        String newRoleName = changeRoleRequest.getRole();
        log.info("Request received to change role for user ID: {} to {}", userId, newRoleName);
        if (newRoleName == null || newRoleName.trim().isEmpty()) {
            log.warn("Bad request: Role name missing in request body for user ID {}", userId);
            return ResponseEntity.badRequest().build();
        }
        roleAssignmentService.changeUserRole(userId, newRoleName);
        log.info("Successfully changed role for user ID: {} to {}", userId, newRoleName);
        return ResponseEntity.noContent().build();
    }

}
