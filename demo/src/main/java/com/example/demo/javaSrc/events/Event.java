package com.example.demo.javaSrc.events;


import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

import com.example.demo.javaSrc.tasks.Task;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "class_id")
    private Long classId;

    @Column(nullable = false)
    private String title;

    @Column
    private String content;

    @Column(name = "location_or_link")
    private String locationOrLink;

    @Column(nullable = false)
    private int duration;

    @JsonProperty("start_event")
    @Column(name = "start_event", nullable = false)
    private LocalDateTime startEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Task> tasks; 


    public Event() {}

    public Event(Long schoolId, Long classId, String title, LocalDateTime startEvent,
                 String content, String locationOrLink, int duration,
                 EventType eventType, Long createdBy) {
        this.schoolId = schoolId;
        this.classId = classId;
        this.title = title;
        this.startEvent = startEvent;
        this.content = content;
        this.locationOrLink = locationOrLink;
        this.duration = duration;
        this.eventType = eventType;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getLocationOrLink() { return locationOrLink; }
    public void setLocationOrLink(String locationOrLink) { this.locationOrLink = locationOrLink; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public LocalDateTime getStartEvent() { return startEvent; }
    public void setStartEvent(LocalDateTime startEvent) { this.startEvent = startEvent; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    // Allow setting eventType from string for JSON deserialization
    @JsonSetter("event_type")
    public void setEventTypeFromString(Object eventType) {
        if (eventType instanceof String str) {
            this.eventType = EventType.valueOf(str.toUpperCase());
        } else if (eventType instanceof EventType et) {
            this.eventType = et;
        }
    }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public enum EventType { EXAM, TEST, SCHOOL_EVENT, PARENTS_MEETING, PERSONAL }
}
