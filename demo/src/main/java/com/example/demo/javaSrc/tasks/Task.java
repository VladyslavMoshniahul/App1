package com.example.demo.javaSrc.tasks;


import java.util.Date;
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

    @Column(name = "event_id")
    private Long eventId;

    @Column(nullable = false)
    private String title;

    @Column
    private String content;

    @Column(nullable = false)
    private Date deadline;

    @Column(nullable = false)
    private boolean completed;

    public Task() {}

    public Task(Long schoolId, Long classId, Long eventId,
                String title, String content, Date deadline) {
        this.schoolId = schoolId;
        this.classId = classId;
        this.eventId = eventId;
        this.title = title;
        this.content = content;
        this.deadline = deadline;
        this.completed = false;
    }

    public Long getId() { return id; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getDeadline() { return (Date) deadline.clone(); }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    // Robust setter for JSON deserialization (accepts ISO string or Date)
    @com.fasterxml.jackson.annotation.JsonSetter("deadline")
    public void setDeadlineFromJson(Object deadline) {
        if (deadline instanceof String str) {
            try {
                // Accept both "2024-06-01T12:00:00.000Z" and "2024-06-01T12:00:00"
                java.time.Instant instant;
                if (str.endsWith("Z")) {
                    instant = java.time.Instant.parse(str);
                } else {
                    instant = java.time.LocalDateTime.parse(str.replace("Z", ""))
                        .atZone(java.time.ZoneId.systemDefault()).toInstant();
                }
                this.deadline = java.util.Date.from(instant);
            } catch (Exception e) {
                this.deadline = null;
            }
        } else if (deadline instanceof java.util.Date d) {
            this.deadline = d;
        }
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}