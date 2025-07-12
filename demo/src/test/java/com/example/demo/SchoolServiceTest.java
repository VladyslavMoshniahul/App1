package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.demo.javaSrc.school.*;

@SpringBootTest
public class SchoolServiceTest {
    @MockBean
    private SchoolRepository schoolRepository;

    @Autowired
    private SchoolService schoolService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllSchools() {
        School school1 = new School();
        school1.setName("Greenwood High");
        School school2 = new School();
        school2.setName("Riverdale Academy");

        when(schoolRepository.findAll()).thenReturn(List.of(school1, school2));

        List<School> result = schoolService.getAllSchools();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Greenwood High");
        assertThat(result.get(1).getName()).isEqualTo("Riverdale Academy");
    }

    @Test
    void testCreateSchool() {
        School school = new School();
        school.setName("Greenwood High");

        when(schoolRepository.save(school)).thenReturn(school);

        School created = schoolService.createSchool(school);

        assertThat(created.getName()).isEqualTo("Greenwood High");
        verify(schoolRepository, times(1)).save(school);
    }
}
