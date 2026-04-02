package com.innowise.authservice.dao;

import com.innowise.authservice.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialsDao extends JpaRepository<UserCredentials, Long> {
  Optional<UserCredentials> findByLogin(String login);
}
