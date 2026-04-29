package com.bondworks.common;

import java.time.Instant;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<Map<String, Object>> api(ApiException ex) {
    return ResponseEntity.status(ex.status()).body(Map.of(
        "timestamp", Instant.now(),
        "code", ex.code(),
        "message", ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(Map.of(
        "timestamp", Instant.now(),
        "code", "VALIDATION_FAILED",
        "message", "Request validation failed"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<Map<String, Object>> integrity(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
        "timestamp", Instant.now(),
        "code", "DATA_INTEGRITY_VIOLATION",
        "message", "Request conflicts with persisted trading state"));
  }
}
