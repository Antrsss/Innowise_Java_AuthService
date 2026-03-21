package com.innowise.authservice.dto;

import com.innowise.authservice.entity.Role;

public record AuthRequest(
    String login,
    String password,
    Role role
) {}
