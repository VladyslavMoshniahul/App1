package com.example.demo.javaSrc.school;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchoolService {
    @Autowired
    private final SchoolRepository schoolRepository;

    public SchoolService(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    public School createSchool(School school) {
        return schoolRepository.save(school);
    }

    public School getSchoolById(long id) {
        return schoolRepository.findById(id);
    }

    public School getSchoolByName(String name) {
        return schoolRepository.findByNameIgnoreCase(name).orElse(null);
    }
}
