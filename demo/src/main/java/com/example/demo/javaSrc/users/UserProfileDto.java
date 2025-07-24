package com.example.demo.javaSrc.users;

public record UserProfileDto(
    Long id,
    String firstName,
    String lastName,
    String aboutMe,
    String email,
    String role,
    String schoolName,
    String className
) {}
