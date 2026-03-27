package com.innowise.authservice.service;

import com.innowise.authservice.entity.UserCredentials;
import com.innowise.authservice.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;

public interface JwtService {
  String extractLogin(String token);
  String generateAccessToken(UserCredentials user);
  String generateRefreshToken(UserCredentials user);
  boolean isTokenExpired(String token);
  Claims validateToken(String token) throws UnauthorizedException;
}
