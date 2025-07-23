package com.example.demo.javaSrc.school;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findBySchoolId(Long schoolId);
    SchoolClass findBySchoolIdAndName(Long teacherId,String name);
}