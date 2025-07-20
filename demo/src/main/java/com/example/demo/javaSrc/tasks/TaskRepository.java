package com.example.demo.javaSrc.tasks;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findBySchoolId(Long schoolId);
    List<Task> findBySchoolIdAndClassId(Long schoolId, Long classId);

    List<Task> findByEventId(Long eventId);
    List<Task> findBySchoolIdAndClassIdAndTitleContainingIgnoreCase(Long schoolId, Long classId, String keyword);
    List<Task> findBySchoolIdAndClassIdAndContentContainingIgnoreCase(Long schoolId, Long classId, String keyword);

    List<Task> findBySchoolIdAndTitleContainingIgnoreCase(Long schoolId, String keyword);
    List<Task> findBySchoolIdAndContentContainingIgnoreCase(Long schoolId, String keyword);

}

