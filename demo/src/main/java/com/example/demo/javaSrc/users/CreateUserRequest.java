package com.example.demo.javaSrc.users;

import java.time.LocalDate;

public record CreateUserRequest(
    String firstName,
    String lastName,
    String email,
    String password,
    User.Role role,
    String schoolName,
    String className,
    LocalDate dateOfBirth
) {
    public CreateUserRequest {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if(role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
    }
}
