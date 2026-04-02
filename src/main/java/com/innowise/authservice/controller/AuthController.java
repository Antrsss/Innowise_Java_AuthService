package com.innowise.authservice.controller;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.TokenResponse;
import com.innowise.authservice.dto.RefreshRequest;
import com.innowise.authservice.dto.TokenValidationResponse;
import com.innowise.authservice.entity.Role;
import com.innowise.authservice.exception.ResourceConflictException;
import com.innowise.authservice.exception.UnauthorizedException;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<String> saveUser(@RequestBody @Valid AuthRequest authRequest)
      throws ResourceConflictException {

    authService.saveUser(authRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body("User credentials saved successfully");
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody @Valid AuthRequest authRequest)
      throws UnauthorizedException {

    return ResponseEntity.ok(authService.login(authRequest));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest refreshRequest)
      throws UnauthorizedException {

    return ResponseEntity.ok(authService.refreshAccessToken(refreshRequest.refreshToken()));
  }

  @GetMapping("/validate")
  public ResponseEntity<TokenValidationResponse> validate(@RequestParam("token") String token)
      throws UnauthorizedException {

    Claims claims = jwtService.validateToken(token);

    String roleName = claims.get("role", String.class);
    Role role = Role.valueOf(roleName);

    return ResponseEntity.ok(new TokenValidationResponse(
        true,
        claims.get("userId", Long.class),
        claims.getSubject(),
        role.name(),
        "Token is active"
    ));
  }
}
