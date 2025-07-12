package com.example.demo.javaSrc.voting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VotingParticipantRepository extends JpaRepository<VotingParticipant, Long> {
    List<VotingParticipant> findByVoteId(Long votingId);
    Optional<VotingParticipant> findByVoteIdAndUserId(Long votingId, Long userId);
}