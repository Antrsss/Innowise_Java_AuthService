package com.innowise.authservice.dto;

public record TokenValidationResponse(
    boolean isValid,
    Long userId,
    String login,
    String role,
    String message
) {}
