package com.example.complaintsystem.exception; // Or your package

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handler for Validation Errors (e.g., @Valid failure)
    // Important to handle failed validation constraints on DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {} on request: {}", errors, request.getDescription(false));

        // Return the map of field errors directly for validation issues
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // 400
    }

    // Handler for Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
        log.warn("Resource not found: {} on {}", ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND); // 404
    }

    // Handler for Bad Requests
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
        log.warn("Bad request: {} on {}", ex.getMessage(), request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // 400
    }

    // Handler for Access Denied (Authorization Failure)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "Access Denied: You do not have permission to perform this action.", // User-friendly message
                request.getDescription(false)
        );
        log.warn("Access Denied for user {} on {}: {}",
                (request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous/unauthenticated"),
                request.getDescription(false),
                ex.getMessage());

        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN); // 403
    }

    // General Fallback Handler
    // Important to catch any other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                "An internal server error occurred.",
                request.getDescription(false));

        // Log as ERROR, including the full stack trace for internal diagnosis
        log.error("Unhandled exception on {}: {}", request.getDescription(false), ex.getMessage(), ex);

        // Return the ErrorDetails object
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }

    @ExceptionHandler(value = TokenRefreshException.class)
    public ResponseEntity<ErrorDetails> handleTokenRefreshException(TokenRefreshException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
        log.warn("Token refresh failed: {} on {}", ex.getMessage(), request.getDescription(false));
        // Return 403 Forbidden as defined by @ResponseStatus on the exception class
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN); // 403
    }

}