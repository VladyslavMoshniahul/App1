package com.example.demo.javaSrc.voting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface VotingVoteRepository extends JpaRepository<VotingVote, Long> {
}