package com.example.demo.javaSrc.school;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByNameIgnoreCase(String name);
    School findById(long id);
    
}