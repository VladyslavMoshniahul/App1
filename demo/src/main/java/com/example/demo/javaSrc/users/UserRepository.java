package com.example.demo.javaSrc.users;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findBySchoolId(Long schoolId);
    List<User> findBySchoolIdAndClassId(Long schoolId, Long classId);
    List<User> findByRoleAndSchoolId(User.Role role, Long schoolId);
    List<User> findByRoleAndSchoolIdAndClassId(User.Role role, Long schoolId, Long classId);
    String findEmailById(Long id);
}
