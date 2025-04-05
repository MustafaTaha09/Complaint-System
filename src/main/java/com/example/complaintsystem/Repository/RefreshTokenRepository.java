package com.example.complaintsystem.Repository;

import com.example.complaintsystem.Entity.RefreshToken;
import com.example.complaintsystem.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // For delete operation

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Transactional
    int deleteByUser(User user);
}