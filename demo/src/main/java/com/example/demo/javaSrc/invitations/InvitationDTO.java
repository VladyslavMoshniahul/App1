package com.example.demo.javaSrc.invitations;

import java.time.LocalDateTime;

public class InvitationDTO {

    public enum Type{EVENT, VOTE};

    private Long invitationId;
    private Long eventOrVoteId;
    private String eventOrVoteTitle;
    private LocalDateTime eventOrVoteDate;
    private String invitedBy; 
    private UserInvitationStatus.Status status;
    private LocalDateTime updatedAt;
    private Type type;

    public InvitationDTO(
        Long invitationId,
        Long eventOrVoteId,
        String eventOrVoteTitle,
        LocalDateTime eventOrVoteDate,
        String invitedBy,
        UserInvitationStatus.Status status,
        LocalDateTime updatedAt,
        Type type
    ) {
        this.invitationId = invitationId;
        this.eventOrVoteId = eventOrVoteId;
        this.eventOrVoteTitle = eventOrVoteTitle;
        this.eventOrVoteDate = eventOrVoteDate;
        this.invitedBy = invitedBy;
        this.status = status;
        this.updatedAt = updatedAt;
        this.type = type;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }

    public Long getEventOrVoteId() {
        return eventOrVoteId;
    }

    public void setEventOrVoteId(Long eventOrVoteId) {
        this.eventOrVoteId = eventOrVoteId;
    }

    public String getEventTitle() {
        return eventOrVoteTitle;
    }

    public void setEventOrVoteTitle(String eventOrVoteTitle) {
        this.eventOrVoteTitle = eventOrVoteTitle;
    }

    public LocalDateTime getventOrVoteDate() {
        return eventOrVoteDate;
    }

    public void setEventOrVoteDate(LocalDateTime eventOrVoteDate) {
        this.eventOrVoteDate = eventOrVoteDate;
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

    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
}
