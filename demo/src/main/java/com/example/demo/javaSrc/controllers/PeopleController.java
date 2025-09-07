package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
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

    public PeopleController(PeopleService peopleService, PasswordEncoder passwordEncoder,
            SchoolService schoolService, ClassService classService, SimpMessagingTemplate messagingTemplate) {
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
    public List<People> getAdmins() {
        List<People> admins = peopleService.getPeopleByRole("ADMIN");
        messagingTemplate.convertAndSend("/topic/users/admins/list", admins);
        return admins;
    }

    @GetMapping("/teachers")
    public List<People> getTeachers(
            Authentication auth,
            @RequestParam(required = false) String className) {
        People me = currentUser(auth);
        Long schoolId = me.getSchoolId();
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        List<People> teachers;
        if (schoolClass == null) {
            teachers = peopleService.getPeopleBySchoolAndRole(schoolId, People.Role.TEACHER);
            messagingTemplate.convertAndSend("/topic/users/teachers/list", teachers);
        } else {
            teachers = peopleService.getBySchoolClassAndRole(schoolId, schoolClass.getId(), People.Role.TEACHER);
            messagingTemplate.convertAndSend("/topic/users/teachers/list/class/" + schoolClass.getId(), teachers);
        }
        return teachers;
    }

    @GetMapping("/students")
    public List<People> getStudents(
            Authentication auth,
            @RequestParam(required = false) String className) {
        People me = currentUser(auth);
        Long schoolId = me.getSchoolId();
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        Long classId = schoolClass != null ? schoolClass.getId() : null;
        List<People> students;
        if (className == null) {
            students = peopleService.getBySchoolClassAndRole(schoolId, null, People.Role.STUDENT);
            messagingTemplate.convertAndSend("/topic/users/students/list", students);
        } else {
            students = peopleService.getBySchoolClassAndRole(schoolId, classId, People.Role.STUDENT);
            messagingTemplate.convertAndSend("/topic/users/students/list/class/" + classId, students);
        }
        return students;
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
                .map(p -> new PeopleDto(p.getId(), p.getFirstName(), p.getLastName(), p.getEmail(), p.getRole().name()))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/parents")
    public List<People> getParents(
            Authentication auth,
            @RequestParam(required = false) String className) {

        People me = currentUser(auth);
        Long schoolId = me.getSchoolId();
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        Long classId = schoolClass != null ? schoolClass.getId() : null;

        List<People> parents;
        if (className == null) {
            parents = peopleService.getBySchoolClassAndRole(schoolId, null, People.Role.PARENT);
            messagingTemplate.convertAndSend("/topic/users/parents/list", parents);
        } else {
            parents = peopleService.getBySchoolClassAndRole(schoolId, classId, People.Role.PARENT);
            messagingTemplate.convertAndSend("/topic/users/parents/list/class/" + classId, parents);
        }

        return parents;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/teachers")
    public List<People> getTeachersByAdmin(
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) String className) {
        School school = schoolService.getSchoolByName(schoolName);
        Long schoolId = school != null ? school.getId() : null;
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        Long classId = schoolClass != null ? schoolClass.getId() : null;

        List<People> teachers;
        if (classId == null) {
            teachers = peopleService.getBySchoolClassAndRole(schoolId, null, People.Role.TEACHER);
            messagingTemplate.convertAndSend("/topic/admin/teachers/list/school/" + schoolId, teachers);
        } else {
            teachers = new ArrayList<>();
            teachers.addAll(peopleService.getBySchoolClassAndRole(schoolId, classId, People.Role.TEACHER));
            messagingTemplate.convertAndSend("/topic/admin/teachers/list/school/" + schoolId + "/class/" + classId,
                    teachers);
        }
        return teachers;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/directors")
    public List<People> getDirectorsByAdmin(
            @RequestParam(required = false) String schoolName) {
        School school = schoolService.getSchoolByName(schoolName);
        Long schoolId = school != null ? school.getId() : null;

        List<People> directors;
        if (schoolId == null) {
            messagingTemplate.convertAndSend("/topic/admin/directors/error",
                    "Школу '" + schoolName + "' не знайдено для отримання директорів.");
            return List.of();
        } else {
            directors = new ArrayList<>();
            directors.addAll(peopleService.getBySchoolClassAndRole(schoolId, null, People.Role.DIRECTOR));
            messagingTemplate.convertAndSend("/topic/admin/directors/list/school/" + schoolId, directors);
        }
        return directors;
    }
    
    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR', 'ADMIN')")
    @PostMapping("/create_users")
    public ResponseEntity<People> createUser(
            @RequestBody CreatePeopleRequest newUserRequest) {
        People newUser = new People();
        newUser.setFirstName(newUserRequest.firstName());
        newUser.setLastName(newUserRequest.lastName());
        newUser.setEmail(newUserRequest.email());
        newUser.setPassword(newUserRequest.password());
        newUser.setRole(newUserRequest.role());
        newUser.setDateOfBirth(newUserRequest.dateOfBirth());

        if (newUser.getRole() == People.Role.ADMIN) {
            newUser.setSchoolId(null);
            newUser.setClassId(null);
        } else {

            School school = schoolService.getSchoolByName(newUserRequest.schoolName());
            SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(school.getId(),
                    newUserRequest.className());
            if (newUser.getRole() == People.Role.DIRECTOR) {
                newUser.setSchoolId(school.getId());
                newUser.setClassId(null);
            }

            if (newUser.getRole() == People.Role.TEACHER) {
                newUser.setSchoolId(school.getId());
                if (schoolClass == null) {
                    newUser.setClassId(null);
                } else {
                    newUser.setClassId(schoolClass.getId());
                }
            }

            if (newUser.getRole() == People.Role.PARENT || newUser.getRole() == People.Role.STUDENT) {
                if (school == null || schoolClass == null) {
                    return ResponseEntity.badRequest().body(null);
                }
                newUser.setSchoolId(school.getId());
                newUser.setClassId(schoolClass.getId());
            }
        }

        if (peopleService.findByEmail(newUser.getEmail()) != null) {
            messagingTemplate.convertAndSend("/topic/users/create/error",
                    "Помилка створення користувача: користувач з таким email вже існує.");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        if (newUser.getRole() != People.Role.ADMIN && newUser.getSchoolId() == null) {
            messagingTemplate.convertAndSend("/topic/users/create/error",
                    "Помилка створення користувача: для ролі '" + newUser.getRole() + "' обов'язково вказати школу.");

            return ResponseEntity.badRequest().body(null);
        }
        if ((newUser.getRole() == People.Role.PARENT || newUser.getRole() == People.Role.STUDENT)
                && (newUser.getClassId() == null || newUser.getSchoolId() == null)) {

            messagingTemplate.convertAndSend("/topic/users/create/error",
                    "Помилка створення користувача: для учня/батька обов'язково вказати школу та клас.");
            return ResponseEntity.badRequest().body(null);
        }

        String rawPass = newUser.getPassword();
        newUser.setPassword(passwordEncoder.encode(rawPass));
        messagingTemplate.convertAndSend("/topic/users/created", newUser);

        return ResponseEntity.ok(peopleService.createPeople(newUser));
    }

    @GetMapping("/myProfile")
    public ResponseEntity<PeopleProfileDto> getMyProfile(Authentication auth) {
        String email = auth.getName();
        People user = peopleService.findByEmail(email);
        if (user == null) {
            messagingTemplate.convertAndSend("/topic/users/profile/error",
                    "Профіль для користувача '" + email + "' не знайдено.");

            return ResponseEntity.status(401).build();
        }

        String schoolName = null;
        String className = null;

        if (user.getSchoolId() != null) {
            School school = schoolService.getSchoolById(user.getSchoolId());
            if (school != null) {
                schoolName = school.getName();
            }
        }

        if (user.getClassId() != null) {
            SchoolClass schoolClass = classService.getBySchoolIdAndId(user.getSchoolId(), user.getClassId());
            if (schoolClass != null) {
                className = schoolClass.getName();
            }
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
        messagingTemplate.convertAndSend("/topic/users/profile", dto);

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<People> updateMyProfile(@RequestBody People updatedData, Authentication auth) {
        String email = auth.getName();
        People currentUser = peopleService.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (updatedData.getEmail() != null && !updatedData.getEmail().equals(email)) {
            if (!isValidEmail(updatedData.getEmail())) {
                messagingTemplate.convertAndSend("/topic/users/update/error",
                        "Не вдалося оновити профіль: недійсний формат email '" + updatedData.getEmail() + "'.");
                return ResponseEntity.badRequest().body(null);
            }
            if (peopleService.findByEmail(updatedData.getEmail()) != null) {
                messagingTemplate.convertAndSend("/topic/users/update/error",
                        "Не вдалося оновити профіль: email '" + updatedData.getEmail() + "' вже зайнятий.");
                return ResponseEntity.badRequest().body(null);
            }
        }

        if (updatedData.getFirstName() != null) {
            currentUser.setFirstName(updatedData.getFirstName());
        }
        if (updatedData.getLastName() != null) {
            currentUser.setLastName(updatedData.getLastName());
        }
        if (updatedData.getAboutMe() != null) {
            currentUser.setAboutMe(updatedData.getAboutMe());
        }
        if (updatedData.getDateOfBirth() != null) {
            currentUser.setDateOfBirth(updatedData.getDateOfBirth());
        }
        if (updatedData.getEmail() != null) {
            currentUser.setEmail(updatedData.getEmail());
        }
        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }

        Long userId = currentUser.getId();
        People updated = peopleService.updatePeople(userId, currentUser);
        messagingTemplate.convertAndSend("/topic/users/updated/id/" + userId, updated);
        return ResponseEntity.ok(updated);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR', 'ADMIN')")
    @GetMapping("/loadUsers")
    public List<People> getAllUsers() {
        messagingTemplate.convertAndSend("/topic/users/all/list", peopleService.getAllPeoples());
        return peopleService.getAllPeoples();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/role/{role}")
    public List<People> getUsersByRole(@PathVariable String role) {
        messagingTemplate.convertAndSend("/topic/users/list/role/" + role, peopleService.getPeopleByRole(role));
        return peopleService.getPeopleByRole(role);
    }

    @GetMapping("/users/role/school/{role}")
    public List<People> getUsersBySchoolClassAndRole(Authentication auth,
            @RequestParam(required = false) String className,
            @PathVariable People.Role role) {
        People me = currentUser(auth);
        SchoolClass schoolclass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);
        if (schoolclass == null) {
            schoolclass = new SchoolClass();
            schoolclass.setId(null);
        }
        String topic = "/topic/users/list/school/" + me.getSchoolId() + "/class/"
                + (schoolclass.getId() != null ? schoolclass.getId() : "null") + "/role/" + role;
        messagingTemplate.convertAndSend(topic,
                peopleService.getBySchoolClassAndRole(me.getSchoolId(), schoolclass.getId(), role));
        return peopleService.getBySchoolClassAndRole(me.getSchoolId(), schoolclass.getId(), role);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @PutMapping("/updateUsers/{id}")
    public ResponseEntity<People> updateUserByTeacherOrDirector(@PathVariable Long id,
            @RequestBody People updatedData) {
        People updated = peopleService.updateProfile(id, updatedData);
        if (updated != null) {
            messagingTemplate.convertAndSend("/topic/users/updated/id/" + id, updated);
            return ResponseEntity.ok(updated);
        } else {
            messagingTemplate.convertAndSend("/topic/users/update/error/id/" + id,
                    "Не вдалося оновити користувача з ID " + id + ".");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<People> getUserById(@PathVariable Long id) {
        People user = peopleService.getPeopleById(id);
        if (user != null) {
            messagingTemplate.convertAndSend("/topic/users/details/id/" + id, user);
            return ResponseEntity.ok(user);
        } else {
            messagingTemplate.convertAndSend("/topic/users/details/error/id/" + id,
                    "Користувача з ID " + id + " не знайдено.");
            return ResponseEntity.notFound().build();
        }
    }
}