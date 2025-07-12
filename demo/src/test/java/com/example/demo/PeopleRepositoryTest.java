package com.example.demo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

@SpringBootTest
public class PeopleRepositoryTest {
    @Autowired
    private PeopleRepository peopleRepository;
    @Autowired
    private ClassRepository classRepository;
    @Autowired
    private SchoolRepository schoolRepository;

    private People person;
    private SchoolClass class1;
    private School school;
    private People person2;
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

        person = new People();
        person.setSchoolId(school.getId());
        person.setClassId(class1.getId());
        person.setEmail("testemail@gmail.com");
        person.setPassword("password123");
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setRole(People.Role.STUDENT);
        peopleRepository.save(person);

        person2 = new People();
        person2.setRole(People.Role.TEACHER);
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
        Optional<People> found = peopleRepository.findByEmail(email);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }


    @Test
    void testFindByRole() {

        List<People> students = peopleRepository.findByRole(People.Role.STUDENT);
        List<People> teachers = peopleRepository.findByRole(People.Role.TEACHER);

        assertThat(students).hasSize(1);
        assertThat(teachers).hasSize(1);
    }    

   @Test
    void testFindBySchoolId() {

        List<People> school1People = peopleRepository.findBySchoolId(school.getId());
        List<People> school2People = peopleRepository.findBySchoolId(school2.getId());

        assertThat(school1People).hasSize(1);
        assertThat(school2People).hasSize(1);
    }

    @Test
    void testFindBySchoolIdAndClassId() {
         List<People> class1People = peopleRepository.findBySchoolIdAndClassId(school.getId(), class1.getId());
        List<People> class2People = peopleRepository.findBySchoolIdAndClassId(school2.getId(), class2.getId());

        assertThat(class1People).hasSize(1);
        assertThat(class2People).hasSize(1);
    }
}
