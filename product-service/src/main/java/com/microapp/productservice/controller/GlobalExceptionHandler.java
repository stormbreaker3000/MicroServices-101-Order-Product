package com.microapp.productservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the product-service REST layer.
 * <p>
 * Converts Bean Validation failures into a friendly HTTP 400 response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle @Valid / @Validated constraint violations on request bodies.
     *
     * @param ex The validation exception thrown by Spring MVC.
     * @return HTTP 400 with a map of field → first error message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (a, b) -> a));
        return ResponseEntity.badRequest().body(Map.of("errors", fieldErrors));
    }
}
