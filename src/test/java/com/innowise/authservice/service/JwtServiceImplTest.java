package com.innowise.authservice.service;

import com.innowise.authservice.entity.Role;
import com.innowise.authservice.entity.UserCredentials;
import com.innowise.authservice.service.impl.JwtServiceImpl;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtServiceImplTest {

  private JwtServiceImpl jwtService;

  @Value("${jwt.secret}")
  private String secret;

  private UserCredentials user;

  @BeforeEach
  void setUp() {
    jwtService = new JwtServiceImpl(secret);

    user = new UserCredentials();
    user.setId(100L);
    user.setLogin("darya_test");
    user.setRole(Role.ROLE_USER);
  }

  @Test
  void shouldGenerateAndExtractAccessToken() {
    String token = jwtService.generateAccessToken(user);

    assertNotNull(token);
    assertEquals("darya_test", jwtService.extractLogin(token));
  }

  @Test
  void shouldGenerateRefreshToken() {
    String token = jwtService.generateRefreshToken(user);

    assertNotNull(token);
    assertEquals("darya_test", jwtService.extractLogin(token));
  }

  @Test
  void validateToken_ShouldPassForValidToken() {
    String token = jwtService.generateAccessToken(user);
    assertDoesNotThrow(() -> jwtService.validateToken(token));
  }

  @Test
  void validateToken_ShouldThrowException_WhenTokenIsInvalid() {
    String invalidToken = "invalid.token.here";
    assertThrows(JwtException.class, () -> jwtService.validateToken(invalidToken));
  }

  @Test
  void isTokenExpired_ShouldReturnFalseForNewToken() {
    String token = jwtService.generateAccessToken(user);
    assertFalse(jwtService.isTokenExpired(token));
  }

  @Test
  void shouldHandleClaimsCorrectly() {
    String token = jwtService.generateAccessToken(user);
    String login = jwtService.extractLogin(token);

    assertEquals(user.getLogin(), login);
  }
}
