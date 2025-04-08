package com.example.complaintsystem.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // returns 403
public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String token, String message) {
        super(String.format("Refresh Token Failed [%s]: %s", token, message));
    }
    public TokenRefreshException(String message) {
        super(message);
    }
}
