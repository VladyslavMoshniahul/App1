package com.example.demo.javaSrc.school;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClassService {
      private final ClassRepository classRepository;

    @Autowired
    public ClassService(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public List<SchoolClass> getBySchoolId(Long schoolId) {
        return classRepository.findBySchoolId(schoolId);
    }

    public SchoolClass createClass(SchoolClass schoolClass) {
        return classRepository.save(schoolClass);
    }
}
