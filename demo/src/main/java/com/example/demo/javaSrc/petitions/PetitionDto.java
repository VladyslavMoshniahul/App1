package com.example.demo.javaSrc.petitions;

import java.util.Date;

public record PetitionDto(
    Long id,
    String title,
    String description,
    Date endDate,
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTitle'");
    }

    public void setDescription(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDescription'");
    }
}
