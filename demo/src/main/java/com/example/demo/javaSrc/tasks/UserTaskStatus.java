package com.example.demo.javaSrc.tasks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_task_status")
public class UserTaskStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "completed_at")
    private java.sql.Timestamp completedAt;

    public UserTaskStatus() {}

    public UserTaskStatus(Long userId, Long taskId, Boolean isCompleted) {
        this.userId = userId;
        this.taskId = taskId;
        this.isCompleted = isCompleted;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTaskId() {
        return taskId;
    }
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Boolean isCompleted() {
        return isCompleted;
    }
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public java.sql.Timestamp getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(java.sql.Timestamp completedAt) {
        this.completedAt = completedAt;
    }
}
