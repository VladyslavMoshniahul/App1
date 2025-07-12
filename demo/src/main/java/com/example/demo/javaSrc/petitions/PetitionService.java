package com.example.demo.javaSrc.petitions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.javaSrc.users.*;

import jakarta.transaction.Transactional;

@Service
public class PetitionService {

    private final UserRepository userRepository;
    @Autowired
    private final PetitionRepository petitionRepository;
    @Autowired
    private PetitionVoteRepository petitionVoteRepository;

    public PetitionService(PetitionRepository petitionRepository, UserRepository userRepository) {
        this.petitionRepository = petitionRepository;
        this.userRepository = userRepository;
    }

    public Petition createPetition(Petition petition) {
        return petitionRepository.save(petition);
    }

    public Petition getPetitionById(Long id) {
        return petitionRepository.findById(id).orElse(null);
    }

    public List<Petition> getPetitionByClassAndSchool(Long classId, Long schoolId) {
        return petitionRepository.findByClassIdAndSchoolId(classId, schoolId);
    }

    public List<Petition> getPetitionBySchool(Long schoolId) {
        return petitionRepository.findBySchoolId(schoolId);
    }

    public List<Petition> getPetitionByTitle(String title) {
        return petitionRepository.findByTitle(title);
    }

    public List<Petition> getPetitionByDescription(String description) {
        return petitionRepository.findByDescription(description);
    }

    public List<Petition> getPetitionByCreatedBy(Long createdBy) {
        return petitionRepository.findByCreatedBy(createdBy);
    }

    public List<Petition> getPetitionByStartDateBetween(Date startDate, Date endDate) {
        return petitionRepository.findByStartDateBetween(startDate, endDate);
    }

    public List<Petition> getAllPetition() {
        return petitionRepository.findAll();
    }

    public void deletePetition(Long id) {

        petitionRepository.deleteById(id);
    }

    public List<Petition> getPetitionByStatus(Petition.Status status) {
        return petitionRepository.findByStatus(status);
    }

    public List<Petition> getPetitionByDirectorsDecision(Petition.DirectorsDecision directorsDecision) {
        return petitionRepository.findByDirectorsDecision(directorsDecision);
    }

    public Petition updatePetition(Long id, Petition updatedPetition) {
        return petitionRepository.findById(id).map(existing -> {
            existing.setSchoolId(updatedPetition.getSchoolId());
            existing.setClassId(updatedPetition.getClassId());
            existing.setTitle(updatedPetition.getTitle());
            existing.setDescription(updatedPetition.getDescription());
            existing.setCreatedBy(updatedPetition.getCreatedBy());
            existing.setStartDate(updatedPetition.getStartDate());
            existing.setEndDate(updatedPetition.getEndDate());
            existing.setStatus(updatedPetition.getStatus());
            existing.setCurrentPositiveVoteCount(updatedPetition.getCurrentPositiveVoteCount());
            existing.setDirectorsDecision(updatedPetition.getDirectorsDecision());
            return petitionRepository.save(existing);
        }).orElse(null);
    }

    public int getTotalStudentsForPetition(Petition petition) {
        if (petition.getClassId() != null) {
            return userRepository
                    .findByRoleAndSchoolIdAndClassId(
                            User.Role.STUDENT,
                            petition.getSchoolId(),
                            petition.getClassId())
                    .size();
        } else {
            return userRepository
                    .findByRoleAndSchoolId(
                            User.Role.STUDENT,
                            petition.getSchoolId())
                    .size();
        }
    }

    @Transactional
    public void vote(Long petitionId, Long studentId, PetitionVote.VoteVariant vote) throws Exception {
        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new Exception("Petition not found"));

        if (LocalDateTime.now().isAfter(petition.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime())) {
            throw new Exception("Petition already ended");
        }

        boolean alreadyVoted = petitionVoteRepository.existsByPetitionIdAndStudentId(petitionId, studentId);
        if (alreadyVoted) {
            throw new Exception("Student already voted for this petition");
        }

        PetitionVote petitionVote = new PetitionVote();
        petitionVote.setPetition(petition);
        petitionVote.setStudentId(studentId);
        petitionVote.setVote(vote);
        petitionVote.setVotedAt(LocalDateTime.now());
        petitionVoteRepository.save(petitionVote);

        if (vote == PetitionVote.VoteVariant.YES) {
            int newCount = petition.getCurrentPositiveVoteCount() + 1;
            petition.setCurrentPositiveVoteCount(newCount);

            // Перевірка, чи досягнуто 50%+1 учасників
            int totalStudents = getTotalStudentsForPetition(petition);
            if (newCount >= (totalStudents / 2) + 1) {
                petition.setDirectorsDecision(Petition.DirectorsDecision.PENDING);
            }

            petitionRepository.save(petition);
        }
    }

}