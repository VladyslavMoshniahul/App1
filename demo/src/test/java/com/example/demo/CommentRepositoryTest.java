package com.example.demo;

import com.example.demo.javaSrc.comments.Comment;
import com.example.demo.javaSrc.comments.CommentRepository;
import com.example.demo.javaSrc.people.*;
import com.example.demo.javaSrc.petitions.Petition;
import com.example.demo.javaSrc.petitions.PetitionRepository;
import com.example.demo.javaSrc.school.ClassRepository;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private PetitionRepository petitionRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    private People testPeople;
    private Petition testPetition;

    @BeforeEach
    void setUp() {
        classRepository.deleteAll();
        peopleRepository.deleteAll();
        petitionRepository.deleteAll();
        commentRepository.deleteAll();
        schoolRepository.deleteAll();

        School school = new School();
        school.setName("Test School");
        school = schoolRepository.save(school);  

        SchoolClass class1 = new SchoolClass();
        class1.setName("1A");
        class1.setSchoolId(school.getId());  
        class1 = classRepository.save(class1);

        testPeople = new People();
        testPeople.setSchoolId(school.getId());
        testPeople.setClassId(class1.getId());
        testPeople.setFirstName("test");
        testPeople.setLastName("ggg");
        testPeople.setEmail("email@test.com");
        testPeople.setPassword("wvvrvfrere");
        testPeople.setRole(People.Role.STUDENT);
        testPeople = peopleRepository.save(testPeople);

        Date start = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDateTime.now().plusDays(10).atZone(ZoneId.systemDefault()).toInstant());

        testPetition = new Petition(
            "Test Petition",
            "This is a test petition",
            school.getId(),       
            class1.getId(),
            testPeople.getId(),
            start,
            end,
            Petition.Status.OPEN
        );
        testPetition = petitionRepository.save(testPetition);
    }


    @Test
    void testSaveAndFindByPetitionId() {
        Comment comment = new Comment(testPeople.getId(), testPetition.getId(), "Great idea!");
        commentRepository.save(comment);

        List<Comment> comments = commentRepository.findByPetitionId(testPetition.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getText()).isEqualTo("Great idea!");
    }

    @Test
    void testFindByPeopleId() {
        commentRepository.save(new Comment(testPeople.getId(), testPetition.getId(), "Great idea!"));

        List<Comment> comments = commentRepository.findByUserId(testPeople.getId());
        assertThat(comments).hasSize(1);
    }

    @Test
    void testFindByPetitionIdAndPeopleId() {
        commentRepository.save(new Comment(testPeople.getId(), testPetition.getId(), "Great idea!"));

        List<Comment> comments = commentRepository.findByPetitionIdAndUserId(testPetition.getId(), testPeople.getId());
        assertThat(comments).hasSize(1);
    }

    @Test
    void testDeleteByPetitionId() {
       
        commentRepository.save(new Comment(testPeople.getId(), testPetition.getId(), "Great idea!"));
        commentRepository.deleteByPetitionId(testPetition.getId());

        List<Comment> comments = commentRepository.findByPetitionId(testPetition.getId());
        assertThat(comments).isEmpty();
    }

    @Test
    void testDeleteByPeopleId() {
        commentRepository.save(new Comment(testPeople.getId(), testPetition.getId(), "Great idea!"));
        commentRepository.deleteByUserId(testPeople.getId());

        List<Comment> comments = commentRepository.findByUserId(testPeople.getId());
        assertThat(comments).isEmpty();
    }
}
