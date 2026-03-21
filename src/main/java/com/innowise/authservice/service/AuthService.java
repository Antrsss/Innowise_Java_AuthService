package com.innowise.authservice.service;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.TokenResponse;
import com.innowise.authservice.exception.ResourceConflictException;
import com.innowise.authservice.exception.UnauthorizedException;

public interface AuthService {
  void saveUser(AuthRequest authRequest) throws ResourceConflictException;
  TokenResponse login(AuthRequest authRequest) throws UnauthorizedException;
  String refreshAccessToken(String refreshToken) throws UnauthorizedException;
}
