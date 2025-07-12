package com.example.demo.javaSrc.voting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VotingVoteRepository extends JpaRepository<VotingVote, Long> {
    List<VotingVote> findByVotingId(Long votingId);
    List<VotingVote> findByUserId(Long userId);
    Optional<VotingVote> findByVotingIdAndUserIdAndVariantId(Long votingId, Long userId, Long variantId);
    long countByVotingIdAndVariantId(Long votingId, Long variantId);
    List<VotingVote> findByVotingIdAndUserId(Long votingId, Long userId); 
}