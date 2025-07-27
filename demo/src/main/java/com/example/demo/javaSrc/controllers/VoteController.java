package com.example.demo.javaSrc.controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final UserController userController;

    public VoteController(VoteService voteService, ObjectMapper objectMapper,UserController userController) {
        this.voteService = voteService;
        this.objectMapper = new ObjectMapper();
        this.userController = userController;
    }

    @PostMapping("/createVoting")
    public ResponseEntity<Vote> createVoting(@RequestBody Vote request, Authentication auth) {
        try {
            Vote newVote = new Vote();
            newVote.setSchoolId(userController.currentUser(auth).getSchoolId());
            Long classId = request.getClassId() != null ? 
                                                        request.getClassId() : userController.currentUser(auth).getClassId();
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
            return new ResponseEntity<>(createdVote, HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/votes")
    public List<Vote> getVotes(@RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId) {
        if (classId != null && schoolId != null) {
            return voteService.getVotingsByClassAndSchool(classId, schoolId);
        } else if (schoolId != null) {
            return voteService.getVotingsBySchool(schoolId);
        } else {
            return voteService.getAllVotings();
        }
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
         Long classId = request.getClassId() != null ? 
                                                        request.getClassId() : userController.currentUser(auth).getClassId();
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

        Vote createdVote = voteService.updateVoting(id, updatedVote, variantStrings, participantIds);
        return new ResponseEntity<>(createdVote, HttpStatus.CREATED);
    }

    @DeleteMapping("voting/{id}")
    public ResponseEntity<Void> deleteVoting(@PathVariable Long id) {
        voteService.deleteVoting(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}