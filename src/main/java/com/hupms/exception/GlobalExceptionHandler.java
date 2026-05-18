package com.hupms.exception;

import com.hupms.util.ApiResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> notFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    ResponseEntity<ApiResponse<Void>> forbidden(UnauthorizedAccessException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), ex);
    }

    @ExceptionHandler({DuplicatePassportException.class, DuplicateKeyException.class})
    ResponseEntity<ApiResponse<Void>> conflict(Exception ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), ex);
    }

    @ExceptionHandler({GroupCapacityExceededException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<ApiResponse<Void>> badRequest(Exception ex) {
        String message = ex instanceof MethodArgumentNotValidException ? "Validation failed" : ex.getMessage();
        return error(HttpStatus.BAD_REQUEST, message, ex);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiResponse<Void>> unauthorized(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password", ex);
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message, Exception ex) {
        return ResponseEntity.status(status).body(ApiResponse.failure(message, ex.getClass().getSimpleName()));
    }
}
