package com.example.demo.javaSrc.voting;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VotingVariantRepository extends JpaRepository<VotingVariant, Long> {
    List<VotingVariant> findByVoteId(Long votingId);
}