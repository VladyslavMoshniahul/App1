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

import com.example.demo.javaSrc.people.*;


@SpringBootTest
public class PeopleServiceTest {
    
    @MockBean
    private PeopleRepository peopleRepository;

    @Autowired
    private PeopleService peopleService;

    @BeforeEach
    void setUp() {
         MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllPeople() {
          People person1 = new People();
        person1.setFirstName("John");
        person1.setEmail("testemail@gmail.com");
        person1.setPassword("password123");
        person1.setRole(People.Role.STUDENT);
        person1.setSchoolId(1L);
        person1.setClassId(1L);

        People person2 = new People();
        person2.setFirstName("Jane");
        person2.setEmail("testemail1@gmail.com");
        person2.setPassword("password123");
        person2.setRole(People.Role.TEACHER);
        person2.setSchoolId(1L);
        person2.setClassId(2L);

        when(peopleRepository.findAll()).thenReturn(List.of(person1, person2));

        List<People> result = peopleService.getAllPeople();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(1).getFirstName()).isEqualTo("Jane");
    }

    @Test
    void testCreatePeople() {
         People person = new People();
        String email = "testemail@gmail.com";
        person.setEmail(email);
        person.setPassword("password123");
        person.setRole(People.Role.STUDENT);
        person.setSchoolId(1L);
        person.setClassId(1L);
        person.setFirstName("John");
        person.setLastName("Doe");

        when(peopleRepository.save(person)).thenReturn(person);

        People created = peopleService.createPeople(person);

        assertThat(created.getFirstName()).isEqualTo("John");
        assertThat(created.getLastName()).isEqualTo("Doe");
        verify(peopleRepository, times(1)).save(person);
    }

    @Test
    void testGetBySchoolAndClass() {
        Long schoolId = 1L;
        Long classId = 2L;
        People person1 = new People();
        person1.setSchoolId(schoolId);
        person1.setClassId(classId);

        when(peopleRepository.findBySchoolIdAndClassId(schoolId, classId)).thenReturn(List.of(person1));

        List<People> result = peopleService.getBySchoolAndClass(schoolId, classId);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getSchoolId()).isEqualTo(schoolId);
        assertThat(result.get(0).getClassId()).isEqualTo(classId);
    }

    @Test
    void testGetBySchoolClassAndRole() {
        Long schoolId = 1L;
        Long classId = 2L;
        People person1 = new People();
        person1.setSchoolId(schoolId);
        person1.setClassId(classId);
        person1.setRole(People.Role.STUDENT);

        when(peopleRepository.findBySchoolIdAndClassId(schoolId, classId)).thenReturn(List.of(person1));

        List<People> result = peopleService.getBySchoolClassAndRole(schoolId, classId, People.Role.STUDENT);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRole()).isEqualTo(People.Role.STUDENT);
    }

    @Test
    void testGetPeopleByRole() {
        People person1 = new People();
        person1.setRole(People.Role.STUDENT);
        People person2 = new People();
        person2.setRole(People.Role.TEACHER);

        when(peopleRepository.findByRole(People.Role.STUDENT)).thenReturn(List.of(person1));

        List<People> result = peopleService.getPeopleByRole("STUDENT");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRole()).isEqualTo(People.Role.STUDENT);
    }

    @Test
    void testFindByEmail() {
        People person = new People();
        String email = "ggg@ggg.com";
        person.setEmail(email);

        when(peopleRepository.findByEmail(email)).thenReturn(java.util.Optional.of(person));
        People found = peopleService.findByEmail(email);
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo(email);
    }

    @Test
    void testUpdateProfile() {
        People existingPerson = new People();
        Long id = existingPerson.getId();
        existingPerson.setFirstName("Old Name");

        People updatedData = new People();
        updatedData.setFirstName("New Name");

        when(peopleRepository.findById(id)).thenReturn(java.util.Optional.of(existingPerson));
        when(peopleRepository.save(existingPerson)).thenReturn(existingPerson);

        People result = peopleService.updateProfile(id, updatedData);

        assertThat(result.getFirstName()).isEqualTo("New Name");
        verify(peopleRepository, times(1)).save(existingPerson);
    }

    @Test
    void testUpdateUser() {  
        People existing = new People();
        Long id = existing.getId();
        existing.setFirstName("OldName");
        existing.setLastName("OldLast");
        existing.setEmail("old@email.com");
        existing.setPassword("oldpass");
        existing.setAboutMe("Old about me");

        People updatedData = new People();
        updatedData.setFirstName("John1");
        updatedData.setLastName("Doe1");
        updatedData.setEmail("hh1@h.com");
        updatedData.setPassword("1234561");
        updatedData.setAboutMe("About me1");

        when(peopleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(peopleRepository.save(any(People.class))).thenAnswer(invocation -> invocation.getArgument(0));

        People updated = peopleService.updateUser(id, updatedData);

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