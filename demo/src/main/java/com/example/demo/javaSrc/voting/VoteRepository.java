package com.example.demo.javaSrc.voting;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByClassIdAndSchoolId(Long classId, Long schoolId);

    List<Vote> findBySchoolId(Long schoolId);

    List<Vote> findByTitle(String title);

    List<Vote> findByDescription(String description);

    List<Vote> findByCreatedBy(Long createdBy);

    List<Vote> findByStartDateBetween(Date startDate, Date endDate);

    List<Vote> findByVotingLevel(Vote.VotingLevel votingLevel);

    List<Vote> findByStatus(Vote.VoteStatus status);
}