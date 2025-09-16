package com.example.demo.javaSrc.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.peoples.CreatePeopleRequest;
import com.example.demo.javaSrc.peoples.PeopleService;
import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.peoples.PeopleDto;
import com.example.demo.javaSrc.peoples.PeopleProfileDto;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolService;

import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/user")
public class PeopleController {

    @Autowired
    private final PeopleService peopleService;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final SchoolService schoolService;
    @Autowired
    private final ClassService classService;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public PeopleController(PeopleService peopleService,
            PasswordEncoder passwordEncoder,
            SchoolService schoolService,
            ClassService classService,
            SimpMessagingTemplate messagingTemplate) {
        this.peopleService = peopleService;
        this.passwordEncoder = passwordEncoder;
        this.schoolService = schoolService;
        this.classService = classService;
        this.messagingTemplate = messagingTemplate;
    }

    public People currentUser(Authentication auth) {
        return peopleService.findByEmail(auth.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admins")
    public ResponseEntity<List<People>> getAdmins() {
        List<People> admins = peopleService.getPeopleByRole("ADMIN");
        messagingTemplate.convertAndSend("/topic/user/admins", admins);
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<People>> getTeachers(Authentication auth) {
        People me = currentUser(auth);
        List<People> teachers = peopleService.getPeopleBySchoolAndRole(me.getSchoolId(), People.Role.TEACHER);
        messagingTemplate.convertAndSend("/topic/user/teachers/school", teachers);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/students")
    public ResponseEntity<List<People>> getStudents(Authentication auth,
            @RequestParam(required = false) String className) {
        People me = currentUser(auth);
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);
        Long classId = (schoolClass != null ? schoolClass.getId() : null);

        List<People> students;
        if (className == null) {
            students = peopleService.getBySchoolClassAndRole(me.getSchoolId(), null, People.Role.STUDENT);
            messagingTemplate.convertAndSend("/topic/user/students/school", students);
        } else {
            students = peopleService.getBySchoolClassAndRole(me.getSchoolId(), classId, People.Role.STUDENT);
            messagingTemplate.convertAndSend("/topic/user/students/class/" + classId, students);
        }
        return ResponseEntity.ok(students);
    }

    @GetMapping("/people")
    public ResponseEntity<List<PeopleDto>> getPeople(Authentication auth,
            @RequestParam(required = false) String role) {
        People me = currentUser(auth);
        Long schoolId = me.getSchoolId();

        List<People> people;
        if (role == null || role.equalsIgnoreCase("ALL")) {
            people = peopleService.getPeopleBySchool(schoolId);
        } else {
            People.Role enumRole = People.Role.valueOf(role.toUpperCase());
            people = peopleService.getPeopleBySchoolAndRole(schoolId, enumRole);
        }

        List<PeopleDto> result = people.stream()
                .map(p -> new PeopleDto(
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getEmail(),
                        p.getRole().name()))
                .toList();

        messagingTemplate.convertAndSend("/topic/user/people", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/parents")
    public ResponseEntity<List<People>> getParents(Authentication auth,
            @RequestParam(required = false) String className) {
        People me = currentUser(auth);
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);
        Long classId = (schoolClass != null ? schoolClass.getId() : null);

        List<People> parents;
        if (className == null) {
            parents = peopleService.getBySchoolClassAndRole(me.getSchoolId(), null, People.Role.PARENT);
            messagingTemplate.convertAndSend("/topic/user/parents/school", parents);
        } else {
            parents = peopleService.getBySchoolClassAndRole(me.getSchoolId(), classId, People.Role.PARENT);
            messagingTemplate.convertAndSend("/topic/user/parents/class/" + classId, parents);
        }

        return ResponseEntity.ok(parents);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/teachers")
    public ResponseEntity<List<People>> getTeachersByAdmin(@RequestParam String schoolName) {
        School school = schoolService.getSchoolByName(schoolName);
        if (school == null) {
            return ResponseEntity.notFound().build();
        }
        List<People> teachers = peopleService.getBySchoolClassAndRole(school.getId(), null, People.Role.TEACHER);
        messagingTemplate.convertAndSend("/topic/user/admin/teachers/" + school.getId(), teachers);
        return ResponseEntity.ok(teachers);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/directors")
    public ResponseEntity<List<People>> getDirectorsByAdmin(@RequestParam String schoolName) {
        School school = schoolService.getSchoolByName(schoolName);
        if (school == null) {
            return ResponseEntity.notFound().build();
        }
        List<People> directors = peopleService.getBySchoolClassAndRole(school.getId(), null, People.Role.DIRECTOR);
        messagingTemplate.convertAndSend("/topic/user/admin/directors/" + school.getId(), directors);
        return ResponseEntity.ok(directors);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR', 'ADMIN')")
    @PostMapping("/create_users")
    public ResponseEntity<People> createUser(@RequestBody CreatePeopleRequest req) {
        People newUser = new People();
        newUser.setFirstName(req.firstName());
        newUser.setLastName(req.lastName());
        newUser.setEmail(req.email());
        newUser.setPassword(req.password());
        newUser.setRole(req.role());
        newUser.setDateOfBirth(req.dateOfBirth());

        if (newUser.getRole() == People.Role.ADMIN) {
            newUser.setSchoolId(null);
            newUser.setClassId(null);
        } else {
            School school = schoolService.getSchoolByName(req.schoolName());
            if (school == null) {
                return ResponseEntity.badRequest().build();
            }
            SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(school.getId(), req.className());

            switch (newUser.getRole()) {
                case DIRECTOR -> {
                    newUser.setSchoolId(school.getId());
                    newUser.setClassId(null);
                }
                case TEACHER -> {
                    newUser.setSchoolId(school.getId());
                    newUser.setClassId(schoolClass != null ? schoolClass.getId() : null);
                }
                case PARENT, STUDENT -> {
                    if (schoolClass == null)
                        return ResponseEntity.badRequest().build();
                    newUser.setSchoolId(school.getId());
                    newUser.setClassId(schoolClass.getId());
                }
                default -> {
                }
            }
        }

        if (peopleService.findByEmail(newUser.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        People created = peopleService.createPeople(newUser);

        messagingTemplate.convertAndSend("/topic/user/create_user", created);

        URI location = URI.create("/api/user/users/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/myProfile")
    public ResponseEntity<PeopleProfileDto> getMyProfile(Authentication auth) {
        People user = peopleService.findByEmail(auth.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String schoolName = null;
        String className = null;

        if (user.getSchoolId() != null) {
            School school = schoolService.getSchoolById(user.getSchoolId());
            if (school != null)
                schoolName = school.getName();
        }

        if (user.getClassId() != null) {
            SchoolClass schoolClass = classService.getBySchoolIdAndId(user.getSchoolId(), user.getClassId());
            if (schoolClass != null)
                className = schoolClass.getName();
        }

        PeopleProfileDto dto = new PeopleProfileDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getAboutMe(),
                user.getEmail(),
                user.getRole().toString(),
                schoolName,
                className);

        messagingTemplate.convertAndSend("/topic/user/myProfile", dto);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<People> updateMyProfile(@RequestBody People updatedData, Authentication auth) {
        People currentUser = peopleService.findByEmail(auth.getName());
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (updatedData.getEmail() != null && !updatedData.getEmail().equals(currentUser.getEmail())) {
            if (!isValidEmail(updatedData.getEmail())) {
                return ResponseEntity.badRequest().build();
            }
            if (peopleService.findByEmail(updatedData.getEmail()) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        if (updatedData.getFirstName() != null)
            currentUser.setFirstName(updatedData.getFirstName());
        if (updatedData.getLastName() != null)
            currentUser.setLastName(updatedData.getLastName());
        if (updatedData.getAboutMe() != null)
            currentUser.setAboutMe(updatedData.getAboutMe());
        if (updatedData.getDateOfBirth() != null)
            currentUser.setDateOfBirth(updatedData.getDateOfBirth());
        if (updatedData.getEmail() != null)
            currentUser.setEmail(updatedData.getEmail());
        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }

        People updated = peopleService.updatePeople(currentUser.getId(), currentUser);
        messagingTemplate.convertAndSend("/topic/user/me/" + currentUser.getId(), updated);
        return ResponseEntity.ok(updated);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<People>> getUsersByRole(@PathVariable String role) {
        List<People> users = peopleService.getPeopleByRole(role);
        messagingTemplate.convertAndSend("/topic/user/users/admin/role/" + role, users);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/role/school/{role}")
    public ResponseEntity<List<People>> getUsersBySchoolClassAndRole(Authentication auth,
            @RequestParam(required = false) String className,
            @PathVariable People.Role role) {
        People me = currentUser(auth);
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);
        Long classId = (schoolClass != null ? schoolClass.getId() : null);

        List<People> result = peopleService.getBySchoolClassAndRole(me.getSchoolId(), classId, role);
        messagingTemplate.convertAndSend("/topic/user/getUsersBySchoolClassAndRole", result);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @PutMapping("/updateUsers/{id}")
    public ResponseEntity<People> updateUserByTeacherOrDirector(@PathVariable Long id,
            @RequestBody People updatedData) {
        People updated = peopleService.updateProfile(id, updatedData);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        messagingTemplate.convertAndSend("/topic/user/updatedUsers/" + id, updated);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<People> getUserById(@PathVariable Long id) {
        People user = peopleService.getPeopleById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        messagingTemplate.convertAndSend("/topic/user/users/" + id, user);
        return ResponseEntity.ok(user);
    }
}
