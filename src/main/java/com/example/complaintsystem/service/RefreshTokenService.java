package com.example.complaintsystem.service;

import com.example.complaintsystem.entity.RefreshToken;
import com.example.complaintsystem.entity.User;
import com.example.complaintsystem.exception.ResourceNotFoundException; // Ensure this exists
import com.example.complaintsystem.exception.TokenRefreshException;
import com.example.complaintsystem.repository.RefreshTokenRepository;
import com.example.complaintsystem.repository.UserRepository;
import com.example.complaintsystem.security.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               JwtConfig jwtConfig) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtConfig = jwtConfig;
    }

    public Optional<RefreshToken> findByToken(String token) {
        log.debug("Finding refresh token by token string");
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Integer userId) {
        log.info("Creating refresh token for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cannot create refresh token. User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });

        // Delete existing token for the user to enforce single active refresh token
        int deletedCount = refreshTokenRepository.deleteByUser(user);
        if (deletedCount > 0) {
            log.info("Deleted {} existing refresh token(s) for user ID: {}", deletedCount, userId);
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtConfig.getRefreshExpirationMs()));
        refreshToken.setToken(UUID.randomUUID().toString()); // Generate random token

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.info("Successfully created refresh token with expiry {} for user ID: {}", refreshToken.getExpiryDate() ,userId);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            log.warn("Refresh token ID {} (Token: ...) has expired at {}. Deleting.", token.getId(), token.getExpiryDate());
            refreshTokenRepository.delete(token);

            // Throws 403 Forbidden
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        log.debug("Refresh token ID {} is still valid.", token.getId());
        return token;
    }

    @Transactional
    public int deleteByUserId(Integer userId) {
        log.info("Attempting to delete refresh token for user ID: {}", userId);
        // Ensure user exists before attempting to delete based on potentially invalid user object
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cannot delete refresh token. User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
        int deletedCount = refreshTokenRepository.deleteByUser(user);
        log.info("Deleted {} refresh token(s) for user ID: {}", deletedCount, userId);
        return deletedCount;
    }
}