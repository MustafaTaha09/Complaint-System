package com.example.complaintsystem.dto.Roles;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data Transfer Object for creating or updating a Role")
public class RoleRequestDTO {

    @Schema(description = "Name for the role. Conventionally starts with 'ROLE_'. Cannot be blank.",
            example = "ROLE_SUPPORT_AGENT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Role name cannot be blank")
    @Size(max = 50, message = "Role name cannot exceed 50 characters")
    // "Role name must start with ROLE_
    private String roleName;
}