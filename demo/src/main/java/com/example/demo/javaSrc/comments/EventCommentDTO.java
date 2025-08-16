package com.example.demo.javaSrc.comments;

import java.time.LocalDateTime;

import com.example.demo.javaSrc.peoples.People;

public class EventCommentDTO {
    Long id;
    String text;
    String email;
    LocalDateTime createdAt;

    public EventCommentDTO(EventsComment comment, People user) {
        this.id = comment.getId();
        this.text = comment.getComment();
        this.email = user != null ? user.getEmail() : null;
        this.createdAt = comment.getCreatedAt();
    }
}
