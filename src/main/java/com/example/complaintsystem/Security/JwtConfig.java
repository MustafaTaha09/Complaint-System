package com.example.complaintsystem.Security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
// Define the Security Scheme globally
@SecurityScheme(
        name = "Bearer Authentication", // Can be any name, used for reference
        type = SecuritySchemeType.HTTP, // HTTP authentication
        bearerFormat = "JWT",           // Mention JWT format
        scheme = "bearer"               // Use the "bearer" scheme
)
// Apply the security globally (optional, you can apply per-controller or per-method)
// This assumes MOST endpoints need authentication, except the ones explicitly permitted.
@OpenAPIDefinition(
        info = @Info(title = "Complaint System API", version = "v1", description = "API Documentation"), // Redundant if using properties, but good practice
        security = {@SecurityRequirement(name = "Bearer Authentication")} // Apply the scheme globally
)
public class JwtConfig {

    @Value("${jwt.secret}") // we are getting it from application properties
    private String secret;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    public String getSecret() {
        return secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}