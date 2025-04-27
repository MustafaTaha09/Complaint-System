package com.example.complaintsystem.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtConfig jwtConfig;

    private final ResourceLoader resourceLoader;

    // Change key fields
    // private final SecretKey key; // Symmetric key
    private final PrivateKey privateKey;
    private final PublicKey publicKey;


    @Autowired
    public JwtTokenProvider(JwtConfig jwtConfig, ResourceLoader resourceLoader) {
        this.jwtConfig = jwtConfig;
        this.resourceLoader = resourceLoader;

        try {
            this.privateKey = loadPrivateKey(jwtConfig.getPrivateKeyLocation());
            this.publicKey = loadPublicKey(jwtConfig.getPublicKeyLocation());
            logger.info("Successfully loaded RSA public and private keys.");
        } catch (Exception e) {
            logger.error("Failed to load RSA keys on startup!", e);
            throw new RuntimeException("Could not initialize JWT provider: Failed to load keys", e);
        }
    }

    private PrivateKey loadPrivateKey(String location) throws NoSuchAlgorithmException, InvalidKeySpecException, java.io.IOException {
        logger.debug("Loading private key from: {}", location);
        String keyString = readKeyFromClasspath(location);
        String privateKeyPEM = keyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("\\r\\n|\\r|\\n", "") // Remove newlines/carriage returns
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey loadPublicKey(String location) throws NoSuchAlgorithmException, InvalidKeySpecException, java.io.IOException {
        logger.debug("Loading public key from: {}", location);
        String keyString = readKeyFromClasspath(location);
        String publicKeyPEM = keyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("\\r\\n|\\r|\\n", "") // Remove newlines/carriage returns
                .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }

    private String readKeyFromClasspath(String location) throws java.io.IOException {
        InputStream inputStream = resourceLoader.getResource(location).getInputStream();
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpirationMs());
        // Build the JWT claims
        Claims claims = Jwts.claims().setSubject(userPrincipal.getUsername());
        claims.put("userId", userPrincipal.getUserId()); // Add the user ID
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())); // Add roles/authorities


        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
//                .signWith(key, SignatureAlgorithm.HS512)
                .signWith(privateKey, SignatureAlgorithm.RS256) // Use private key now
                .compact();
    }

    public String getUsernameFromJWT(String token) {
//        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();

        try {
            return getClaimsFromJWT(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Failed to get username from JWT: {}", e.getMessage());
            return null; // Or rethrow a custom exception
        }
    }

    public Integer getUserIdFromJWT(String token) {
        try {
            Claims claims = getClaimsFromJWT(token);
            return claims.get("userId", Integer.class); // Use type-safe getter
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Failed to get userId from JWT: {}", e.getMessage());
            return null;
        }
    }

//    public Integer getUserIdFromJWT(String token) {
//        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
//        return (Integer) claims.get("userId");
//    }


    private Claims getClaimsFromJWT(String token) {
        // log.debug("Attempting to parse claims from token");
        return Jwts.parserBuilder()
                .setSigningKey(publicKey) // Verify with public key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey) // Verify with public key
                    .build()
                    .parseClaimsJws(authToken);
            logger.trace("JWT validation successful for token ending with: ...{}", authToken.length() > 10 ? authToken.substring(authToken.length() - 10) : authToken);
            return true;
        } catch (MalformedJwtException ex) { logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) { logger.warn("Expired JWT token: {}", ex.getMessage()); // Often expected, so WARN
        } catch (UnsupportedJwtException ex) { logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) { logger.error("JWT claims string is empty or invalid: {}", ex.getMessage());
        } catch (SignatureException ex) {
            logger.error("JWT signature validation failed: {}", ex.getMessage());
        }
        return false;
    }

//    public boolean validateToken(String authToken) {
//        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
//            return true;
//        } catch (MalformedJwtException ex) {
//            logger.error("Invalid JWT token");
//        } catch (ExpiredJwtException ex) {
//            logger.error("Expired JWT token");
//        } catch (UnsupportedJwtException ex) {
//            logger.error("Unsupported JWT token");
//        } catch (IllegalArgumentException ex) {
//            logger.error("JWT claims string is empty.");
//        }
//        return false;
//    }
}