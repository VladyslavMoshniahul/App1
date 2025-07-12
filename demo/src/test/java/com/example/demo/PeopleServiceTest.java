package com.example.demo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.demo.javaSrc.users.*;


@SpringBootTest
public class PeopleServiceTest {
    
    @MockBean
    private UserRepository peopleRepository;

    @Autowired
    private UserService peopleService;

    @BeforeEach
    void setUp() {
         MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllPeople() {
          User person1 = new User();
        person1.setFirstName("John");
        person1.setEmail("testemail@gmail.com");
        person1.setPassword("password123");
        person1.setRole(User.Role.STUDENT);
        person1.setSchoolId(1L);
        person1.setClassId(1L);

        User person2 = new User();
        person2.setFirstName("Jane");
        person2.setEmail("testemail1@gmail.com");
        person2.setPassword("password123");
        person2.setRole(User.Role.TEACHER);
        person2.setSchoolId(1L);
        person2.setClassId(2L);

        when(peopleRepository.findAll()).thenReturn(List.of(person1, person2));

        List<User> result = peopleService.getAllUsers();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(1).getFirstName()).isEqualTo("Jane");
    }

    @Test
    void testCreatePeople() {
         User person = new User();
        String email = "testemail@gmail.com";
        person.setEmail(email);
        person.setPassword("password123");
        person.setRole(User.Role.STUDENT);
        person.setSchoolId(1L);
        person.setClassId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");

        when(peopleRepository.save(person)).thenReturn(person);

        User created = peopleService.createUser(person);

        assertThat(created.getFirstName()).isEqualTo("John");
        assertThat(created.getLastName()).isEqualTo("Doe");
        verify(peopleRepository, times(1)).save(person);
    }

    @Test
    void testGetBySchoolAndClass() {
        Long schoolId = 1L;
        Long classId = 2L;
        User person1 = new User();
        person1.setSchoolId(schoolId);
        person1.setClassId(classId);

        when(peopleRepository.findBySchoolIdAndClassId(schoolId, classId)).thenReturn(List.of(person1));

        List<User> result = peopleService.getBySchoolAndClass(schoolId, classId);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getSchoolId()).isEqualTo(schoolId);
        assertThat(result.get(0).getClassId()).isEqualTo(classId);
    }

    @Test
    void testGetBySchoolClassAndRole() {
        Long schoolId = 1L;
        Long classId = 2L;
        User person1 = new User();
        person1.setSchoolId(schoolId);
        person1.setClassId(classId);
        person1.setRole(User.Role.STUDENT);

        when(peopleRepository.findBySchoolIdAndClassId(schoolId, classId)).thenReturn(List.of(person1));

        List<User> result = peopleService.getBySchoolClassAndRole(schoolId, classId, User.Role.STUDENT);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRole()).isEqualTo(User.Role.STUDENT);
    }

    @Test
    void testGetPeopleByRole() {
        User person1 = new User();
        person1.setRole(User.Role.STUDENT);
        User person2 = new User();
        person2.setRole(User.Role.TEACHER);

        when(peopleRepository.findByRole(User.Role.STUDENT)).thenReturn(List.of(person1));

        List<User> result = peopleService.getUserByRole("STUDENT");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRole()).isEqualTo(User.Role.STUDENT);
    }

    @Test
    void testFindByEmail() {
        User person = new User();
        String email = "ggg@ggg.com";
        person.setEmail(email);

        when(peopleRepository.findByEmail(email)).thenReturn(java.util.Optional.of(person));
        User found = peopleService.findByEmail(email);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo(email);
    }

    @Test
    void testUpdateProfile() {
        User existingPerson = new User();
        Long id = existingPerson.getId();
        existingPerson.setFirstName("Old Name");

        User updatedData = new User();
        updatedData.setFirstName("New Name");

        when(peopleRepository.findById(id)).thenReturn(java.util.Optional.of(existingPerson));
        when(peopleRepository.save(existingPerson)).thenReturn(existingPerson);

        User result = peopleService.updateProfile(id, updatedData);

        assertThat(result.getFirstName()).isEqualTo("New Name");
        verify(peopleRepository, times(1)).save(existingPerson);
    }

    @Test
    void testUpdateUser() {  
        User existing = new User();
        Long id = existing.getId();
        existing.setFirstName("OldName");
        existing.setLastName("OldLast");
        existing.setEmail("old@email.com");
        existing.setPassword("oldpass");
        existing.setAboutMe("Old about me");

        User updatedData = new User();
        updatedData.setFirstName("John1");
        updatedData.setLastName("Doe1");
        updatedData.setEmail("hh1@h.com");
        updatedData.setPassword("1234561");
        updatedData.setAboutMe("About me1");

        when(peopleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(peopleRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = peopleService.updateUser(id, updatedData);

        assertThat(updated).isNotNull();
        assertThat(updated.getFirstName()).isEqualTo("John1");
        assertThat(updated.getLastName()).isEqualTo("Doe1");
        assertThat(updated.getEmail()).isEqualTo("hh1@h.com");
        assertThat(updated.getPassword()).isEqualTo("1234561");
        assertThat(updated.getAboutMe()).isEqualTo("About me1");

        verify(peopleRepository, times(1)).findById(id);
        verify(peopleRepository, times(1)).save(existing);
    }
        
}