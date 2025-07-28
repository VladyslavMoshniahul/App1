package com.example.demo.javaSrc.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.CreateClassRequest;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolService;
import com.example.demo.javaSrc.users.User;

@RestController
@RequestMapping("/api/school")
public class SchoolController {
    @Autowired
    private final SchoolService schoolService;
    @Autowired
    private final ClassService classService;
    @Autowired
    private final UserController userController;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public SchoolController(SchoolService schoolService, ClassService classService, UserController userController,
            SimpMessagingTemplate messagingTemplate) {
        this.schoolService = schoolService;
        this.classService = classService;
        this.userController = userController;
        this.messagingTemplate = messagingTemplate;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/schools")
    public List<School> getAllSchools() {
        List<School> schools = schoolService.getAllSchools();
        messagingTemplate.convertAndSend("/topic/schools/list", schools);
        return schools;
    }

    @GetMapping("admin/classes")
    public List<SchoolClass> getClassesForAdmin(
            @RequestParam String schoolName) {
        School school = schoolService.getSchoolByName(schoolName);
        Long schoolId = school != null ? school.getId() : null;
        if (schoolId == null) {
            messagingTemplate.convertAndSend("/topic/admin/classes/error",
                    "Школу '" + schoolName + "' не знайдено для отримання класів.");
            return List.of();
        }
        List<SchoolClass> classes = classService.getBySchoolId(schoolId);
        messagingTemplate.convertAndSend("/topic/admin/classes/list", classes);
        return classes;
    }

    @GetMapping("/classes")
    public List<SchoolClass> getClasses(
            Authentication auth) {
        User me = userController.currentUser(auth);
        Long schoolId = me.getSchoolId();
        if (schoolId == null) {
            messagingTemplate.convertAndSend("/topic/classes/error", "Користувач не прив'язаний до школи.");
            return List.of();
        }
        List<SchoolClass> classes = classService.getBySchoolId(schoolId);
        messagingTemplate.convertAndSend("/topic/classes/list", classes);
        return classes;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<School> createNewSchool(@RequestBody School school) {
        School created = schoolService.createSchool(school);
        messagingTemplate.convertAndSend("/topic/schools/created", created);
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @PostMapping("/create-class")
    public ResponseEntity<SchoolClass> createNewClass(@RequestBody CreateClassRequest request) {
        String schoolName = request.schoolName();
        School school = schoolService.getSchoolByName(schoolName);
        if (school == null) {
            messagingTemplate.convertAndSend("/topic/classes/create/error",
                    "Не вдалося створити клас: школу '" + schoolName + "' не знайдено.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        SchoolClass schoolClass = new SchoolClass(school.getId(), request.className());
        SchoolClass created = classService.createClass(schoolClass);
        messagingTemplate.convertAndSend("/topic/classes/created", created);
        return ResponseEntity.ok(created);
    }

}
