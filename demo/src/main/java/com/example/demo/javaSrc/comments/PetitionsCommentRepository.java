package com.example.demo.javaSrc.comments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface PetitionsCommentRepository extends JpaRepository<PetitionsComment, Long> {
    
    List<PetitionsComment> findByPetitionId(Long petitionId);
    
    List<PetitionsComment> findByUserId(Long userId);

    List<PetitionsComment> findByPetitionIdAndUserId(Long petitionId, Long userId);
    
    @Transactional
    void deleteByPetitionId(Long petitionId);
    
    @Transactional
    void deleteByUserId(Long userId);
}
