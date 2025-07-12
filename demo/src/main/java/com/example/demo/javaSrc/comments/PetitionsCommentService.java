package com.example.demo.javaSrc.comments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PetitionsCommentService {
    @Autowired
    private PetitionsCommentRepository petitionsCommentRepository;
    public PetitionsCommentService(PetitionsCommentRepository petitionsCommentRepository) {
        this.petitionsCommentRepository = petitionsCommentRepository;
    }

}
