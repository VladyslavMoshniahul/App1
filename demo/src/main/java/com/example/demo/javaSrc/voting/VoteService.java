package com.example.demo.javaSrc.voting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.javaSrc.invitations.InvitationDTO;
import com.example.demo.javaSrc.invitations.InvitationsService;
import com.example.demo.javaSrc.peoples.*;

@Service
public class VoteService {

    private final InvitationsService invitationsService;
    @Autowired
    private final VoteRepository voteRepository;
    @Autowired
    private final VotingVariantRepository votingVariantRepository;
    @Autowired
    private final VotingVoteRepository votingVoteRepository;
    @Autowired
    private final PeopleRepository userRepository;

    public VoteService(VoteRepository voteRepository,
            VotingVariantRepository votingVariantRepository,
            VotingVoteRepository votingVoteRepository,
            PeopleRepository userRepository, InvitationsService invitationsService) {
        this.voteRepository = voteRepository;
        this.votingVariantRepository = votingVariantRepository;
        this.votingVoteRepository = votingVoteRepository;
        this.userRepository = userRepository;
        this.invitationsService = invitationsService;
    }

    @Transactional
    public Vote createVoting(Vote vote, List<String> variantStrings) {
        Vote savedVote = voteRepository.save(vote);

        List<VotingVariant> savedVariants = new ArrayList<>();
        if (variantStrings != null) {
            for (String text : variantStrings) {
                VotingVariant variant = new VotingVariant();
                variant.setVote(savedVote);
                variant.setText(text);
                savedVariants.add(votingVariantRepository.save(variant));
            }
        }
        savedVote.setVariants(savedVariants);

        return savedVote;
    }

    public Vote getVotingById(Long id) {
        return voteRepository.findById(id).orElse(null);
    }

    public List<Vote> getVotingsByClassAndSchool(Long classId, Long schoolId) {
        return voteRepository.findByClassIdAndSchoolId(classId, schoolId);
    }

    public List<Vote> getVotingsBySchool(Long schoolId) {
        return voteRepository.findBySchoolId(schoolId);
    }

    public List<Vote> getVotingsByTitle(String title) {
        return voteRepository.findByTitle(title);
    }

    public List<Vote> getVotingsByDescription(String description) {
        return voteRepository.findByDescription(description);
    }

    public List<Vote> getVotingsByCreatedBy(Long createdBy) {
        return voteRepository.findByCreatedBy(createdBy);
    }

    public List<Vote> getVotingsByStartDateBetween(Date startDate, Date endDate) {
        return voteRepository.findByStartDateBetween(startDate, endDate);
    }

    public List<Vote> getAllVotings() {
        return voteRepository.findAll();
    }

    public void deleteVoting(Long id) {
        voteRepository.deleteById(id);
    }

    @Transactional
    public Vote updateVoting(Long id, Vote updatedVote, List<String> updatedVariantTexts) {
        return voteRepository.findById(id).map(existing -> {
            existing.setSchoolId(updatedVote.getSchoolId());
            existing.setClassId(updatedVote.getClassId());
            existing.setTitle(updatedVote.getTitle());
            existing.setDescription(updatedVote.getDescription());
            existing.setCreatedBy(updatedVote.getCreatedBy());
            existing.setStartDate(updatedVote.getStartDate());
            existing.setEndDate(updatedVote.getEndDate());
            existing.setMultipleChoice(updatedVote.isMultipleChoice());
            existing.setVotingLevel(updatedVote.getVotingLevel());

            if (updatedVariantTexts != null) {
                votingVariantRepository.findByVoteId(id).forEach(votingVariantRepository::delete);
                for (String text : updatedVariantTexts) {
                    votingVariantRepository.save(new VotingVariant(existing, text));
                }
            }

            return voteRepository.save(existing);
        }).orElse(null);
    }

    public List<Vote> getAccessibleVotingsForUser(Long userId, Long schoolId, Long classId) {
        List<Vote> allVotings = voteRepository.findByStatus(Vote.VoteStatus.OPEN);
        return allVotings.stream()
                .filter(vote -> {
                    switch (vote.getVotingLevel()) {
                        case SCHOOL:
                            return vote.getSchoolId().equals(schoolId);
                        case ACLASS:
                            return vote.getClassId() != null && vote.getClassId().equals(classId)
                                    && vote.getSchoolId().equals(schoolId);
                        case TEACHERS_GROUP:
                            People user = userRepository.findById(userId).orElseThrow();
                            if (user.getRole() == People.Role.TEACHER) {
                                return vote.getSchoolId().equals(schoolId);
                            } else {
                                return false;
                            }
                        case SELECTED_USERS:
                            return invitationsService.getInvitationsForUser(userId).stream()
                                .anyMatch(inv -> inv.getType() == InvitationDTO.Type.VOTE
                                        && inv.getEventOrVoteId().equals(vote.getId()));
                        default:
                            return false;
                    }
                })
                .toList();
    }

    @Transactional
    public Vote closeVoting(Long voteId) {
        return voteRepository.findById(voteId).map(vote -> {
            vote.setStatus(Vote.VoteStatus.CLOSED);
            return voteRepository.save(vote);
        }).orElse(null);
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void closeExpiredVotings() {
        LocalDateTime now = LocalDateTime.now();
        List<Vote> openVotings = voteRepository.findByStatus(Vote.VoteStatus.OPEN);

        List<Vote> expired = openVotings.stream()
                .filter(vote -> vote.getEndDate().isBefore(now))
                .peek(vote -> vote.setStatus(Vote.VoteStatus.CLOSED))
                .toList();

        voteRepository.saveAll(expired);
    }

    @Transactional
    public boolean recordVote(Long votingId, List<Long> variantIds, Long userId) {
        Optional<Vote> voteOptional = voteRepository.findById(votingId);
        if (voteOptional.isEmpty()) {
            return false;
        }
        Vote vote = voteOptional.get();

        if (vote.getStatus() == Vote.VoteStatus.CLOSED || vote.getEndDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (!vote.isMultipleChoice() && variantIds.size() > 1) {
            return false;
        }

        for (Long variantId : variantIds) {
            if (!vote.isMultipleChoice()) {
                long existingVotesCount = votingVariantRepository.findByVoteId(votingId).stream()
                        .flatMap(variant -> votingVoteRepository.findByVotingIdAndUserId(votingId, userId).stream())
                        .count();
                if (existingVotesCount > 0) {
                    return false;
                }
            } else {
                Optional<VotingVote> existingVote = votingVoteRepository.findByVotingIdAndUserIdAndVariantId(votingId,
                        userId, variantId);
                if (existingVote.isPresent()) {
                    continue;
                }
            }

            VotingVote newVote = new VotingVote(vote, votingVariantRepository.findById(variantId)
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found")), userId);
            votingVoteRepository.save(newVote);
        }
        return true;
    }

    public VotingResults getVotingResults(Long votingId) {
        Optional<Vote> voteOptional = voteRepository.findById(votingId);
        if (voteOptional.isEmpty()) {
            return null;
        }
        Vote vote = voteOptional.get();

        List<VotingVariant> variants = votingVariantRepository.findByVoteId(votingId);
        VotingResults results = new VotingResults(vote.getTitle(), vote.getDescription(), vote.isMultipleChoice());

        for (VotingVariant variant : variants) {
            long voteCount = votingVoteRepository.countByVotingIdAndVariantId(votingId, variant.getId());
            results.addVariantResult(variant.getText(), voteCount);
        }
        return results;
    }

}