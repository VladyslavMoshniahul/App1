package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.example.demo.javaSrc.users.User;
import com.example.demo.javaSrc.users.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private final UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;

    }

    public User currentUser(Authentication auth) {
        return userService.findByEmail(auth.getName());
    }

    @GetMapping("/teachers")
    public List<User> getTeachers(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String name) {

        User me = currentUser(auth);
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();

        List<User> teachers;
        if (classId == null) {
            teachers = userService.getBySchoolClassAndRole(sch, null, User.Role.TEACHER);
        } else {
            teachers = new ArrayList<>();
            teachers.addAll(userService.getBySchoolClassAndRole(sch, null, User.Role.TEACHER));
            teachers.addAll(userService.getBySchoolClassAndRole(sch, cls, User.Role.TEACHER));
        }

        if (name != null && !name.isBlank()) {
            teachers.removeIf(p -> !p.getFirstName().toLowerCase().contains(name.toLowerCase()) &&
                    !p.getLastName().toLowerCase().contains(name.toLowerCase()));
        }
        return teachers;
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
                    .filter(p -> p.getFirstName() != null && p.getFirstName().toLowerCase().contains(name.toLowerCase()))
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
            @RequestBody User newUser,
            Authentication auth) {

        if (newUser.getSchoolId() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String rawPass = newUser.getPassword();
        newUser.setPassword(passwordEncoder.encode(rawPass));

        return ResponseEntity.ok(userService.createUser(newUser));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(user);
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

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @GetMapping("/loadUsers")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR')")
    @GetMapping("/users/role/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userService.getUserByRole(role);
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
