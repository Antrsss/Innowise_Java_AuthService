package com.innowise.authservice.service;

import com.innowise.authservice.entity.Role;
import com.innowise.authservice.entity.UserCredentials;
import com.innowise.authservice.exception.UnauthorizedException;
import com.innowise.authservice.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

  private JwtServiceImpl jwtService;
  private final String secret = "my_super_secret_key_for_testing_purposes_only_32_chars";
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
    assertThrows(UnauthorizedException.class, () -> jwtService.validateToken(invalidToken));
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
