package com.example.complaintsystem.DTO.Security;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body containing the refresh token")
public class RefreshTokenRequestDTO {

    @Schema(description = "The refresh token string", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3d4-e5f6...")
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}