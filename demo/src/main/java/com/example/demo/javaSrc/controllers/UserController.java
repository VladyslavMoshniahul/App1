package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Додано імпорт
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

import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.School;
import com.example.demo.javaSrc.school.SchoolClass;
import com.example.demo.javaSrc.school.SchoolService;
import com.example.demo.javaSrc.users.CreateUserRequest;
import com.example.demo.javaSrc.users.User;
import com.example.demo.javaSrc.users.UserProfileDto;
import com.example.demo.javaSrc.users.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private final UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final SchoolService schoolService;

    @Autowired
    private final ClassService classService;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate; // Додано SimpMessagingTemplate

    public UserController(UserService userService, PasswordEncoder passwordEncoder,
                          SchoolService schoolService, ClassService classService, SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.schoolService = schoolService;
        this.classService = classService;
        this.messagingTemplate = messagingTemplate; // Ініціалізація
    }

    public User currentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null; // або кинути виняток, залежно від логіки
        }
        return userService.findByEmail(auth.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admins")
    public List<User> getAdmins() {
        List<User> admins = userService.getUserByRole("ADMIN");
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/users/admins", admins);
        // --- End WebSocket Integration ---
        return admins;
    }

    @GetMapping("/teachers")
    public List<User> getTeachers(
            Authentication auth,
            @RequestParam(required = false) String className) {

        User me = currentUser(auth);
        if (me == null || me.getSchoolId() == null) {
            // messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Unauthorized access or no school associated.");
            return List.of();
        }

        Long schoolId = me.getSchoolId();
        SchoolClass schoolClass = null;
        if (className != null) {
            schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        }
        Long classId = schoolClass != null ? schoolClass.getId() : null;

        List<User> teachers;
        if (className == null) {
            teachers = userService.getBySchoolClassAndRole(schoolId, null, User.Role.TEACHER);
        } else {
            teachers = userService.getBySchoolClassAndRole(schoolId, classId, User.Role.TEACHER);
        }

        // --- WebSocket Integration (Optional for GET methods) ---
        // if (classId != null) {
        //     messagingTemplate.convertAndSend("/topic/school/" + schoolId + "/class/" + classId + "/teachers", teachers);
        // } else {
        //     messagingTemplate.convertAndSend("/topic/school/" + schoolId + "/teachers", teachers);
        // }
        // --- End WebSocket Integration ---
        return teachers;
    }

    @GetMapping("/students")
    public List<User> getStudents(
            Authentication auth,
            @RequestParam(required = false) String className) {

        User me = currentUser(auth);
        if (me == null || me.getSchoolId() == null) {
            // messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Unauthorized access or no school associated.");
            return List.of();
        }

        Long schoolId = me.getSchoolId();
        SchoolClass schoolClass = null;
        if (className != null) {
            schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        }
        Long classId = schoolClass != null ? schoolClass.getId() : null;

        List<User> students;
        if (className == null) {
            students = userService.getBySchoolClassAndRole(schoolId, null, User.Role.STUDENT);
        } else {
            students = userService.getBySchoolClassAndRole(schoolId, classId, User.Role.STUDENT);
        }

        // --- WebSocket Integration (Optional for GET methods) ---
        // if (classId != null) {
        //     messagingTemplate.convertAndSend("/topic/school/" + schoolId + "/class/" + classId + "/students", students);
        // } else {
        //     messagingTemplate.convertAndSend("/topic/school/" + schoolId + "/students", students);
        // }
        // --- End WebSocket Integration ---
        return students;
    }

    @GetMapping("/parents")
    public List<User> getParents(
            Authentication auth,
            @RequestParam(required = false) String className) {

        User me = currentUser(auth);
        if (me == null || me.getSchoolId() == null) {
            // messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Unauthorized access or no school associated.");
            return List.of();
        }

        Long schoolId = me.getSchoolId();
        SchoolClass schoolClass = null;
        if (className != null) {
            schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        }
        Long classId = schoolClass != null ? schoolClass.getId() : null;

        List<User> parents;
        if (className == null) {
            parents = userService.getBySchoolClassAndRole(schoolId, null, User.Role.PARENT);
        } else {
            parents = userService.getBySchoolClassAndRole(schoolId, classId, User.Role.PARENT);
        }

        // --- WebSocket Integration (Optional for GET methods) ---
        // if (classId != null) {
        //     messagingTemplate.convertAndSend("/topic/school/" + schoolId + "/class/" + classId + "/parents", parents);
        // } else {
        //     messagingTemplate.convertAndSend("/topic/school/" + schoolId + "/parents", parents);
        // }
        // --- End WebSocket Integration ---
        return parents;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/teachers")
    public List<User> getTeachersByAdmin(
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) String className) {
        School school = schoolService.getSchoolByName(schoolName);
        Long schoolId = school != null ? school.getId() : null;
        if (schoolId == null) {
            return List.of();
        }
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        Long classId = schoolClass != null ? schoolClass.getId() : null;

        List<User> teachers;
        if (classId == null) {
            teachers = userService.getBySchoolClassAndRole(schoolId, null, User.Role.TEACHER);
        } else {
            teachers = new ArrayList<>();
            teachers.addAll(userService.getBySchoolClassAndRole(schoolId, null, User.Role.TEACHER));
            teachers.addAll(userService.getBySchoolClassAndRole(schoolId, classId, User.Role.TEACHER));
        }
        // --- WebSocket Integration (Optional for GET methods) ---
        // if (classId != null) {
        //     messagingTemplate.convertAndSend("/topic/admin/school/" + schoolId + "/class/" + classId + "/teachers", teachers);
        // } else {
        //     messagingTemplate.convertAndSend("/topic/admin/school/" + schoolId + "/teachers", teachers);
        // }
        // --- End WebSocket Integration ---
        return teachers;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/directors")
    public List<User> getDirectorsByAdmin(
            @RequestParam(required = false) String schoolName) {
        School school = schoolService.getSchoolByName(schoolName);
        Long schoolId = school != null ? school.getId() : null;

        List<User> directors;
        if (schoolId == null) {
            return List.of();
        } else {
            directors = new ArrayList<>();
            directors.addAll(userService.getBySchoolClassAndRole(schoolId, null, User.Role.DIRECTOR));
        }
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/admin/school/" + schoolId + "/directors", directors);
        // --- End WebSocket Integration ---
        return directors;
    }

    @GetMapping("/users")
    public List<User> getAllUsers(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {

        List<User> all = userService.getAllUsers();

        if (schoolId != null) {
            all = all.stream()
                    .filter(p -> p.getSchoolId() != null && p.getSchoolId().equals(schoolId))
                    .toList();
        }

        if (classId != null) {
            all = all.stream()
                    .filter(p -> p.getClassId() != null && p.getClassId().equals(classId))
                    .toList();
        }

        if (name != null && !name.isBlank()) {
            all = all.stream()
                    .filter(p -> p.getFirstName() != null
                            && p.getFirstName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        if (email != null && !email.isBlank()) {
            all = all.stream()
                    .filter(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(email))
                    .toList();
        }

        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/users/all", all);
        // --- End WebSocket Integration ---
        return all;
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR', 'ADMIN')")
    @PostMapping("/create_users")
    public ResponseEntity<User> createUser(
            @RequestBody CreateUserRequest newUserRequest) {
        User newUser = new User();
        newUser.setFirstName(newUserRequest.firstName());
        newUser.setLastName(newUserRequest.lastName());
        newUser.setEmail(newUserRequest.email());
        newUser.setPassword(newUserRequest.password());
        newUser.setRole(newUserRequest.role());
        newUser.setDateOfBirth(newUserRequest.dateOfBirth());

        if (newUser.getRole() == User.Role.ADMIN) {
            newUser.setSchoolId(null);
            newUser.setClassId(null);
        } else {
            School school = schoolService.getSchoolByName(newUserRequest.schoolName());
            if (school == null) {
                // --- WebSocket Integration ---
                messagingTemplate.convertAndSend("/topic/userCreationErrors", "School not found: " + newUserRequest.schoolName());
                // --- End WebSocket Integration ---
                return ResponseEntity.badRequest().body(null);
            }
            newUser.setSchoolId(school.getId());

            SchoolClass schoolClass = null;
            if (newUserRequest.className() != null && !newUserRequest.className().isEmpty()) {
                schoolClass = classService.getClassesBySchoolIdAndName(school.getId(), newUserRequest.className());
                if (schoolClass == null && (newUser.getRole() == User.Role.PARENT || newUser.getRole() == User.Role.STUDENT)) {
                    // --- WebSocket Integration ---
                    messagingTemplate.convertAndSend("/topic/userCreationErrors", "Class not found: " + newUserRequest.className() + " in school " + newUserRequest.schoolName());
                    // --- End WebSocket Integration ---
                    return ResponseEntity.badRequest().body(null);
                }
            }
            newUser.setClassId(schoolClass != null ? schoolClass.getId() : null);

            if (newUser.getRole() == User.Role.DIRECTOR) {
                newUser.setClassId(null); // Директор не має класу
            }

            if (newUser.getRole() == User.Role.TEACHER) {
                // Вчитель може мати клас або ні
            }

            if ((newUser.getRole() == User.Role.PARENT || newUser.getRole() == User.Role.STUDENT)
                    && newUser.getClassId() == null) {
                // --- WebSocket Integration ---
                messagingTemplate.convertAndSend("/topic/userCreationErrors", "Students and Parents must be assigned to a class.");
                // --- End WebSocket Integration ---
                return ResponseEntity.badRequest().body(null);
            }
        }

        if (userService.findByEmail(newUser.getEmail()) != null) {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSend("/topic/userCreationErrors", "User with email " + newUser.getEmail() + " already exists.");
            // --- End WebSocket Integration ---
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        if (newUser.getRole() != User.Role.ADMIN && newUser.getSchoolId() == null) {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSend("/topic/userCreationErrors", "Non-admin users must be assigned to a school.");
            // --- End WebSocket Integration ---
            return ResponseEntity.badRequest().body(null);
        }

        String rawPass = newUser.getPassword();
        newUser.setPassword(passwordEncoder.encode(rawPass));

        User createdUser = userService.createUser(newUser);

        // --- WebSocket Integration ---
        // Сповіщаємо про створення нового користувача
        messagingTemplate.convertAndSend("/topic/users/new", createdUser);
        if (createdUser.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/school/" + createdUser.getSchoolId() + "/users/new", createdUser);
            if (createdUser.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/class/" + createdUser.getClassId() + "/users/new", createdUser);
            }
        }
        // --- End WebSocket Integration ---

        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/myProfile")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            // --- WebSocket Integration ---
            // messagingTemplate.convertAndSendToUser(email, "/queue/errors", "User not found for profile request.");
            // --- End WebSocket Integration ---
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

        UserProfileDto dto = new UserProfileDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getAboutMe(),
                user.getEmail(),
                user.getRole().toString(),
                schoolName,
                className);

        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/myProfile", dto);
        // --- End WebSocket Integration ---
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(@RequestBody User updatedData, Authentication auth) {
        String email = auth.getName();
        User currentUser = userService.findByEmail(email);
        if (currentUser == null) {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSendToUser(email, "/queue/errors", "User not found for update request.");
            // --- End WebSocket Integration ---
            return ResponseEntity.notFound().build();
        }

        if (updatedData.getEmail() != null && !updatedData.getEmail().equals(email)) {
            if (!isValidEmail(updatedData.getEmail())) {
                // --- WebSocket Integration ---
                messagingTemplate.convertAndSendToUser(currentUser.getId().toString(), "/queue/errors", "Invalid email format: " + updatedData.getEmail());
                // --- End WebSocket Integration ---
                return ResponseEntity.badRequest().body(null);
            }
            if (userService.findByEmail(updatedData.getEmail()) != null) {
                // --- WebSocket Integration ---
                messagingTemplate.convertAndSendToUser(currentUser.getId().toString(), "/queue/errors", "Email " + updatedData.getEmail() + " is already taken.");
                // --- End WebSocket Integration ---
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
        if (updatedData.getEmail() != null) { // Оновлюємо email
            currentUser.setEmail(updatedData.getEmail());
        }
        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }

        Long userId = currentUser.getId();
        User updated = userService.updateUser(userId, currentUser);

        // --- WebSocket Integration ---
        // Сповіщаємо про оновлення профілю користувача
        if (updated != null) {
            // Сповіщення особисто користувачу
            messagingTemplate.convertAndSendToUser(updated.getId().toString(), "/queue/profileUpdate", updated);
            // Сповіщення для адміністраторів або директорів, якщо це важливо
            messagingTemplate.convertAndSend("/topic/users/updated", updated);
            if (updated.getSchoolId() != null) {
                messagingTemplate.convertAndSend("/topic/school/" + updated.getSchoolId() + "/users/updated", updated);
            }
            if (updated.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/class/" + updated.getClassId() + "/users/updated", updated);
            }
        }
        // --- End WebSocket Integration ---

        return ResponseEntity.ok(updated);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR', 'ADMIN')")
    @GetMapping("/loadUsers")
    public List<User> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/users/all", users);
        // --- End WebSocket Integration ---
        return users;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/role/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        List<User> users = userService.getUserByRole(role);
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/users/role/" + role, users);
        // --- End WebSocket Integration ---
        return users;
    }

    @GetMapping("/users/role/school/{role}")
    public List<User> getUsersBySchoolClassAndRole(Authentication auth,
                                                 @RequestParam(required = false) String className,
                                                 @RequestParam(required = false) User.Role role) {
        User me = currentUser(auth);
        if (me == null || me.getSchoolId() == null) {
            // messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Unauthorized access or no school associated.");
            return List.of();
        }

        SchoolClass schoolclass = null;
        if (className != null) {
            schoolclass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);
        }
        
        Long classId = schoolclass != null ? schoolclass.getId() : null; // Отримуємо ID класу, якщо знайдено

        List<User> users = userService.getBySchoolClassAndRole(me.getSchoolId(), classId, role);
        // --- WebSocket Integration (Optional for GET methods) ---
        // if (classId != null) {
        //     messagingTemplate.convertAndSend("/topic/school/" + me.getSchoolId() + "/class/" + classId + "/users/role/" + role.name(), users);
        // } else {
        //     messagingTemplate.convertAndSend("/topic/school/" + me.getSchoolId() + "/users/role/" + role.name(), users);
        // }
        // --- End WebSocket Integration ---
        return users;
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUserByTeacherOrDirector(@PathVariable Long id, @RequestBody User updatedData) {
        User updated = userService.updateProfile(id, updatedData);
        if (updated != null) {
            // --- WebSocket Integration ---
            // Сповіщаємо про оновлення профілю користувача
            messagingTemplate.convertAndSendToUser(updated.getId().toString(), "/queue/profileUpdate", updated); // Для самого оновленого користувача
            messagingTemplate.convertAndSend("/topic/users/updated", updated); // Загальний топік
            if (updated.getSchoolId() != null) {
                messagingTemplate.convertAndSend("/topic/school/" + updated.getSchoolId() + "/users/updated", updated);
            }
            if (updated.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/class/" + updated.getClassId() + "/users/updated", updated);
            }
            // --- End WebSocket Integration ---
            return ResponseEntity.ok(updated);
        } else {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSend("/topic/userUpdateErrors", "User with ID " + id + " not found for update.");
            // --- End WebSocket Integration ---
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            // --- WebSocket Integration (Optional for GET methods) ---
            // messagingTemplate.convertAndSend("/topic/users/" + id, user);
            // --- End WebSocket Integration ---
            return ResponseEntity.ok(user);
        } else {
            // --- WebSocket Integration ---
            // messagingTemplate.convertAndSend("/topic/userNotFound", "User with ID " + id + " not found.");
            // --- End WebSocket Integration ---
            return ResponseEntity.notFound().build();
        }
    }

}