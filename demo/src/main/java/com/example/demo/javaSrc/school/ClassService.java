package com.example.demo.javaSrc.school;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClassService {
    @Autowired
    private final ClassRepository classRepository;

    public ClassService(ClassRepository classRepository) {
        this.classRepository = classRepository;
    }

    public List<SchoolClass> getBySchoolId(Long schoolId) {
        return classRepository.findBySchoolId(schoolId);
    }

    public SchoolClass getClassById(Long classId) {
        return classRepository.findById(classId).orElse(null);
    }

    public List<SchoolClass> getAllClasses() {
        return classRepository.findAll();
    }

    public List<SchoolClass> getClassesBySchoolIdAndName(Long schoolId, String name) {
        return classRepository.findBySchoolIdAndName(schoolId, name);
    }

    public SchoolClass createClass(SchoolClass schoolClass) {
        return classRepository.save(schoolClass);
    }
}
