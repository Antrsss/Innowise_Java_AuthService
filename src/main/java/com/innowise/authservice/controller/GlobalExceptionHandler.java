package com.innowise.authservice.controller;

import com.innowise.authservice.dto.TokenValidationResponse;
import com.innowise.authservice.exception.BaseServiceException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseServiceException.class)
  public ResponseEntity<Object> handleBaseException(BaseServiceException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("message", ex.getMessage());

    return new ResponseEntity<>(body, ex.getStatus());
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<TokenValidationResponse> handleJwtExceptions(JwtException ex) {
    String message = "Token error";
    if (ex instanceof ExpiredJwtException) message = "Token has expired";
    if (ex instanceof MalformedJwtException) message = "Invalid token format";

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new TokenValidationResponse(false, null, null, null, message));
  }
}
