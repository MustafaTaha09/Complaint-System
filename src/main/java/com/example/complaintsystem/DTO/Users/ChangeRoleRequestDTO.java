package com.example.complaintsystem.DTO.Users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request body for changing a user's role")
public class ChangeRoleRequestDTO {

    @Schema(description = "The name of the new role to assign (e.g., ROLE_USER, ROLE_ADMIN)",
            requiredMode = Schema.RequiredMode.REQUIRED, // Indicate it's required
            example = "ROLE_ADMIN")
    @NotBlank(message = "Role name cannot be blank")
    private String role;
}