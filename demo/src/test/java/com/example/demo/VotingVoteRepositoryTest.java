package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleRepository;
import com.example.demo.javaSrc.school.ClassRepository;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolRepository;
import com.example.demo.javaSrc.voting.Vote;
import com.example.demo.javaSrc.voting.VoteRepository;
import com.example.demo.javaSrc.voting.VotingVariant;
import com.example.demo.javaSrc.voting.VotingVariantRepository;
import com.example.demo.javaSrc.voting.VotingVote;
import com.example.demo.javaSrc.voting.VotingVoteRepository;

@SpringBootTest
public class VotingVoteRepositoryTest {
    @Autowired
    private VotingVoteRepository votingVoteRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private PeopleRepository peopleRepository;
    @Autowired
    private VotingVariantRepository votingVariantRepository;

    private School school;
    private SchoolClass schoolClass;
    private People user;
    private Vote vote;
    private VotingVariant variant;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        classRepository.deleteAll();
        schoolRepository.deleteAll();
        classRepository.deleteAll();
        peopleRepository.deleteAll();
        votingVoteRepository.deleteAll();

        school = new School();
        school.setName("Test School");
        school = schoolRepository.save(school);
        
        schoolClass = new SchoolClass();
        schoolClass.setName("1A");
        schoolClass.setSchoolId(school.getId());
        classRepository.save(schoolClass);

        user = new People();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("vsd@vvxhsbg");
        user.setSchoolId(school.getId());
        user.setClassId(schoolClass.getId());
        user.setRole(People.Role.STUDENT);
        user.setPassword("password");
        peopleRepository.save(user);

        vote = new Vote();
        vote.setTitle("Test Vote");
        vote.setDescription("Some desc");
        vote.setStartDate(toDate(LocalDateTime.now()));
        vote.setEndDate(toDate(LocalDateTime.now().plusDays(1)));
        vote.setStatus(Vote.VoteStatus.OPEN);
        vote.setSchoolId(school.getId());
        vote.setClassId(schoolClass.getId());
        vote.setCreatedBy(user.getId());
        vote.setVotingLevel(Vote.VotingLevel.ACLASS);
        vote.setMultipleChoice(false);
        vote.setVariantsJson("[\"A\", \"B\"]");
        vote = voteRepository.save(vote);

        variant = votingVariantRepository.save(new VotingVariant(vote, "Variant A"));

        VotingVote voteEntity = new VotingVote();
        voteEntity.setVote(vote);
        voteEntity.setUserId(user.getId());
        voteEntity.setVariant(variant);
        voteEntity.setVoteDate(toDate(LocalDateTime.now()));
        votingVoteRepository.save(voteEntity);
    }

    private Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void testFindByVotingId() {
        List<VotingVote> votes = votingVoteRepository.findByVotingId(vote.getId());
        assertEquals(1, votes.size());
        assertEquals(vote.getId(), votes.get(0).getVote().getId());
    }

    @Test
    void testFindByUserId() {
        List<VotingVote> votes = votingVoteRepository.findByUserId(user.getId());
        assertEquals(1, votes.size());
        assertEquals(user.getId(), votes.get(0).getUserId());
    }

    @Test
    void testFindByVotingIdAndUserIdAndVariantId() {
        Optional<VotingVote> found = votingVoteRepository.findByVotingIdAndUserIdAndVariantId(
                vote.getId(), user.getId(), variant.getId());
        assertTrue(found.isPresent());
        assertEquals(variant.getId(), found.get().getVariant().getId());
    }

    @Test
    void testCountByVotingIdAndVariantId() {
        long count = votingVoteRepository.countByVotingIdAndVariantId(vote.getId(), variant.getId());
        assertEquals(1, count);
    }

    @Test
    void testFindByVotingIdAndUserId() {
        List<VotingVote> votes = votingVoteRepository.findByVotingIdAndUserId(vote.getId(), user.getId());
        assertEquals(1, votes.size());
        assertEquals(user.getId(), votes.get(0).getUserId());
    }
}
