package com.fiveguys.trip_planner.exception;

import com.fiveguys.trip_planner.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateEmailException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(LocalDateTime.now(), 409, "CONFLICT", e.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNickname(DuplicateNicknameException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(LocalDateTime.now(), 409, "CONFLICT", e.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(DuplicatePhoneException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePhone(DuplicatePhoneException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(LocalDateTime.now(), 409, "CONFLICT", e.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<ErrorResponse> handleLogin(InvalidLoginException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(LocalDateTime.now(), 401, "UNAUTHORIZED", e.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(LocalDateTime.now(), 400, "BAD_REQUEST", message, request.getRequestURI())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(LocalDateTime.now(), 400, "BAD_REQUEST", e.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleOAuth2(OAuth2AuthenticationException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(LocalDateTime.now(), 401, "UNAUTHORIZED", e.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        LocalDateTime.now(),
                        500,
                        "INTERNAL_SERVER_ERROR",
                        "서버 내부 오류가 발생했습니다.",
                        request.getRequestURI()
                )
        );
    }
}