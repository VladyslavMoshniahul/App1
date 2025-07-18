package com.example.demo.javaSrc.invitations;

import java.time.LocalDateTime;

public class InvitationDTO {
    private Long invitationId;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime eventDate;
    private String invitedBy; 
    private UserInvitationStatus.Status status;
    private LocalDateTime updatedAt;

    public InvitationDTO(
        Long invitationId,
        Long eventId,
        String eventTitle,
        LocalDateTime eventDate,
        String invitedBy,
        UserInvitationStatus.Status status,
        LocalDateTime updatedAt
    ) {
        this.invitationId = invitationId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
        this.invitedBy = invitedBy;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public UserInvitationStatus.Status getStatus() {
        return status;
    }

    public void setStatus(UserInvitationStatus.Status status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
