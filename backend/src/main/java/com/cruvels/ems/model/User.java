package com.cruvels.ems.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This table stores LOGIN credentials only (email + hashed password + role).
// Personal/work details live in the separate Employee table.
// Splitting these two keeps "who can log in" separate from "who this person is at work".
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    // Never store plain text passwords! This holds a BCrypt HASH, not the real password.
    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING) // stores "ADMIN" / "MANAGER" / "EMPLOYEE" as text, not a number
    private Role role;

    private LocalDateTime createdAt = LocalDateTime.now();
}
