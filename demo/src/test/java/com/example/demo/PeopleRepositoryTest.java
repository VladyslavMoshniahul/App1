package com.example.demo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.users.*;
import com.example.demo.javaSrc.school.ClassRepository;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolRepository;

@SpringBootTest
public class PeopleRepositoryTest {
    @Autowired
    private UserRepository peopleRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private SchoolRepository schoolRepository;

    private User person;
    private SchoolClass class1;
    private School school;
    private User person2;
    private SchoolClass class2;
    private School school2;

    @BeforeEach
    void setUp() {
        peopleRepository.deleteAll();
        classRepository.deleteAll();
        schoolRepository.deleteAll();

        school = new School();
        school.setName("Test School");
        school = schoolRepository.save(school);
        class1 = new SchoolClass();
        class1.setName("1A");
        class1.setSchoolId(school.getId());
        classRepository.save(class1);

        school2 = new School();
        school2.setName("Test School 2");
        school2 = schoolRepository.save(school2);

        class2 = new SchoolClass();
        class2.setName("2A");
        class2.setSchoolId(school2.getId());
        classRepository.save(class2); 

        person = new User();
        person.setSchoolId(school.getId());
        person.setClassId(class1.getId());
        person.setEmail("testemail@gmail.com");
        person.setPassword("password123");
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setRole(User.Role.STUDENT);
        peopleRepository.save(person);

        person2 = new User();
        person2.setRole(User.Role.TEACHER);
        person2.setSchoolId(school2.getId());
        person2.setClassId(class2.getId());
        person2.setEmail("testemail1@gmail.com");
        person2.setPassword("password123");
        person2.setFirstName("John");
        person2.setLastName("Doe");
        peopleRepository.save(person2);

  
    }

    @Test
    void testFindByEmail() {
        String email = person.getEmail();
        Optional<User> found = peopleRepository.findByEmail(email);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }


    @Test
    void testFindByRole() {

        List<User> students = peopleRepository.findByRole(User.Role.STUDENT);
        List<User> teachers = peopleRepository.findByRole(User.Role.TEACHER);

        assertThat(students).hasSize(1);
        assertThat(teachers).hasSize(1);
    }    

   @Test
    void testFindBySchoolId() {

        List<User> school1People = peopleRepository.findBySchoolId(school.getId());
        List<User> school2People = peopleRepository.findBySchoolId(school2.getId());

        assertThat(school1People).hasSize(1);
        assertThat(school2People).hasSize(1);
    }

    @Test
    void testFindBySchoolIdAndClassId() {
         List<User> class1People = peopleRepository.findBySchoolIdAndClassId(school.getId(), class1.getId());
        List<User> class2People = peopleRepository.findBySchoolIdAndClassId(school2.getId(), class2.getId());

        assertThat(class1People).hasSize(1);
        assertThat(class2People).hasSize(1);
    }
}
