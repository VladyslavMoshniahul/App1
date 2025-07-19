package com.example.demo.javaSrc.invitations;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;


import jakarta.persistence.Column;

@Entity
@Table(name = "user_invitation_status")
public class UserInvitationStatus {
    public enum Status {
        ACCEPTED,
        DECLINED,
        PENDING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "invitation_id", nullable = false)
    private Long invitationId;

    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserInvitationStatus() {
    }

    public UserInvitationStatus(Long userId, Long invitationId, Status status) {
        this.userId = userId;
        this.invitationId = invitationId;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public Long getInvitationId() {
        return invitationId;
    }
    public Status getStatus() {
        return status;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
