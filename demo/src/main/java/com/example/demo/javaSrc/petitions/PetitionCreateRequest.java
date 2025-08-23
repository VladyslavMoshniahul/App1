package com.example.demo.javaSrc.petitions;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record PetitionCreateRequest(
    String   title,
    String   description,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime startDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime endDate,
    Long     classId
) {}
