package com.example.demo.javaSrc.users;

import java.time.LocalDate;

public record UserProfileDto(
    Long id,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String aboutMe,
    String email,
    String role,
    String schoolName,
    String className
) {}
