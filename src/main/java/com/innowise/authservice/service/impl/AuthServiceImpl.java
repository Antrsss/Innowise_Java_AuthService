package com.innowise.authservice.service.impl;

import com.innowise.authservice.dao.RefreshTokenDao;
import com.innowise.authservice.dao.UserCredentialsDao;
import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.TokenResponse;
import com.innowise.authservice.entity.RefreshToken;
import com.innowise.authservice.entity.Role;
import com.innowise.authservice.entity.UserCredentials;
import com.innowise.authservice.exception.ResourceConflictException;
import com.innowise.authservice.exception.UnauthorizedException;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private static final int REFRESH_TOKEN_LIFETIME = 1000 * 60 * 60 * 24;

  private final UserCredentialsDao credentialsDao;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RefreshTokenDao refreshTokenDao;

  @Override
  public void saveUser(AuthRequest authRequest) throws ResourceConflictException {
    if (credentialsDao.findByLogin(authRequest.login()).isPresent()) {
      throw new ResourceConflictException("Login already taken");
    }

    UserCredentials user = new UserCredentials();
    user.setLogin(authRequest.login());
    user.setPassword(passwordEncoder.encode(authRequest.password()));
    user.setRole(Role.ROLE_USER);

    credentialsDao.save(user);
  }

  @Override
  public TokenResponse login(AuthRequest authRequest) throws UnauthorizedException {
    UserCredentials user = credentialsDao.findByLogin(authRequest.login())
        .orElseThrow(() -> new UnauthorizedException("Invalid login or password"));

    if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
      throw new UnauthorizedException("Invalid login or password");
    }

    String accessToken = jwtService.generateAccessToken(user);
    String refreshTokenStr = jwtService.generateRefreshToken(user);

    refreshTokenDao.deleteByUser(user);

    RefreshToken refreshTokenEntity = RefreshToken.builder()
        .user(user)
        .token(refreshTokenStr)
        .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_LIFETIME))
        .build();
    refreshTokenDao.save(refreshTokenEntity);

    return new TokenResponse(
        accessToken,
        refreshTokenStr
    );
  }

  @Override
  public TokenResponse refreshAccessToken(String refreshToken) throws UnauthorizedException {

    RefreshToken tokenEntity = refreshTokenDao.findByToken(refreshToken)
        .orElseThrow(() -> new UnauthorizedException("Refresh token not found in database"));

    if (tokenEntity.getExpiryDate().isBefore(Instant.now()) || jwtService.isTokenExpired(refreshToken)) {
      refreshTokenDao.delete(tokenEntity);
      throw new UnauthorizedException("Refresh token expired");
    }

    UserCredentials user = tokenEntity.getUser();
    if (user == null) {
      throw new UnauthorizedException("User associated with token not found");
    }

    String newAccessToken = jwtService.generateAccessToken(user);

    return new TokenResponse(newAccessToken, refreshToken);
  }
}
