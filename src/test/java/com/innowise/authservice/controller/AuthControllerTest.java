package com.innowise.authservice.controller;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.RefreshRequest;
import com.innowise.authservice.dto.TokenResponse;
import com.innowise.authservice.dto.TokenValidationResponse;
import com.innowise.authservice.entity.Role;
import com.innowise.authservice.exception.ResourceConflictException;
import com.innowise.authservice.exception.UnauthorizedException;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private AuthService authService;

  @Mock
  private JwtService jwtService;

  @InjectMocks
  private AuthController authController;

  private AuthRequest authRequest;
  private TokenResponse tokenResponse;
  private RefreshRequest refreshRequest;

  @BeforeEach
  void setUp() {
    authRequest = new AuthRequest("testuser", "password123", Role.ROLE_USER);
    tokenResponse = new TokenResponse("access-token-123", "refresh-token-456");
    refreshRequest = new RefreshRequest("refresh-token-456");
  }

  @Test
  void saveUser_ShouldReturnCreated_WhenUserIsSavedSuccessfully() throws ResourceConflictException {
    doNothing().when(authService).saveUser(authRequest);

    ResponseEntity<String> response = authController.saveUser(authRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualTo("User credentials saved successfully");
    verify(authService, times(1)).saveUser(authRequest);
  }

  @Test
  void saveUser_ShouldThrowResourceConflictException_WhenUserAlreadyExists() throws ResourceConflictException {
    doThrow(new ResourceConflictException("User already exists"))
        .when(authService).saveUser(authRequest);

    assertThatThrownBy(() -> authController.saveUser(authRequest))
        .isInstanceOf(ResourceConflictException.class)
        .hasMessage("User already exists");
    verify(authService, times(1)).saveUser(authRequest);
  }

  @Test
  void login_ShouldReturnTokenResponse_WhenCredentialsAreValid() throws UnauthorizedException {
    when(authService.login(authRequest)).thenReturn(tokenResponse);

    ResponseEntity<TokenResponse> response = authController.login(authRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(tokenResponse);
    assertThat(response.getBody().accessToken()).isEqualTo("access-token-123");
    assertThat(response.getBody().refreshToken()).isEqualTo("refresh-token-456");
    verify(authService, times(1)).login(authRequest);
  }

  @Test
  void login_ShouldThrowUnauthorizedException_WhenCredentialsAreInvalid() throws UnauthorizedException {
    when(authService.login(authRequest)).thenThrow(new UnauthorizedException("Invalid credentials"));

    assertThatThrownBy(() -> authController.login(authRequest))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Invalid credentials");
    verify(authService, times(1)).login(authRequest);
  }

  @Test
  void refresh_ShouldReturnNewTokenResponse_WhenRefreshTokenIsValid() throws UnauthorizedException {
    String newAccessToken = "new-access-token-789";
    when(authService.refreshAccessToken(refreshRequest.refreshToken()))
        .thenReturn(new TokenResponse(newAccessToken, "refresh-token-456"));

    ResponseEntity<TokenResponse> response = authController.refresh(refreshRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().accessToken()).isEqualTo(newAccessToken);
    assertThat(response.getBody().refreshToken()).isEqualTo(refreshRequest.refreshToken());
    verify(authService, times(1)).refreshAccessToken(refreshRequest.refreshToken());
  }

  @Test
  void refresh_ShouldThrowUnauthorizedException_WhenRefreshTokenIsInvalid() throws UnauthorizedException {
    when(authService.refreshAccessToken(refreshRequest.refreshToken()))
        .thenThrow(new UnauthorizedException("Invalid refresh token"));

    assertThatThrownBy(() -> authController.refresh(refreshRequest))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Invalid refresh token");
    verify(authService, times(1)).refreshAccessToken(refreshRequest.refreshToken());
  }

  @Test
  void validate_ShouldReturnOk_WhenTokenIsValid() throws UnauthorizedException {
    String validToken = "valid-jwt-token";
    String expectedUsername = "testUser";
    Long expectedId = 1L;
    String expectedRole = "ROLE_USER";

    Claims mockClaims = mock(Claims.class);
    when(mockClaims.getSubject()).thenReturn(expectedUsername);
    when(mockClaims.get("userId", Long.class)).thenReturn(expectedId);
    when(mockClaims.get("role", String.class)).thenReturn(expectedRole);

    when(jwtService.validateToken(validToken)).thenReturn(mockClaims);

    ResponseEntity<TokenValidationResponse> response = authController.validate(validToken);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    TokenValidationResponse body = response.getBody();

    assertThat(body).isNotNull();
    assertThat(body.isValid()).isTrue();
    assertThat(body.login()).isEqualTo(expectedUsername);
    assertThat(body.userId()).isEqualTo(expectedId);
    assertThat(body.role()).isEqualTo(expectedRole);

    verify(jwtService, times(1)).validateToken(validToken);
  }

  @Test
  void validate_ShouldThrowUnauthorizedException_WhenTokenIsInvalid() throws UnauthorizedException {
    String invalidToken = "invalid-jwt-token";
    doThrow(new UnauthorizedException("Token expired"))
        .when(jwtService).validateToken(invalidToken);

    assertThatThrownBy(() -> authController.validate(invalidToken))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Token expired");
    verify(jwtService, times(1)).validateToken(invalidToken);
  }

  @Test
  void validate_ShouldThrowUnauthorizedException_WhenTokenIsMalformed() throws UnauthorizedException {
    String malformedToken = "malformed-token";
    doThrow(new UnauthorizedException("Invalid token format"))
        .when(jwtService).validateToken(malformedToken);

    assertThatThrownBy(() -> authController.validate(malformedToken))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Invalid token format");
    verify(jwtService, times(1)).validateToken(malformedToken);
  }
}