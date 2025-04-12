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

@OpenAPIDefinition(
        info = @Info(title = "Complaint System API", version = "v1", description = "API Documentation"),
        security = {@SecurityRequirement(name = "Bearer Authentication")} // Apply the scheme globally
)
public class JwtConfig {

//    @Value("${jwt.secret}") // We are getting it from application properties
//    private String secret;

    // fields for key locations (private & public)
    @Value("${jwt.private.key.location}")
    private String privateKeyLocation;

    @Value("${jwt.public.key.location}")
    private String publicKeyLocation;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    @Value("${jwt.refreshExpirationMs}")
    private long refreshExpirationMs;

//    public String getSecret() {
//        return secret;
//    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public String getPrivateKeyLocation() { return privateKeyLocation; }

    public String getPublicKeyLocation() { return publicKeyLocation; }
}