package com.innowise.authservice.service.impl;

import com.innowise.authservice.dao.UserCredentialsDao;
import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.TokenResponse;
import com.innowise.authservice.entity.Role;
import com.innowise.authservice.entity.UserCredentials;
import com.innowise.authservice.exception.ResourceConflictException;
import com.innowise.authservice.exception.UnauthorizedException;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserCredentialsDao credentialsDao;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Override
  public void saveUser(AuthRequest authRequest) throws ResourceConflictException {
    if (credentialsDao.findByLogin(authRequest.login()).isPresent()) {
      throw new ResourceConflictException("Login already taken");
    }

    UserCredentials user = new UserCredentials();
    user.setLogin(authRequest.login());
    user.setPassword(passwordEncoder.encode(authRequest.password()));
    user.setRole(authRequest.role() == null ?  Role.ROLE_USER : authRequest.role());

    credentialsDao.save(user);
  }

  @Override
  public TokenResponse login(AuthRequest authRequest) throws UnauthorizedException {
    UserCredentials user = credentialsDao.findByLogin(authRequest.login())
        .orElseThrow(() -> new UnauthorizedException("Invalid login or password"));

    if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
      throw new UnauthorizedException("Invalid login or password");
    }

    return new TokenResponse(
        jwtService.generateAccessToken(user),
        jwtService.generateRefreshToken(user)
    );
  }

  @Override
  public String refreshAccessToken(String refreshToken) throws UnauthorizedException {
    if (jwtService.isTokenExpired(refreshToken)) {
      throw new UnauthorizedException("Refresh token expired");
    }

    String login = jwtService.extractLogin(refreshToken);
    UserCredentials user = credentialsDao.findByLogin(login)
        .orElseThrow(() -> new UnauthorizedException("User not found"));

    return jwtService.generateAccessToken(user);
  }
}
