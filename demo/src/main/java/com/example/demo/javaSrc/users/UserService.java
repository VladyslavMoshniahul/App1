package com.example.demo.javaSrc.users;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User person) {
        if (person.getRole() != User.Role.ADMIN && person.getSchoolId() == null) {
            throw new IllegalArgumentException("School ID is required unless role is ADMIN.");
        }
        return userRepository.save(person);
    }

    public List<User> getBySchoolAndClass(Long schoolId, Long classId) {
        if (schoolId == null) return Collections.emptyList();
        if (classId == null) {
            return userRepository.findBySchoolId(schoolId);
        }
        return userRepository.findBySchoolIdAndClassId(schoolId, classId);
    }

    public List<User> getBySchoolClassAndRole(Long schoolId, Long classId, User.Role role) {
        return getBySchoolAndClass(schoolId, classId).stream()
                .filter(p -> role.equals(p.getRole()))
                .collect(Collectors.toList());
    }

    public List<User> getUserByRole(String role) {
        return userRepository.findByRole(User.Role.valueOf(role.toUpperCase()));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User updateProfile(Long id, User updatedData) {
        return userRepository.findById(id).map(existing -> {
            copyCommonFields(updatedData, existing, false);
            return userRepository.save(existing);
        }).orElse(null);
    }

    public User updateUser(Long id, User updatedData) {
        return userRepository.findById(id).map(existing -> {
            copyCommonFields(updatedData, existing, true);
            return userRepository.save(existing);
        }).orElse(null);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<User> getUsersByRoles(User.Role... roles) {
        List<User.Role> roleList = Arrays.asList(roles);
        return userRepository.findAll().stream()
                .filter(u -> roleList.contains(u.getRole()))
                .collect(Collectors.toList());
    }

    private void copyCommonFields(User source, User target, boolean includeSensitive) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAboutMe(source.getAboutMe());
        target.setDateOfBirth(source.getDateOfBirth());
        if (includeSensitive) {
            target.setEmail(source.getEmail());
            target.setPassword(source.getPassword());
        }
    }
}
