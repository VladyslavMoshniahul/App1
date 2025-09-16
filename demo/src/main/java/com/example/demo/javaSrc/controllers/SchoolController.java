package com.example.demo.javaSrc.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.CreateClassRequest;
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
    @Autowired
    private final PeopleController userController;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public SchoolController(SchoolService schoolService,
            ClassService classService,
            PeopleController userController,
            SimpMessagingTemplate messagingTemplate) {
        this.schoolService = schoolService;
        this.classService = classService;
        this.userController = userController;
        this.messagingTemplate = messagingTemplate;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/schools")
    public ResponseEntity<List<School>> getAllSchools() {
        List<School> schools = schoolService.getAllSchools();
        messagingTemplate.convertAndSend("/topic/school/schools", schools);
        return ResponseEntity.ok(schools);
    }

    @GetMapping("/admin/classes")
    public ResponseEntity<List<SchoolClass>> getClassesForAdmin(@RequestParam String schoolName) {
        School school = schoolService.getSchoolByName(schoolName);
        if (school == null) {
            return ResponseEntity.notFound().build();
        }
        List<SchoolClass> classes = classService.getBySchoolId(school.getId());
        messagingTemplate.convertAndSend("/topic/school/admin/classes", classes);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/classes")
    public ResponseEntity<List<SchoolClass>> getClasses(Authentication auth) {
        People me = userController.currentUser(auth);
        Long schoolId = me.getSchoolId();
        if (schoolId == null) {
            return ResponseEntity.notFound().build();
        }
        List<SchoolClass> classes = classService.getBySchoolId(schoolId);
        messagingTemplate.convertAndSend("/topic/school/classes", classes);
        return ResponseEntity.ok(classes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<School> createNewSchool(@RequestBody School school) {
        School created = schoolService.createSchool(school);
        messagingTemplate.convertAndSend("/topic/school/create", created);

        URI location = URI.create("/api/school/schools/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @PostMapping("/create-class")
    public ResponseEntity<SchoolClass> createNewClass(@RequestBody CreateClassRequest request) {
        School school = schoolService.getSchoolByName(request.schoolName());
        if (school == null) {
            return ResponseEntity.notFound().build();
        }

        SchoolClass schoolClass = new SchoolClass(school.getId(), request.className());
        SchoolClass created = classService.createClass(schoolClass);
        messagingTemplate.convertAndSend("/topic/school/create-class", created);

        URI location = URI.create("/api/school/classes/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }
}
