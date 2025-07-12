package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.comments.Comment;
import com.example.demo.javaSrc.comments.CommentRepository;
import com.example.demo.javaSrc.comments.CommentService;
import com.example.demo.javaSrc.people.People;
import com.example.demo.javaSrc.people.PeopleRepository;
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
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private PetitionRepository petitionRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    private People testUser;
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

         testUser = new People();
        testUser.setSchoolId(school.getId());
        testUser.setClassId(class1.getId());
        testUser.setFirstName("test");
        testUser.setLastName("ggg");
        testUser.setEmail("email@test.com");
        testUser.setPassword("wvvrvfrere");
        testUser.setRole(People.Role.STUDENT);
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
        Comment comment = new Comment(testUser.getId(), testPetition.getId(), "Great!");
        Comment saved = commentService.addComment(comment);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testDeleteComment() {
        Comment comment = commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "To delete"));
        commentService.deleteComment(comment.getId());
        assertThat(commentService.getComment(comment.getId())).isNull();
    }

    @Test
    void testGetComment() {
        Comment comment = commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "To get"));
        Comment found = commentService.getComment(comment.getId());
        assertThat(found).isNotNull();
        assertThat(found.getText()).isEqualTo("To get");
    }

    @Test
    void testGetAllComments() {
        commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "1"));
        commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "2"));
        List<Comment> all = commentService.getAllComments();
        assertThat(all).hasSize(2);
    }

    @Test
    void testGetCommentsByPetitionIdAndUserId() {
        commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "By both IDs"));
        List<Comment> list = commentService.getCommentsByPetitionIdAndUserId(testPetition.getId(), testUser.getId());
        assertThat(list).hasSize(1);
    }

    @Test
    void testGetCommentsByPetitionId() {
        commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "By petition"));
        List<Comment> list = commentService.getCommentsByPetitionId(testPetition.getId());
        assertThat(list).hasSize(1);
    }

    @Test
    void testGetCommentsByUserId() {
        commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "By user"));
        List<Comment> list = commentService.getCommentsByUserId(testUser.getId());
        assertThat(list).hasSize(1);
    }

    @Test
    void testDeleteCommentsByPetitionId() {
        commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "Del by petition"));
        commentService.deleteCommentsByPetitionId(testPetition.getId());
        assertThat(commentService.getCommentsByPetitionId(testPetition.getId())).isEmpty();
    }

    @Test
    void testDeleteCommentsByUserId() {
        commentService.addComment(new Comment(testUser.getId(), testPetition.getId(), "Del by user"));
        commentService.deleteCommentsByUserId(testUser.getId());
        assertThat(commentService.getCommentsByUserId(testUser.getId())).isEmpty();
    }

    
}
