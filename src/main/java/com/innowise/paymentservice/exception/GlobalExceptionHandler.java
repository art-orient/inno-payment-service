package com.innowise.paymentservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String TIMESTAMP = "timestamp";
  private static final String STATUS = "status";
  private static final String ERROR = "error";
  private static final String DETAILS = "details";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put(TIMESTAMP, Instant.now());
    body.put(STATUS, HttpStatus.BAD_REQUEST.value());
    body.put(ERROR, "Validation failed");
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
            .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));
    body.put(DETAILS, fieldErrors);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put(TIMESTAMP, Instant.now());
    body.put(STATUS, HttpStatus.BAD_REQUEST.value());
    body.put(ERROR, "Constraint violation");
    body.put(DETAILS, ex.getMessage());
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put(TIMESTAMP, Instant.now());
    body.put(STATUS, HttpStatus.BAD_REQUEST.value());
    body.put(ERROR, ex.getMessage());
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
    Map<String, Object> body = new HashMap<>();
    body.put(TIMESTAMP, Instant.now());
    body.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put(ERROR, ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
