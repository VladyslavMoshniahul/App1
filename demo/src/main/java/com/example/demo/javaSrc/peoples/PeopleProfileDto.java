package com.example.demo.javaSrc.peoples;

import java.time.LocalDate;

public record PeopleProfileDto(
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
