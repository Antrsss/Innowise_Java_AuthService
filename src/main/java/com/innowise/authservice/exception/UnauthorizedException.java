package com.innowise.authservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseServiceException {
  public UnauthorizedException(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }
}
