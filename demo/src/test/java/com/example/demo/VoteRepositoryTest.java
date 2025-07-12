package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


import com.example.demo.javaSrc.voting.Vote;
import com.example.demo.javaSrc.voting.VoteRepository;

@SpringBootTest
public class VoteRepositoryTest {
    @Autowired
    private VoteRepository voteRepository;

    @BeforeEach
    public void setUp() {
        voteRepository.deleteAll();
    }

    @Test
    @DisplayName("Test findById")
    public void testFindById() {
        Vote vote = createSampleVote();
        vote = voteRepository.save(vote);

        var result = voteRepository.findById(vote.getId());
        assertTrue(result.isPresent());
        assertEquals("Test Voting", result.get().getTitle());
    }

    @Test
    public void testFindByClassIdAndSchoolId() {
        Vote vote = createSampleVote();
        vote.setClassId(2L);
        vote.setSchoolId(5L);
        voteRepository.save(vote);

        List<Vote> results = voteRepository.findByClassIdAndSchoolId(2L, 5L);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    public void testFindBySchoolId() {
        voteRepository.save(createSampleVote());
        List<Vote> results = voteRepository.findBySchoolId(1L);
        assertEquals(1, results.size());
    }

    @Test
    public void testFindByTitle() {
        voteRepository.save(createSampleVote());
        List<Vote> results = voteRepository.findByTitle("Test Voting");
        assertFalse(results.isEmpty());
    }

    @Test
    public void testFindByDescription() {
        voteRepository.save(createSampleVote());
        List<Vote> results = voteRepository.findByDescription("Description");
        assertFalse(results.isEmpty());
    }

    @Test
    public void testFindByCreatedBy() {
        Vote vote = createSampleVote();
        vote.setCreatedBy(999L);
        voteRepository.save(vote);

        List<Vote> results = voteRepository.findByCreatedBy(999L);
        assertEquals(1, results.size());
    }

    @Test
    public void testFindByStartDateBetween() {
        Vote vote = createSampleVote();
        vote.setStartDate(new Date(System.currentTimeMillis() - 10000));
        vote.setEndDate(new Date(System.currentTimeMillis() + 10000));
        voteRepository.save(vote);

        Date from = new Date(System.currentTimeMillis() - 50000);
        Date to = new Date(System.currentTimeMillis() + 50000);
        List<Vote> results = voteRepository.findByStartDateBetween(from, to);
        assertFalse(results.isEmpty());
    }

    @Test
    public void testFindByVotingLevel() {
        Vote vote = createSampleVote();
        vote.setVotingLevel(Vote.VotingLevel.SCHOOL);
        voteRepository.save(vote);

        List<Vote> results = voteRepository.findByVotingLevel(Vote.VotingLevel.SCHOOL);
        assertFalse(results.isEmpty());
    }

    @Test
    public void testFindByStatus() {
        Vote vote = createSampleVote();
        vote.setStatus(Vote.VoteStatus.OPEN);
        voteRepository.save(vote);

        List<Vote> results = voteRepository.findByStatus(Vote.VoteStatus.OPEN);
        assertFalse(results.isEmpty());
    }

    private Vote createSampleVote() {
        Vote vote = new Vote();
        vote.setSchoolId(1L);
        vote.setClassId(1L);
        vote.setTitle("Test Voting");
        vote.setDescription("Description");
        vote.setCreatedBy(1L);
        vote.setStartDate(new Date());
        vote.setEndDate(new Date(System.currentTimeMillis() + 1000000));
        vote.setVotingLevel(Vote.VotingLevel.SCHOOL);
        vote.setStatus(Vote.VoteStatus.OPEN);
        vote.setMultipleChoice(false);
        vote.setVariantsJson("[\"Option 1\",\"Option 2\"]");
        return vote;
    }
}
