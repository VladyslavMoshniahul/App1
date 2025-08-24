package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.invitations.InvitationDTO;
import com.example.demo.javaSrc.invitations.InvitationsService;
import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.voting.Vote;
import com.example.demo.javaSrc.voting.VoteService;
import com.example.demo.javaSrc.voting.VotingParticipant;
import com.example.demo.javaSrc.voting.VotingResults;
import com.example.demo.javaSrc.voting.VotingVariant;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/vote")
public class VoteController {
    @Autowired
    private final VoteService voteService;
    @Autowired
    private final ObjectMapper objectMapper;
    @Autowired
    private final PeopleController userController;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private final InvitationsService invitationsService;
    @Autowired
    private final ClassService classService;

    public VoteController(VoteService voteService, ObjectMapper objectMapper, PeopleController userController,
            SimpMessagingTemplate messagingTemplate,InvitationsService invitationsService,ClassService classService) {
        this.voteService = voteService;
        this.objectMapper = new ObjectMapper();
        this.userController = userController;
        this.messagingTemplate = messagingTemplate;
        this.invitationsService = invitationsService;
        this.classService = classService;
    }

    @PostMapping("/createVoting")
    public ResponseEntity<Vote> createVoting(@RequestBody Vote request, Authentication auth) {
        try {
            Vote newVote = new Vote();
            newVote.setSchoolId(userController.currentUser(auth).getSchoolId());
            Long classId = request.getClassId() != null ? request.getClassId()
                    : userController.currentUser(auth).getClassId();
            newVote.setClassId(classId);
            newVote.setTitle(request.getTitle());
            newVote.setDescription(request.getDescription());
            newVote.setCreatedBy(userController.currentUser(auth).getId());
            newVote.setStartDate(request.getStartDate());
            newVote.setEndDate(request.getEndDate());
            newVote.setMultipleChoice(request.isMultipleChoice());

            if (request.getVotingLevel() != null) {
                newVote.setVotingLevel(request.getVotingLevel());
            } else {
                newVote.setVotingLevel(Vote.VotingLevel.SCHOOL);
            }

            newVote.setStatus(Vote.VoteStatus.OPEN);

            try {
                if (request.getVariants() != null && !request.getVariants().isEmpty()) {
                    String variantsJson = objectMapper.writeValueAsString(
                            request.getVariants().stream().map(VotingVariant::getText).toList());
                    newVote.setVariantsJson(variantsJson);
                } else {
                    newVote.setVariantsJson("[]");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(null);
            }

            List<String> variantStrings = request.getVariants() != null
                    ? request.getVariants().stream().map(VotingVariant::getText).toList()
                    : List.of();

            List<Long> participantIds = request.getParticipants() != null
                    ? request.getParticipants().stream().map(VotingParticipant::getUserId).toList()
                    : List.of();

            Vote createdVote = voteService.createVoting(newVote, variantStrings, participantIds);

            messagingTemplate.convertAndSend("/topic/votings/new", createdVote);
            if (createdVote.getSchoolId() != null) {
                messagingTemplate.convertAndSend("/topic/votings/school/" + createdVote.getSchoolId(), createdVote);
            }
            if (createdVote.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/votings/class/" + createdVote.getClassId(), createdVote);
            }
            participantIds.forEach(userId -> {
                messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/my-votings/new", createdVote);
            });

            return new ResponseEntity<>(createdVote, HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/votes")
    public List<Vote> getVotes(Authentication auth,
                            @RequestParam(required = false) String className) {
        People currentUser = userController.currentUser(auth);
        Long schoolId = currentUser.getSchoolId();
        Long classId = classService.getClassesBySchoolIdAndName(schoolId, className).getId();

        List<InvitationDTO> invitations = invitationsService.getInvitationsForUser(currentUser.getId());
        List<Long> invitedVoteIds = invitations.stream()
                .filter(inv -> inv.getType() == InvitationDTO.Type.VOTE)
                .map(InvitationDTO::getEventOrVoteId)
                .toList();

        List<Vote> invitedVotes = invitedVoteIds.stream()
                .map(voteService::getVotingById)
                .filter(Objects::nonNull)
                .toList();

        List<Vote> votesBySchoolOrClass = new ArrayList<>();
        if (schoolId != null) {
            if (classId != null) {
                votesBySchoolOrClass.addAll(voteService.getVotingsByClassAndSchool(classId, schoolId));
            } 
            votesBySchoolOrClass.addAll(voteService.getVotingsBySchool(schoolId));
        }

        if (currentUser.getRole() == People.Role.STUDENT || currentUser.getRole() == People.Role.PARENT) {     
            votesBySchoolOrClass.addAll(voteService.getVotingsByClassAndSchool(currentUser.getClassId(), schoolId));
            votesBySchoolOrClass.addAll(voteService.getVotingsBySchool(schoolId));
        }

        return Stream.concat(invitedVotes.stream(), votesBySchoolOrClass.stream())
                .distinct()
                .toList();
    }

    @GetMapping("voting/{id}")
    public ResponseEntity<Vote> getVotingById(@PathVariable Long id) {
        Vote vote = voteService.getVotingById(id);
        if (vote != null) {
            return new ResponseEntity<>(vote, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("voting/user/{userId}")
    public ResponseEntity<List<Vote>> getAccessibleVotings(@PathVariable Long userId,
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long classId) {
        List<Vote> votings = voteService.getAccessibleVotingsForUser(userId, schoolId, classId);
        return new ResponseEntity<>(votings, HttpStatus.OK);
    }

    @PostMapping(value = "voting/{votingId}/vote", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> castVote(
            @PathVariable Long votingId,
            @RequestBody List<Long> variantIds,
            Authentication auth) {

        Vote vote = voteService.getVotingById(votingId);
        if (!vote.isMultipleChoice() && variantIds.size() > 1) {
            return ResponseEntity
                    .badRequest()
                    .body("Це одно‑відповідне голосування, виберіть лише один варіант.");
        }

        Long userId = userController.currentUser(auth).getId();

        boolean success = voteService.recordVote(votingId, variantIds, userId);
        if (success) {

            VotingResults updatedResults = voteService.getVotingResults(votingId);
            messagingTemplate.convertAndSend("/topic/votings/" + votingId + "/results", updatedResults);
            Vote updatedVote = voteService.getVotingById(votingId);
            if (updatedVote != null) {
                messagingTemplate.convertAndSend("/topic/votings/updated", updatedVote);
                if (updatedVote.getSchoolId() != null) {
                    messagingTemplate.convertAndSend("/topic/votings/school/" + updatedVote.getSchoolId(), updatedVote);
                }
                if (updatedVote.getClassId() != null) {
                    messagingTemplate.convertAndSend("/topic/votings/class/" + updatedVote.getClassId(), updatedVote);
                }
            }
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/my-vote-status",
                    "Ваш голос за голосування ID " + votingId + " успішно зараховано.");

            return ResponseEntity.ok("Vote recorded successfully");
        } else {
            return ResponseEntity.badRequest()
                    .body("Failed to record vote. Check voting status, eligibility, or if you already voted.");
        }
    }

    @GetMapping("voting/{votingId}/results")
    public ResponseEntity<VotingResults> getVotingResults(@PathVariable Long votingId) {
        VotingResults results = voteService.getVotingResults(votingId);
        if (results != null) {
            return new ResponseEntity<>(results, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("voting/{id}")
    public ResponseEntity<Vote> updateVoting(@PathVariable Long id, @RequestBody Vote request, Authentication auth) {
        Vote updatedVote = new Vote();
        updatedVote.setSchoolId(userController.currentUser(auth).getSchoolId());
        Long classId = request.getClassId() != null ? request.getClassId()
                : userController.currentUser(auth).getClassId();
        updatedVote.setClassId(classId);
        updatedVote.setTitle(request.getTitle());
        updatedVote.setDescription(request.getDescription());
        updatedVote.setCreatedBy(userController.currentUser(auth).getId());
        updatedVote.setStartDate(request.getStartDate());
        updatedVote.setEndDate(request.getEndDate());
        updatedVote.setMultipleChoice(request.isMultipleChoice());
        updatedVote.setVotingLevel(request.getVotingLevel());

        List<String> variantStrings = request.getVariants() != null
                ? request.getVariants().stream().map(VotingVariant::getText).toList()
                : List.of();

        List<Long> participantIds = request.getParticipants() != null
                ? request.getParticipants().stream().map(VotingParticipant::getUserId).toList()
                : List.of();

        Vote resultVote = voteService.updateVoting(id, updatedVote, variantStrings, participantIds);

        if (resultVote != null) {
            messagingTemplate.convertAndSend("/topic/votings/updated", resultVote);
            messagingTemplate.convertAndSend("/topic/votings/" + id + "/updated", resultVote);
            if (resultVote.getSchoolId() != null) {
                messagingTemplate.convertAndSend("/topic/votings/school/" + resultVote.getSchoolId(), resultVote);
            }
            if (resultVote.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/votings/class/" + resultVote.getClassId(), resultVote);
            }
        }
        return new ResponseEntity<>(resultVote, HttpStatus.CREATED);
    }

    @DeleteMapping("voting/{id}")
    public ResponseEntity<Void> deleteVoting(@PathVariable Long id) {
        Vote deletedVote = voteService.getVotingById(id);

        voteService.deleteVoting(id);

        messagingTemplate.convertAndSend("/topic/votings/deleted", "Голосування ID " + id + " було видалено.");
        if (deletedVote != null) {
            if (deletedVote.getSchoolId() != null) {
                messagingTemplate.convertAndSend("/topic/votings/school/" + deletedVote.getSchoolId() + "/deleted",
                        "Голосування ID " + id + " було видалено.");
            }
            if (deletedVote.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/votings/class/" + deletedVote.getClassId() + "/deleted",
                        "Голосування ID " + id + " було видалено.");
            }
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}