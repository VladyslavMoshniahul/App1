package com.example.demo.javaSrc.tasks;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface UserTaskStatusRepository extends JpaRepository<UserTaskStatus,Long> {
    Optional<UserTaskStatus> findByUserIdAndTaskId(Long userId, Long taskId);
    List<UserTaskStatus> findByUserId(Long userId);
    List<UserTaskStatus> findByTaskId(Long taskId);
    List<UserTaskStatus> findByTaskIdAndIsCompleted(Long taskId,Boolean isCompleted);
    List<UserTaskStatus> findByUserIdAndIsCompleted(Long userId,  Boolean isCompleted);
}
