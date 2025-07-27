package com.example.demo.javaSrc.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Додано імпорт
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
    private final SimpMessagingTemplate messagingTemplate; // Додано SimpMessagingTemplate

    public SchoolController(SchoolService schoolService, ClassService classService, UserController userController, SimpMessagingTemplate messagingTemplate) {
        this.schoolService = schoolService;
        this.classService = classService;
        this.userController = userController;
        this.messagingTemplate = messagingTemplate; // Ініціалізація
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/schools")
    public List<School> getAllSchools() {
        List<School> schools = schoolService.getAllSchools();
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/schools/all", schools);
        // --- End WebSocket Integration ---
        return schools;
    }

    @GetMapping("admin/classes")
    public List<SchoolClass> getClassesForAdmin(
            @RequestParam String schoolName) {
        School school = schoolService.getSchoolByName(schoolName);
        Long schoolId = school != null ? school.getId() : null;
        if (schoolId == null) {
            // --- WebSocket Integration ---
            // messagingTemplate.convertAndSend("/topic/admin/classes/notFound", "School " + schoolName + " not found.");
            // --- End WebSocket Integration ---
            return List.of();
        }
        List<SchoolClass> classes = classService.getBySchoolId(schoolId);
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/school/" + schoolId + "/classes/admin", classes);
        // --- End WebSocket Integration ---
        return classes;

    }

    @GetMapping("/classes")
    public List<SchoolClass> getClasses(
            Authentication auth) {
        User me = userController.currentUser(auth);
        Long schoolId = me.getSchoolId();
        if (schoolId == null) {
            // --- WebSocket Integration ---
            // messagingTemplate.convertAndSendToUser(me.getId().toString(), "/queue/errors", "User is not associated with any school.");
            // --- End WebSocket Integration ---
            return List.of();
        }
        List<SchoolClass> classes = classService.getBySchoolId(schoolId);
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/school/" + schoolId + "/classes", classes);
        // --- End WebSocket Integration ---
        return classes;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<School> createNewSchool(@RequestBody School school) {
        School created = schoolService.createSchool(school);
        // --- WebSocket Integration ---
        // Сповіщаємо про створення нової школи
        messagingTemplate.convertAndSend("/topic/schools/new", created);
        // --- End WebSocket Integration ---
        return ResponseEntity.ok(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @PostMapping("/create-class")
    public ResponseEntity<SchoolClass> createNewClass(@RequestBody CreateClassRequest request) {
        String schoolName = request.schoolName();
        School school = schoolService.getSchoolByName(schoolName);
        if (school == null) {
            // --- WebSocket Integration ---
            // Сповіщення про помилку, якщо школа не знайдена
            // messagingTemplate.convertAndSend("/topic/school/classCreationError", "School " + schoolName + " not found for class creation.");
            // --- End WebSocket Integration ---
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        SchoolClass schoolClass = new SchoolClass(school.getId(), request.className());
        SchoolClass created = classService.createClass(schoolClass);
        // --- WebSocket Integration ---
        // Сповіщаємо про створення нового класу
        messagingTemplate.convertAndSend("/topic/school/" + school.getId() + "/classes/new", created);
        // Також можна сповістити всіх адміністраторів або директорів
        // messagingTemplate.convertAndSend("/topic/admin/newClass", created);
        // --- End WebSocket Integration ---
        return ResponseEntity.ok(created);
    }

    @GetMapping("/getClassIdByName")
    public ResponseEntity<Long> getClassIdByName(@RequestParam String name, Authentication auth) {
        User me = userController.currentUser(auth);
        if (me == null || me.getSchoolId() == null) {
            // --- WebSocket Integration ---
            // messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "User not found or not assigned to a school.");
            // --- End WebSocket Integration ---
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long schoolId = me.getSchoolId();
        SchoolClass sc = classService.getClassesBySchoolIdAndName(schoolId, name);
        if (sc == null) {
            // --- WebSocket Integration ---
            // messagingTemplate.convertAndSendToUser(me.getId().toString(), "/queue/errors", "Class with name " + name + " not found in your school.");
            // --- End WebSocket Integration ---
            return ResponseEntity.notFound().build();
        }
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSendToUser(me.getId().toString(), "/queue/classId", sc.getId());
        // --- End WebSocket Integration ---
        return ResponseEntity.ok(sc.getId());
    }
}