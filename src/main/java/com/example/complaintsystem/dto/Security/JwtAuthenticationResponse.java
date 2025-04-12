package com.example.complaintsystem.dto.Security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Response containing the JWT access token and refresh token after successful login or refresh")
public class JwtAuthenticationResponse {

    @Schema(description = "The JWT access token (short-lived)", example = "eyJhbGciOiJIUzUx...")
    private String accessToken;

    @Schema(description = "The type of token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "The refresh token (long-lived) used to obtain new access tokens", example = "a1b2c3d4-e5f6...")
    private String refreshToken;

    public JwtAuthenticationResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}