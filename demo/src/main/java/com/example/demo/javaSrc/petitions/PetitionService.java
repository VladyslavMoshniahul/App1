package com.example.demo.javaSrc.petitions;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.peoples.PeopleRepository;

import jakarta.transaction.Transactional;

@Service
public class PetitionService {
    @Autowired
    private final PeopleRepository userRepository;
    @Autowired
    private final PetitionRepository petitionRepository;
    @Autowired
    private final PetitionVoteRepository petitionVoteRepository;

    public PetitionService(PetitionRepository petitionRepository, PeopleRepository userRepository,
            PetitionVoteRepository petitionVoteRepository) {
        this.petitionRepository = petitionRepository;
        this.userRepository = userRepository;
        this.petitionVoteRepository = petitionVoteRepository;
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

    public List<Petition> getAllPetitions() {
        return petitionRepository.findAll();
    }

    public void deletePetition(Long id) {
        petitionRepository.deleteById(id);
    }

    public List<Petition> getPetitionByStatus(Petition.Status status) {
        return petitionRepository.findByStatus(status);
    }

    public List<Petition> getPetitionByDirectorsDecisionAndSchoolId(Long schoolId, Petition.DirectorsDecision directorsDecision) {
        return petitionRepository.findByDirectorsDecisionAndSchoolId(schoolId, directorsDecision);
    }
    
    public List<Petition> getPetitionByDirectorsDecisionAndSchoolIdAndClassId(Long schoolId, Long classId, Petition.DirectorsDecision directorsDecision) {
        return petitionRepository.findByDirectorsDecisionAndSchoolIdAndClassId(schoolId,classId , directorsDecision);
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
                            People.Role.STUDENT,
                            petition.getSchoolId(),
                            petition.getClassId())
                    .size();
        } else {
            return userRepository
                    .findByRoleAndSchoolId(
                            People.Role.STUDENT,
                            petition.getSchoolId())
                    .size();
        }
    }

    @Transactional
    public void vote(Long petitionId, Long studentId, PetitionVote.VoteVariant vote) {
        Petition petition = petitionRepository.findById(petitionId)
                .orElseThrow(() -> new IllegalArgumentException("Petition not found"));

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime petitionEnd = petition.getEndDate();

        if (now.isAfter(petitionEnd)) {
            throw new IllegalStateException("Petition already ended");
        }

        boolean alreadyVoted = petitionVoteRepository.existsByPetitionIdAndStudentId(petitionId, studentId);
        if (alreadyVoted) {
            throw new IllegalStateException("Student already voted for this petition");
        }

        PetitionVote petitionVote = new PetitionVote();
        petitionVote.setPetition(petition);
        petitionVote.setStudentId(studentId);
        petitionVote.setVote(vote);
        petitionVote.setVotedAt(now);
        petitionVoteRepository.save(petitionVote);

        if (vote == PetitionVote.VoteVariant.YES) {
            int newCount = petition.getCurrentPositiveVoteCount() + 1;
            petition.setCurrentPositiveVoteCount(newCount);

            int totalStudents = getTotalStudentsForPetition(petition);

            if (newCount >= (totalStudents / 2) + 1) {
                petition.setDirectorsDecision(Petition.DirectorsDecision.PENDING);
            }

            petitionRepository.save(petition);
        }
    }

    @Transactional
    public Petition closePetition(Long petitionId) {
        return petitionRepository.findById(petitionId).map(petition -> {
            petition.setStatus(Petition.Status.CLOSED);
            return petitionRepository.save(petition);
        }).orElse(null);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredPetitions() {
        LocalDateTime now = LocalDateTime.now();
        List<Petition> openPetitions = petitionRepository.findByStatus(Petition.Status.OPEN);
        openPetitions.stream()
            .filter(petition -> petition.getEndDate().isBefore(now))
            .forEach(petition -> petition.setStatus(Petition.Status.CLOSED));
        petitionRepository.saveAll(openPetitions);
    }

}
