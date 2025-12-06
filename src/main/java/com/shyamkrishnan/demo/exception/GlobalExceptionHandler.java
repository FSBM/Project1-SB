package com.shyamkrishnan.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // handling validation errors from request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, String> validationErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            validationErrors.put(field, message);
        });
        
        responseBody.put("status", "error");
        responseBody.put("message", "Validation failed");
        responseBody.put("errors", validationErrors);
        
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    // catching runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "error");
        responseBody.put("message", ex.getMessage());
        
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    // handling any other exceptions that might occur
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "error");
        responseBody.put("message", "An unexpected error occurred");
        responseBody.put("details", ex.getMessage());
        
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
