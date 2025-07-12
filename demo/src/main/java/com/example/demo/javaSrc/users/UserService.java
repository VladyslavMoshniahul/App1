package com.example.demo.javaSrc.users;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public User createUser(User person) {
        return userRepository.save(person);
    }

    public List<User> getBySchoolAndClass(Long schoolId, Long classId) {
        if (classId == null) {
            return userRepository.findBySchoolId(schoolId);
        }
        return userRepository.findBySchoolIdAndClassId(schoolId, classId);
    }

    public List<User> getBySchoolClassAndRole(Long schoolId, Long classId, User.Role role) {
        return getBySchoolAndClass(schoolId, classId).stream()
                .filter(p -> p.getRole() == role)
                .collect(Collectors.toList());
    }

    public List<User> getUserByRole(String role) {
        return userRepository.findByRole(
            User.Role.valueOf(role.toUpperCase())
        );
    }
    
    public User findByEmail(String email) {
        Optional<User> maybe = userRepository.findByEmail(email);
        return maybe.orElse(null);
    }

    public User updateProfile(Long id, User updatedData) {
        return userRepository.findById(id).map(existing -> {
            existing.setFirstName(updatedData.getFirstName());
            existing.setLastName(updatedData.getLastName());
            existing.setAboutMe(updatedData.getAboutMe());
            existing.setDateOfBirth(updatedData.getDateOfBirth());
            // email, password, role — не оновлюються тут
            return userRepository.save(existing);
        }).orElse(null);
    }

    public User updateUser(Long id, User updatedData) {
        return userRepository.findById(id).map(existing -> {
            existing.setFirstName(updatedData.getFirstName());
            existing.setLastName(updatedData.getLastName());
            existing.setAboutMe(updatedData.getAboutMe());
            existing.setDateOfBirth(updatedData.getDateOfBirth());
            existing.setEmail(updatedData.getEmail());
            existing.setPassword(updatedData.getPassword());
            return userRepository.save(existing);
        }).orElse(null);
    }
}
