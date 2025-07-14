package com.example.demo.javaSrc.tasks;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findBySchoolId(Long schoolId);
    List<Task> findBySchoolIdAndClassId(Long schoolId, Long classId);
    List<Task> findBySchoolIdAndClassIdAndUserId(Long schoolId, Long classId, Long userId);
    List<Task> findByEventId(Long eventId);
}

