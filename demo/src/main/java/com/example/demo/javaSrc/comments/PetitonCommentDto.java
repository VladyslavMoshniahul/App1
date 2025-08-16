package com.example.demo.javaSrc.comments;

import java.time.LocalDateTime;

import com.example.demo.javaSrc.peoples.People;

public class PetitonCommentDto {
    Long id;
    String text;
    String email;
    LocalDateTime createdAt;

    public PetitonCommentDto(PetitionsComment comment, People user) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.email = user != null ? user.getEmail() : null;
        this.createdAt = comment.getCreatedAt();
    }
}
