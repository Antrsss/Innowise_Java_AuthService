package com.innowise.authservice.service.impl;

import com.innowise.authservice.entity.UserCredentials;
import com.innowise.authservice.exception.UnauthorizedException;
import com.innowise.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

  private static final int ACCESS_TOKEN_LIFETIME = 1000 * 60 * 15;
  private static final int REFRESH_TOKEN_LIFETIME = 1000 * 60 * 60 * 24;
  private static final String USER_ID = "userId";
  private static final String USER_ROLE = "role";

  private final SecretKey key;

  public JwtServiceImpl(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String extractLogin(String token) {
    return extractAllClaims(token)
        .getSubject();
  }

  public String generateAccessToken(UserCredentials user) {
    return Jwts.builder()
        .subject(user.getLogin())
        .claim(USER_ID, user.getId())
        .claim(USER_ROLE, user.getRole().name())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_LIFETIME))
        .signWith(key)
        .compact();
  }

  public String generateRefreshToken(UserCredentials user) {
    return Jwts.builder()
        .subject(user.getLogin())
        .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_LIFETIME))
        .signWith(key)
        .compact();
  }

  @Override
  public boolean isTokenExpired(String token) {
    return extractAllClaims(token)
        .getExpiration()
        .before(new Date());
  }

  @Override
  public void validateToken(String token) throws UnauthorizedException {
    try {
      Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token);
    } catch (Exception e) {
      throw new UnauthorizedException("Token validation failed: " + e.getMessage());
    }
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
