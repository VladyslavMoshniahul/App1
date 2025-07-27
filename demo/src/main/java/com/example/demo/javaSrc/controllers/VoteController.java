package com.example.demo.javaSrc.controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Додано імпорт
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
import com.fasterxml.jackson.core.JsonProcessingException;
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
    @Autowired
    private final SimpMessagingTemplate messagingTemplate; // Додано SimpMessagingTemplate

    public VoteController(VoteService voteService, ObjectMapper objectMapper, UserController userController, SimpMessagingTemplate messagingTemplate) {
        this.voteService = voteService;
        this.objectMapper = objectMapper; // Використовуйте наданий ObjectMapper
        this.userController = userController;
        this.messagingTemplate = messagingTemplate; // Ініціалізація
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
            } catch (JsonProcessingException e) { // Змінено на JsonProcessingException
                e.printStackTrace();
                // --- WebSocket Integration ---
                // Сповіщення про помилку серіалізації варіантів голосування
                messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/errors", "Error processing voting variants: " + e.getMessage());
                // --- End WebSocket Integration ---
                return ResponseEntity.status(500).body(null);
            }

            List<String> variantStrings = request.getVariants() != null
                    ? request.getVariants().stream().map(VotingVariant::getText).toList()
                    : List.of();

            List<Long> participantIds = request.getParticipants() != null
                    ? request.getParticipants().stream().map(VotingParticipant::getUserId).toList()
                    : List.of();

            Vote createdVote = voteService.createVoting(newVote, variantStrings, participantIds);

            // --- WebSocket Integration ---
            // Сповіщаємо про створення нового голосування
            if (createdVote.getSchoolId() != null) {
                messagingTemplate.convertAndSend("/topic/school/" + createdVote.getSchoolId() + "/votings", createdVote);
            }
            if (createdVote.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/class/" + createdVote.getClassId() + "/votings", createdVote);
            } else {
                messagingTemplate.convertAndSend("/topic/votings/general", createdVote);
            }
            // Сповіщаємо учасників про нове голосування
            for (Long participantId : participantIds) {
                messagingTemplate.convertAndSendToUser(participantId.toString(), "/queue/newVoting", createdVote);
            }
            // --- End WebSocket Integration ---

            return new ResponseEntity<>(createdVote, HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace();
            // --- WebSocket Integration ---
            // Сповіщення про загальну помилку створення голосування
            if (auth != null && userController.currentUser(auth) != null) {
                messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/errors", "Error creating voting: " + ex.getMessage());
            }
            // --- End WebSocket Integration ---
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/votes")
    public List<Vote> getVotes(@RequestParam(required = false) Long schoolId,
                               @RequestParam(required = false) Long classId) {
        List<Vote> votes;
        if (classId != null && schoolId != null) {
            votes = voteService.getVotingsByClassAndSchool(classId, schoolId);
        } else if (schoolId != null) {
            votes = voteService.getVotingsBySchool(schoolId);
        } else {
            votes = voteService.getAllVotings();
        }
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSend("/topic/votings/list", votes);
        // --- End WebSocket Integration ---
        return votes;
    }

    @GetMapping("voting/{id}")
    public ResponseEntity<Vote> getVotingById(@PathVariable Long id) {
        Vote vote = voteService.getVotingById(id);
        if (vote != null) {
            // --- WebSocket Integration (Optional for GET methods) ---
            // messagingTemplate.convertAndSend("/topic/voting/" + id, vote);
            // --- End WebSocket Integration ---
            return new ResponseEntity<>(vote, HttpStatus.OK);
        }
        // --- WebSocket Integration ---
        // messagingTemplate.convertAndSend("/topic/voting/" + id + "/notFound", "Voting with ID " + id + " not found.");
        // --- End WebSocket Integration ---
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("voting/user/{userId}")
    public ResponseEntity<List<Vote>> getAccessibleVotings(@PathVariable Long userId,
                                                           @RequestParam Long schoolId,
                                                           @RequestParam(required = false) Long classId) {
        List<Vote> votings = voteService.getAccessibleVotingsForUser(userId, schoolId, classId);
        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/accessibleVotings", votings);
        // --- End WebSocket Integration ---
        return new ResponseEntity<>(votings, HttpStatus.OK);
    }

    @PostMapping(value = "voting/{votingId}/vote", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> castVote(
            @PathVariable Long votingId,
            @RequestBody List<Long> variantIds,
            Authentication auth) {

        Vote vote = voteService.getVotingById(votingId);
        if (vote == null) {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/errors", "Voting with ID " + votingId + " not found.");
            // --- End WebSocket Integration ---
            return ResponseEntity.badRequest().body("Голосування не знайдено.");
        }

        if (!vote.isMultipleChoice() && variantIds.size() > 1) {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/errors", "Це одно-відповідне голосування, виберіть лише один варіант.");
            // --- End WebSocket Integration ---
            return ResponseEntity
                    .badRequest()
                    .body("Це одно-відповідне голосування, виберіть лише один варіант.");
        }

        Long userId = userController.currentUser(auth).getId();

        boolean success = voteService.recordVote(votingId, variantIds, userId);
        if (success) {
            // --- WebSocket Integration ---
            // Сповіщаємо про успішне голосування та оновлюємо результати
            VotingResults results = voteService.getVotingResults(votingId);
            if (results != null) {
                messagingTemplate.convertAndSend("/topic/voting/" + votingId + "/results", results);
            }
            // --- End WebSocket Integration ---
            return ResponseEntity.ok("Vote recorded successfully");
        } else {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/errors", "Не вдалося записати голос. Перевірте статус голосування, право на участь, або чи ви вже проголосували.");
            // --- End WebSocket Integration ---
            return ResponseEntity.badRequest()
                    .body("Failed to record vote. Check voting status, eligibility, or if you already voted.");
        }
    }

    @GetMapping("voting/{votingId}/results")
    public ResponseEntity<VotingResults> getVotingResults(@PathVariable Long votingId) {
        VotingResults results = voteService.getVotingResults(votingId);
        if (results != null) {
            // --- WebSocket Integration (Optional for GET methods) ---
            // messagingTemplate.convertAndSend("/topic/voting/" + votingId + "/currentResults", results);
            // --- End WebSocket Integration ---
            return new ResponseEntity<>(results, HttpStatus.OK);
        }
        // --- WebSocket Integration ---
        // messagingTemplate.convertAndSend("/topic/voting/" + votingId + "/resultsNotFound", "Results for voting with ID " + votingId + " not found.");
        // --- End WebSocket Integration ---
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

        Vote resultVote = voteService.updateVoting(id, updatedVote, variantStrings, participantIds);

        // --- WebSocket Integration ---
        // Сповіщаємо про оновлення голосування
        if (resultVote != null) {
            messagingTemplate.convertAndSend("/topic/voting/" + id + "/updated", resultVote);
            if (resultVote.getSchoolId() != null) {
                messagingTemplate.convertAndSend("/topic/school/" + resultVote.getSchoolId() + "/votings/updated", resultVote);
            }
            if (resultVote.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/class/" + resultVote.getClassId() + "/votings/updated", resultVote);
            }
             // Сповіщаємо учасників про оновлення
            for (Long participantId : participantIds) {
                messagingTemplate.convertAndSendToUser(participantId.toString(), "/queue/votingUpdated", resultVote);
            }
        } else {
             // Сповіщення про невдале оновлення
             if (auth != null && userController.currentUser(auth) != null) {
                messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/errors", "Failed to update voting with ID: " + id);
            }
        }
        // --- End WebSocket Integration ---

        return new ResponseEntity<>(resultVote, HttpStatus.CREATED); // Можливо HttpStatus.OK або HttpStatus.ACCEPTED
    }

    @DeleteMapping("voting/{id}")
    public ResponseEntity<Void> deleteVoting(@PathVariable Long id) {
        Vote voteToDelete = voteService.getVotingById(id); // Отримати голосування перед видаленням для інформації
        voteService.deleteVoting(id);

        // --- WebSocket Integration ---
        // Сповіщаємо про видалення голосування
        if (voteToDelete != null) {
            messagingTemplate.convertAndSend("/topic/voting/" + id + "/deleted", id);
            if (voteToDelete.getSchoolId() != null) {
                messagingTemplate.convertAndSend("/topic/school/" + voteToDelete.getSchoolId() + "/votings/deleted", id);
            }
            if (voteToDelete.getClassId() != null) {
                messagingTemplate.convertAndSend("/topic/class/" + voteToDelete.getClassId() + "/votings/deleted", id);
            }
        }
        // --- End WebSocket Integration ---

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}