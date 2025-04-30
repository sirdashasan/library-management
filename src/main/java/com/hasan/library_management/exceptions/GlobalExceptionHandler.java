package com.hasan.library_management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles custom application-specific exceptions thrown with custom HTTP status.
     * Typically used for domain-related errors (e.g., "User not found").
     **/
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionResponse> handleApiException(ApiException apiException) {
        return new ResponseEntity<>(new ExceptionResponse(
                apiException.getMessage(),
                apiException.getHttpStatus().value(),
                LocalDateTime.now()), apiException.getHttpStatus());
    }

    /**
     * Handles validation errors when @Valid fails on DTO fields.
     * Collects all field-specific errors and returns them in a consistent structure.
     **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = new ArrayList<>();

        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("field", fieldName);
            errorMap.put("message", errorMessage);

            errors.add(errorMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases where JSON can't be parsed (e.g., invalid enum values).
     * Useful for malformed JSON or unexpected request bodies.
     **/
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleJsonParseException(HttpMessageNotReadableException ex) {
        List<Map<String, String>> errors = new ArrayList<>();

        String rawMessage = ex.getMessage();
        String field = "request";
        String message = "Invalid JSON format.";

        if (rawMessage != null && rawMessage.contains("Role")) {
            field = "role";
            message = "Invalid role value. Must be 'LIBRARIAN' or 'PATRON'.";
        }

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("field", field);
        errorMap.put("message", message);
        errors.add(errorMap);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catches all unhandled exceptions and returns a generic error response.
     * Should be the last fallback to avoid leaking sensitive stack traces.
     **/
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception exception) {
        List<Map<String, String>> errors = new ArrayList<>();

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("field", "internal");
        errorMap.put("message", exception.getMessage());
        errors.add(errorMap);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}