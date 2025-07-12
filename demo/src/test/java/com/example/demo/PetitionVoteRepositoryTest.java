package com.example.demo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleRepository;
import com.example.demo.javaSrc.petitions.Petition;
import com.example.demo.javaSrc.petitions.PetitionRepository;
import com.example.demo.javaSrc.petitions.PetitionVote;
import com.example.demo.javaSrc.petitions.PetitionVoteRepository;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PetitionVoteRepositoryTest {
     @Autowired
    private PetitionVoteRepository petitionVoteRepository;

    @Autowired
    private PetitionRepository petitionRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Test
    void testExistsByPetitionIdAndStudentId() {
        petitionVoteRepository.deleteAll();
        petitionRepository.deleteAll();
        peopleRepository.deleteAll();
        schoolRepository.deleteAll();
        
        School school = new School();
        school.setName("Test School");
        school = schoolRepository.save(school);

        Petition petition = new Petition();
        petition.setTitle("Test");
        petition.setDescription("Test");
        petition.setSchoolId(school.getId());
        petition.setStartDate(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        petition.setEndDate(Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        petition.setStatus(Petition.Status.OPEN);
        petition.setCreatedBy(1L);
        petition = petitionRepository.save(petition);

        People student = new People();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        student.setPassword("password");
        student.setRole(People.Role.STUDENT);
        student.setSchoolId(school.getId());
        student = peopleRepository.save(student);

        PetitionVote vote = new PetitionVote();
        vote.setPetition(petition);
        vote.setStudentId(student.getId());
        vote.setVote(PetitionVote.VoteVariant.YES);
        vote = petitionVoteRepository.save(vote);

        boolean exists = petitionVoteRepository.existsByPetitionIdAndStudentId(petition.getId(), student.getId());
        assertThat(exists).isTrue();

        boolean notExists = petitionVoteRepository.existsByPetitionIdAndStudentId(9999L, 9999L);
        assertThat(notExists).isFalse();
    }
}
