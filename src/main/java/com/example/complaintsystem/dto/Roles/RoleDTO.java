package com.example.complaintsystem.dto.Roles;

import io.swagger.v3.oas.annotations.media.Schema; // Import
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for Role details") // Add Schema description
public class RoleDTO {

    @Schema(description = "Unique identifier of the role", example = "1")
    private Integer roleId;

    @Schema(description = "Name of the role (e.g., ROLE_USER, ROLE_ADMIN)", example = "ROLE_ADMIN")
    private String roleName;
}