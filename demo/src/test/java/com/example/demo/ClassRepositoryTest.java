package com.example.demo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.school.*;

@SpringBootTest
public class ClassRepositoryTest {
    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Test
    void testFindBySchoolId(){
        schoolRepository.deleteAll();
        classRepository.deleteAll();

        School school = new School();
        school.setName("Test School");
        school = schoolRepository.save(school);  


        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName("Math Class");
        schoolClass.setSchoolId(school.getId());  
        classRepository.save(schoolClass);
        
        List<SchoolClass> classes = classRepository.findBySchoolId(school.getId());

        assertThat(classes).isNotEmpty();
        assertThat(classes.get(0).getName()).isEqualTo("Math Class");
        assertThat(classes.get(0).getSchoolId()).isEqualTo(school.getId());
        
    }
}
