package com.example.demo.javaSrc.comments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetitionsCommentService {
    @Autowired
    private PetitionsCommentRepository petitionsCommentRepository;
    public PetitionsCommentService(PetitionsCommentRepository petitionsCommentRepository) {
        this.petitionsCommentRepository = petitionsCommentRepository;
    }
    public PetitionsComment addComment(PetitionsComment comment) {
        return petitionsCommentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        petitionsCommentRepository.deleteById(id);
    }

    public PetitionsComment getComment(Long id) {
        return petitionsCommentRepository.findById(id).orElse(null);
    }
    public List<PetitionsComment> getAllComments() {
        return petitionsCommentRepository.findAll();
    }
    public List<PetitionsComment> getCommentsByPetitionIdAndUserId(Long petitionId, Long userId) {
        return petitionsCommentRepository.findByPetitionIdAndUserId(petitionId, userId);
    }
    public List<PetitionsComment> getCommentsByPetitionId(Long petitionId) {
        return petitionsCommentRepository.findByPetitionId(petitionId);
    }
    public List<PetitionsComment> getCommentsByUserId(Long userId) {
        return petitionsCommentRepository.findByUserId(userId);
    }
    public void deleteCommentsByPetitionId(Long petitionId) {
        petitionsCommentRepository.deleteByPetitionId(petitionId);
    }
    public void deleteCommentsByUserId(Long userId) {
        petitionsCommentRepository.deleteByUserId(userId);
    }
}
