package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public UserController(UserService userService, PasswordEncoder passwordEncoder,
            SchoolService schoolService, ClassService classService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.schoolService = schoolService;
        this.classService = classService;
    }

    public User currentUser(Authentication auth) {
        return userService.findByEmail(auth.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admins")
    public List<User> getAdmins() {
        List<User> admins = userService.getUserByRole("ADMIN");
        return admins;
    }

    @GetMapping("/teachers")
    public List<User> getTeachers(
            Authentication auth,
            @RequestParam(required = false) Long classId) {

        User me = currentUser(auth);
        Long sch = me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();

        List<User> teachers;
        if (classId == null) {
            teachers = userService.getBySchoolClassAndRole(sch, null, User.Role.TEACHER);
        } else {
            teachers = userService.getBySchoolClassAndRole(sch, cls, User.Role.TEACHER);
        }

        return teachers;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/teachers")
    public List<User> getTeachersByAdmin(
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) String className) {
        School school = schoolService.getSchoolByName(schoolName);
        Long schoolId = school != null ? school.getId() : null;
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
        return teachers;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/directors")
    public List<User> getDirectorsByAdmin(
            @RequestParam(required = false) String schoolName) {
        School school = schoolService.getSchoolByName(schoolName);
        Long sch = school != null ? school.getId() : null;

        List<User> directors;
        if (sch == null) {
            return List.of();
        } else {
            directors = new ArrayList<>();
            directors.addAll(userService.getBySchoolClassAndRole(sch, null, User.Role.DIRECTOR));
        }
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
            SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(school.getId(),
                    newUserRequest.className());
            if (newUser.getRole() == User.Role.DIRECTOR) {
                newUser.setSchoolId(school.getId());
                newUser.setClassId(null);
            }

            if (newUser.getRole() == User.Role.TEACHER) {
                newUser.setSchoolId(school.getId());
                if (schoolClass == null) {
                    newUser.setClassId(null);
                } else {
                    newUser.setClassId(schoolClass.getId());
                }
            }

            if (newUser.getRole() == User.Role.PARENT || newUser.getRole() == User.Role.STUDENT) {
                if (school == null || schoolClass == null) {
                    return ResponseEntity.badRequest().body(null);
                }
                newUser.setSchoolId(school.getId());
                newUser.setClassId(schoolClass.getId());
            }
        }

        if (userService.findByEmail(newUser.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        if (newUser.getRole() != User.Role.ADMIN && newUser.getSchoolId() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        if ((newUser.getRole() == User.Role.PARENT || newUser.getRole() == User.Role.STUDENT)
                && (newUser.getClassId() == null || newUser.getSchoolId() == null)) {
            return ResponseEntity.badRequest().body(null);
        }

        String rawPass = newUser.getPassword();
        newUser.setPassword(passwordEncoder.encode(rawPass));

        return ResponseEntity.ok(userService.createUser(newUser));
    }

    @GetMapping("/myProfile")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
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

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(@RequestBody User updatedData, Authentication auth) {
        String email = auth.getName();
        User currentUser = userService.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (updatedData.getEmail() != null && !updatedData.getEmail().equals(email)) {
            if (!isValidEmail(updatedData.getEmail())) {
                return ResponseEntity.badRequest().body(null);
            }
            if (userService.findByEmail(updatedData.getEmail()) != null) {
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
        User updated = userService.updateUser(userId, currentUser);
        return ResponseEntity.ok(updated);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR', 'ADMIN')")
    @GetMapping("/loadUsers")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/role/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userService.getUserByRole(role);
    }

    @GetMapping("/users/role/school/{role}")
    public List<User> getUsersBySchoolClassAndRole(Authentication auth,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) User.Role role) {
        User me = currentUser(auth);
        SchoolClass schoolclass = classService.getClassesBySchoolIdAndName(me.getSchoolId(), className);
        return userService.getBySchoolClassAndRole(me.getSchoolId(), schoolclass.getId(), role);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUserByTeacherOrDirector(@PathVariable Long id, @RequestBody User updatedData) {
        User updated = userService.updateProfile(id, updatedData);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

}
