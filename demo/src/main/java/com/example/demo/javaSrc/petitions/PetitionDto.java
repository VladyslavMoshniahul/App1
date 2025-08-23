package com.example.demo.javaSrc.petitions;

import java.time.LocalDateTime;

public record PetitionDto(
    Long id,
    String title,
    String description,
    LocalDateTime endDate,
    int currentVotes,
    int threshold,
    boolean pendingDirector,
    boolean approvedByDirector
) {
    public static PetitionDto from(Petition p, int totalStudents) {
        int threshold = totalStudents / 2 + 1;
        boolean pending = p.getDirectorsDecision() == Petition.DirectorsDecision.PENDING;
        boolean approved = p.getDirectorsDecision() == Petition.DirectorsDecision.APPROVED;

        return new PetitionDto(
            p.getId(),
            p.getTitle(),
            p.getDescription(),
            p.getEndDate(),
            p.getCurrentPositiveVoteCount(),
            threshold,
            pending,
            approved
        );
    }

    public void setTitle(String string) {
        throw new UnsupportedOperationException("Unimplemented method 'setTitle'");
    }

    public void setDescription(String string) {
        throw new UnsupportedOperationException("Unimplemented method 'setDescription'");
    }
}
