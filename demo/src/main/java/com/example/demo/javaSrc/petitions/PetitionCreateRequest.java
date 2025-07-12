package com.example.demo.javaSrc.petitions;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public record PetitionCreateRequest(
    String   title,
    String   description,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate,
    Long     classId
) {}
