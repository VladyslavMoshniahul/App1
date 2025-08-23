package com.example.demo.javaSrc.invitations;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public Invitation() {
    }

    public Invitation(Long id, Long eventId, Long voteId, Long userId, LocalDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.voteId = voteId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public Long getVoteId() { return voteId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    @PreUpdate
    private void validate() {
        if ((eventId == null && voteId == null) || (eventId != null && voteId != null)) {
            throw new IllegalStateException("Invitation must have either eventId OR voteId, but not both");
        }
    }
}
