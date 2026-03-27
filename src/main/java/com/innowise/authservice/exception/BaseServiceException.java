package com.innowise.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseServiceException extends RuntimeException {
  private final HttpStatus status;

  BaseServiceException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }
}
