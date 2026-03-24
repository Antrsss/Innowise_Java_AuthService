package com.innowise.authservice.service;

import com.innowise.authservice.dao.UserCredentialsDao;
import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.TokenResponse;
import com.innowise.authservice.entity.Role;
import com.innowise.authservice.entity.UserCredentials;
import com.innowise.authservice.exception.ResourceConflictException;
import com.innowise.authservice.exception.UnauthorizedException;
import com.innowise.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock
  private UserCredentialsDao credentialsDao;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtService jwtService;

  @InjectMocks
  private AuthServiceImpl authService;

  private AuthRequest authRequest;
  private UserCredentials user;

  @BeforeEach
  void setUp() {
    authRequest = new AuthRequest("testUser", "password", Role.ROLE_USER);
    user = new UserCredentials();
    user.setId(1L);
    user.setLogin("testUser");
    user.setPassword("encodedPassword");
    user.setRole(Role.ROLE_USER);
  }

  @Test
  void saveUser_ShouldSave_WhenLoginIsUnique() throws ResourceConflictException {
    when(credentialsDao.findByLogin(anyString())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    authService.saveUser(authRequest);

    verify(credentialsDao, times(1)).save(any(UserCredentials.class));
  }

  @Test
  void saveUser_ShouldThrowException_WhenLoginExists() {
    when(credentialsDao.findByLogin(anyString())).thenReturn(Optional.of(user));

    assertThrows(ResourceConflictException.class, () -> authService.saveUser(authRequest));
    verify(credentialsDao, never()).save(any());
  }

  @Test
  void login_ShouldReturnTokens_WhenCredentialsAreValid() throws UnauthorizedException {
    when(credentialsDao.findByLogin(authRequest.login())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(authRequest.password(), user.getPassword())).thenReturn(true);
    when(jwtService.generateAccessToken(user)).thenReturn("access_token");
    when(jwtService.generateRefreshToken(user)).thenReturn("refresh_token");

    TokenResponse response = authService.login(authRequest);

    assertNotNull(response);
    assertEquals("access_token", response.accessToken());
    assertEquals("refresh_token", response.refreshToken());
  }

  @Test
  void login_ShouldThrowUnauthorized_WhenPasswordIsWrong() {
    when(credentialsDao.findByLogin(authRequest.login())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.login(authRequest));
  }

  @Test
  void refreshAccessToken_ShouldReturnNewToken_WhenRefreshTokenIsValid() throws UnauthorizedException {
    String refreshToken = "valid_refresh_token";
    when(jwtService.isTokenExpired(refreshToken)).thenReturn(false);
    when(jwtService.extractLogin(refreshToken)).thenReturn("testUser");
    when(credentialsDao.findByLogin("testUser")).thenReturn(Optional.of(user));
    when(jwtService.generateAccessToken(user)).thenReturn("new_access_token");

    String newToken = authService.refreshAccessToken(refreshToken);

    assertEquals("new_access_token", newToken);
    verify(jwtService).generateAccessToken(user);
  }

  @Test
  void saveUser_ShouldSetDefaultRole_WhenRoleIsNull() throws ResourceConflictException {
    AuthRequest requestNoRole = new AuthRequest("newUser", "pass", null);
    when(credentialsDao.findByLogin(anyString())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");

    authService.saveUser(requestNoRole);

    verify(credentialsDao).save(argThat(u -> u.getRole() == Role.ROLE_USER));
  }

  @Test
  void login_ShouldThrowUnauthorized_WhenUserNotFound() {
    when(credentialsDao.findByLogin(anyString())).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.login(authRequest));
  }

  @Test
  void refreshAccessToken_ShouldThrowUnauthorized_WhenTokenExpired() {
    String expiredToken = "expired";
    when(jwtService.isTokenExpired(expiredToken)).thenReturn(true);

    assertThrows(UnauthorizedException.class, () -> authService.refreshAccessToken(expiredToken));
  }

  @Test
  void refreshAccessToken_ShouldThrowUnauthorized_WhenUserNotFoundAfterExtract() {
    String token = "valid_token";
    when(jwtService.isTokenExpired(token)).thenReturn(false);
    when(jwtService.extractLogin(token)).thenReturn("nonExistentUser");
    when(credentialsDao.findByLogin("nonExistentUser")).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.refreshAccessToken(token));
  }
}
