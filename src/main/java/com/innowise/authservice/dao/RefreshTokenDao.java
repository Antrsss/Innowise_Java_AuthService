package com.innowise.authservice.dao;

import com.innowise.authservice.entity.RefreshToken;
import com.innowise.authservice.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenDao extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
  void deleteByUser(UserCredentials user);
}
