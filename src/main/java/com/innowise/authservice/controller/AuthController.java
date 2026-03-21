package com.innowise.authservice.controller;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.dto.TokenResponse;
import com.innowise.authservice.dto.RefreshRequest;
import com.innowise.authservice.exception.ResourceConflictException;
import com.innowise.authservice.exception.UnauthorizedException;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.JwtService;
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

  @PostMapping("/save")
  public ResponseEntity<String> saveUser(@RequestBody AuthRequest authRequest)
      throws ResourceConflictException {

    authService.saveUser(authRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body("User credentials saved successfully");
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody AuthRequest authRequest)
      throws UnauthorizedException {

    return ResponseEntity.ok(authService.login(authRequest));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest refreshRequest)
      throws UnauthorizedException {

    String newAccessToken = authService.refreshAccessToken(refreshRequest.refreshToken());
    return ResponseEntity.ok(new TokenResponse(newAccessToken, refreshRequest.refreshToken()));
  }

  @GetMapping("/validate")
  public ResponseEntity<String> validate(@RequestParam("token") String token)
      throws UnauthorizedException {

    jwtService.validateToken(token);
    return ResponseEntity.ok("Token is valid");
  }
}
