package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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

@SpringBootTest
public class VotingVariantRepositoryTest {
    @Autowired
    private VotingVariantRepository votingVariantRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private PeopleRepository peopleRepository;

    @Test
    void testFindByVoteId() {
        voteRepository.deleteAll();
        classRepository.deleteAll();
        schoolRepository.deleteAll();
        classRepository.deleteAll();
        peopleRepository.deleteAll();

        School school = new School();
        school.setName("Test School");
        school = schoolRepository.save(school);
        
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName("1A");
        schoolClass.setSchoolId(school.getId());
        classRepository.save(schoolClass);

        People user = new People();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("vsd@vvxhsbg");
        user.setSchoolId(school.getId());
        user.setClassId(schoolClass.getId());
        user.setRole(People.Role.STUDENT);
        user.setPassword("password");
        peopleRepository.save(user);

        Vote vote = new Vote();
        vote.setTitle("Test Vote");
        vote.setDescription("This is a test vote");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plusOneDay = now.plusDays(1);
        Date startTime = (Date) Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = (Date) Date.from(plusOneDay.atZone(ZoneId.systemDefault()).toInstant());
        vote.setStartDate(startTime);
        vote.setEndDate(endTime);
        vote.setStatus(Vote.VoteStatus.OPEN);
        vote.setSchoolId(school.getId());
        vote.setClassId(schoolClass.getId());
        vote.setCreatedBy(1L);
        vote.setVotingLevel(Vote.VotingLevel.SCHOOL);
        vote.setVariants(List.of(
            new VotingVariant(vote, "Variant 1"),
            new VotingVariant(vote, "Variant 2"),
            new VotingVariant(vote, "Variant 3")
        ));
        vote.setMultipleChoice(false);
        vote.setVariantsJson("[\"Variant 1\", \"Variant 2\", \"Variant 3\"]");
        voteRepository.save(vote);

        List<VotingVariant> votingVariant = votingVariantRepository.findByVoteId(vote.getId());

        assertEquals(3, votingVariant.size());
    }
}
