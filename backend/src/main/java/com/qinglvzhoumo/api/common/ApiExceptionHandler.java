package com.qinglvzhoumo.api.common;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(ApiException error) {
    return ResponseEntity.status(error.status()).body(Map.of(
        "code", error.code(),
        "message", error.getMessage(),
        "timestamp", Instant.now().toString()
    ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException error) {
    String message = error.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(FieldError::getDefaultMessage)
        .orElse("请求参数不正确");
    return ResponseEntity.badRequest().body(Map.of(
        "code", "VALIDATION_ERROR",
        "message", message,
        "timestamp", Instant.now().toString()
    ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception error) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
        "code", "INTERNAL_ERROR",
        "message", "服务暂时不可用",
        "timestamp", Instant.now().toString()
    ));
  }
}
