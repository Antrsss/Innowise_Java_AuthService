package com.innowise.authservice.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseServiceException {
  public ForbiddenException(String message) {
    super(message, HttpStatus.FORBIDDEN);
  }
}
