package com.innowise.authservice.service;

import com.innowise.authservice.entity.UserCredentials;
import com.innowise.authservice.exception.UnauthorizedException;

public interface JwtService {
  String extractLogin(String token);
  String generateAccessToken(UserCredentials user);
  String generateRefreshToken(UserCredentials user);
  boolean isTokenExpired(String token);
  void validateToken(String token) throws UnauthorizedException;
}
