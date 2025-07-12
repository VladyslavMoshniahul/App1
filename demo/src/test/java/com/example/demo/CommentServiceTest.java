package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.comments.*;
import com.example.demo.javaSrc.users.*;
import com.example.demo.javaSrc.petitions.Petition;
import com.example.demo.javaSrc.petitions.PetitionRepository;
import com.example.demo.javaSrc.school.ClassRepository;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class CommentServiceTest {
    @Autowired
    private PetitionsCommentRepository commentRepository;

    @Autowired
    private PetitionsCommentService commentService;

    @Autowired
    private UserRepository peopleRepository;

    @Autowired
    private PetitionRepository petitionRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    private User testUser;
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

         testUser = new User();
        testUser.setSchoolId(school.getId());
        testUser.setClassId(class1.getId());
        testUser.setFirstName("test");
        testUser.setLastName("ggg");
        testUser.setEmail("email@test.com");
        testUser.setPassword("wvvrvfrere");
        testUser.setRole(User.Role.STUDENT);
        testUser = peopleRepository.save(testUser);

        Date start = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDateTime.now().plusDays(10).atZone(ZoneId.systemDefault()).toInstant());

        testPetition = new Petition(
            "Test Petition",
            "This is a test petition",
            school.getId(),       
            class1.getId(),
            testUser.getId(),
            start,
            end,
            Petition.Status.OPEN
        );
        testPetition = petitionRepository.save(testPetition);
    }

    @Test
    void testAddComment() {
        PetitionsComment comment = new PetitionsComment(testUser.getId(), testPetition.getId(), "Great!");
        PetitionsComment saved = commentService.addComment(comment);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testDeleteComment() {
        PetitionsComment comment = commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "To delete"));
        commentService.deleteComment(comment.getId());
        assertThat(commentService.getComment(comment.getId())).isNull();
    }

    @Test
    void testGetComment() {
        PetitionsComment comment = commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "To get"));
        PetitionsComment found = commentService.getComment(comment.getId());
        assertThat(found).isNotNull();
        assertThat(found.getText()).isEqualTo("To get");
    }

    @Test
    void testGetAllComments() {
        commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "1"));
        commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "2"));
        List<PetitionsComment> all = commentService.getAllComments();
        assertThat(all).hasSize(2);
    }

    @Test
    void testGetCommentsByPetitionIdAndUserId() {
        commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "By both IDs"));
        List<PetitionsComment> list = commentService.getCommentsByPetitionIdAndUserId(testPetition.getId(), testUser.getId());
        assertThat(list).hasSize(1);
    }

    @Test
    void testGetCommentsByPetitionId() {
        commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "By petition"));
        List<PetitionsComment> list = commentService.getCommentsByPetitionId(testPetition.getId());
        assertThat(list).hasSize(1);
    }

    @Test
    void testGetCommentsByUserId() {
        commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "By user"));
        List<PetitionsComment> list = commentService.getCommentsByUserId(testUser.getId());
        assertThat(list).hasSize(1);
    }

    @Test
    void testDeleteCommentsByPetitionId() {
        commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "Del by petition"));
        commentService.deleteCommentsByPetitionId(testPetition.getId());
        assertThat(commentService.getCommentsByPetitionId(testPetition.getId())).isEmpty();
    }

    @Test
    void testDeleteCommentsByUserId() {
        commentService.addComment(new PetitionsComment(testUser.getId(), testPetition.getId(), "Del by user"));
        commentService.deleteCommentsByUserId(testUser.getId());
        assertThat(commentService.getCommentsByUserId(testUser.getId())).isEmpty();
    }

    
}
