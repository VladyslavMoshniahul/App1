package com.example.demo.javaSrc.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolService;

@RestController
@RequestMapping("/api/school")
public class SchoolController {
    @Autowired
    private final SchoolService schoolService;
    @Autowired
    private final ClassService classService;

    public SchoolController(SchoolService schoolService, ClassService classService) {
        this.schoolService = schoolService;
        this.classService = classService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/schools")
    public List<School> getAllSchools() {
        return schoolService.getAllSchools();
    }

    @GetMapping("/classes")
    public List<SchoolClass> getClasses(
            @RequestParam Long schoolId) {
        if (schoolId == null) {
            return List.of();
        }
        return classService.getBySchoolId(schoolId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<School> createNewSchool(@RequestBody School school) {
        School created = schoolService.createSchool(school);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @PostMapping("/create-class")
    public ResponseEntity<SchoolClass> createNewClass(@RequestBody SchoolClass schoolClass) {
        SchoolClass created = classService.createClass(schoolClass);
        return ResponseEntity.ok(created);
    }
}
