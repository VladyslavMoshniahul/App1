package com.example.demo.javaSrc.peoples;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeopleRepository extends JpaRepository<People, Long> {
    Optional<People> findByEmail(String email);
    List<People> findByRole(People.Role role);
    List<People> findBySchoolId(Long schoolId);
    List<People> findBySchoolIdAndClassId(Long schoolId, Long classId);
    List<People> findByRoleAndSchoolId(People.Role role, Long schoolId);
    List<People> findByRoleAndSchoolIdAndClassId(People.Role role, Long schoolId, Long classId);
    String findEmailById(Long id);
    Long findCountBySchoolIdAndClassIdAndRole(Long schoolId, Long classId, People.Role role);
    Long findCountBySchoolIdAndRole(Long schoolId, People.Role role);

}
