package com.innowise.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "credentials")
@Data
public class UserCredentials {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String login;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  private Role role;

  private boolean active = true;
}
