package com.example.complaintsystem.Presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.complaintsystem.DTO.Roles.RoleDTO;
import com.example.complaintsystem.DTO.Roles.RoleRequestDTO;
import com.example.complaintsystem.Service.RoleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody; // Keep Spring's RequestBody

import java.net.URI; // For creating location header
import java.util.List;

@RestController
@RequestMapping("/api/roles") // Consistent API path prefix
@Tag(name = "Roles", description = "APIs for managing user roles (ADMIN Access Required for All)")
@PreAuthorize("hasRole('ADMIN')") // Secure ALL endpoints in this controller for ADMIN only
@SecurityRequirement(name = "Bearer Authentication") // Require JWT for all endpoints here
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Get All Roles", description = "Retrieves a list of all available roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of roles",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = RoleDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Request received to get all roles");
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Get Role by ID", description = "Retrieves details of a specific role by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Role not found with the specified ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(
            @Parameter(description = "Unique ID of the role to retrieve", required = true, example = "1")
            @PathVariable Integer id) {
        log.info("Request received to get role by ID: {}", id);
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Create New Role", description = "Creates a new role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the role to be created", required = true,
                    content = @Content(schema = @Schema(implementation = RoleRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data or role name already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(
            @Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        log.info("Request received to create role with name: '{}'", roleRequestDTO.getRoleName());
        RoleDTO savedRole = roleService.createRole(roleRequestDTO);
        // Create the location URI for the new resource
        URI location = URI.create("/api/roles/" + savedRole.getRoleId());
        return ResponseEntity.created(location).body(savedRole);
    }

    @Operation(summary = "Update Existing Role", description = "Updates the name of an existing role.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New details for the role", required = true,
                    content = @Content(schema = @Schema(implementation = RoleRequestDTO.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data or role name already exists for another role", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Role not found with the specified ID", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(
            @Parameter(description = "ID of the role to update", required = true, example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        log.info("Request received to update role ID: {} to name: '{}'", id, roleRequestDTO.getRoleName());
        RoleDTO updatedRole = roleService.updateRole(id, roleRequestDTO);
        return ResponseEntity.ok(updatedRole);
    }

    @Operation(summary = "Delete Role", description = "Deletes a specific role by its ID. Fails if the role is assigned to any users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request - Role is currently in use and cannot be deleted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Role not found with the specified ID", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "ID of the role to delete", required = true, example = "1")
            @PathVariable Integer id) {
        log.info("Request received to delete role ID: {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}