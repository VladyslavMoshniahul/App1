package com.example.demo.javaSrc.voting;

import java.time.LocalDateTime;
import java.util.List;

public record CreateVoteRequest(String className,
                                String title, String description,
                                LocalDateTime startDateTime, LocalDateTime endDateTime,
                                Vote.VotingLevel votingLevel, boolean isMultipleChoice,
                                List<VotingVariant> variants) {

}
