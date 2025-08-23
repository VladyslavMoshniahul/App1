package com.example.demo.javaSrc.tasks;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name="content")
    private String content;

    @Column(name = "deadline",nullable = false)
    private LocalDateTime deadline;

    public Task() {}

    public Task(Long schoolId, Long classId, 
                String title, String content, LocalDateTime deadline) {
        this.schoolId = schoolId;
        this.classId = classId;
        this.title = title;
        this.content = content;
        this.deadline = deadline;
    }

    public Long getId() { return id; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    @com.fasterxml.jackson.annotation.JsonSetter("deadline")
    public void setDeadlineFromJson(Object deadline) {
        if (deadline instanceof String str) {
            try {
                java.time.Instant instant;
                if (str.endsWith("Z")) {
                    instant = java.time.Instant.parse(str);
                } else {
                    instant = java.time.LocalDateTime.parse(str.replace("Z", ""))
                        .atZone(java.time.ZoneId.systemDefault()).toInstant();
                }
                this.deadline = java.time.LocalDateTime.from(instant);
            } catch (Exception e) {
                this.deadline = null;
            }
        } else if (deadline instanceof java.time.LocalDateTime d) {
            this.deadline = d;
        }
    }

}