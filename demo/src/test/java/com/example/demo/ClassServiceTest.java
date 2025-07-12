package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.demo.javaSrc.school.*;


@SpringBootTest
public class ClassServiceTest {
    @MockBean
    private ClassRepository classRepository;

    @Autowired
    private ClassService classService;

    @BeforeEach
    void setUp() {
        classRepository.deleteAll();
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testGetBySchoolId() {
        Long schoolId = 1L;
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName("Math Class");
        schoolClass.setSchoolId(schoolId);

        when(classRepository.findBySchoolId(schoolId)).thenReturn(List.of(schoolClass));

        List<SchoolClass> result = classService.getBySchoolId(schoolId);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Math Class");
        assertThat(result.get(0).getSchoolId()).isEqualTo(schoolId);
    }

    @Test
    void testCreateClass() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName("Math Class");
        schoolClass.setSchoolId(1L); 

        when(classRepository.save(schoolClass)).thenReturn(schoolClass);

        SchoolClass created = classService.createClass(schoolClass);

        assertThat(created.getName()).isEqualTo("Math Class");
        assertThat(created.getSchoolId()).isEqualTo(1L);
        verify(classRepository, times(1)).save(schoolClass);
    }
}
