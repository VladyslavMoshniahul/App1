package com.example.demo.javaSrc.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.invitations.Invitation;
import com.example.demo.javaSrc.invitations.InvitationDTO;
import com.example.demo.javaSrc.invitations.InvitationsService;
import com.example.demo.javaSrc.invitations.UserInvitationStatus;

@RestController
@RequestMapping("/api/invitations")
public class InvitationsController {
    @Autowired
    private final PeopleController userController;
    @Autowired
    private final InvitationsService invitationsService;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public InvitationsController(PeopleController userController, 
            InvitationsService invitationsService,
            SimpMessagingTemplate messagingTemplate) {
        this.userController = userController;
        this.invitationsService = invitationsService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/createEventInvitation")
    public ResponseEntity<Invitation> createEventInvitation(Authentication auth,
            @RequestBody List<Long> userIds,
            @RequestParam Long eventId) {
        Invitation saved = invitationsService.createEventInvitation(userController.currentUser(auth).getId(), eventId,
                userIds);
        messagingTemplate.convertAndSend("/topic/invitations/" + eventId, "new invitation");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/createVoteInvitation")
    public ResponseEntity<Invitation> createVoteInvitation(Authentication auth,
            @RequestBody List<Long> userIds,
            @RequestParam Long voteId) {
        Invitation saved = invitationsService.createVoteInvitation(userController.currentUser(auth).getId(), voteId,
                userIds);
        messagingTemplate.convertAndSend("/topic/invitations/vote/" + voteId, "new invitation");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/myInvitations/{type}")
    public ResponseEntity<List<InvitationDTO>> getMyInvitations(Authentication auth,
            @PathVariable String type) {

        Long userId = userController.currentUser(auth).getId();
        List<InvitationDTO> invitations = invitationsService.getInvitationsForUser(userId);
        if ("event".equalsIgnoreCase(type)) {
            invitations = invitations.stream()
                    .filter(inv -> inv.getType() == InvitationDTO.Type.EVENT)
                    .toList();
        } else if ("vote".equalsIgnoreCase(type)) {
            invitations = invitations.stream()
                    .filter(inv -> inv.getType() == InvitationDTO.Type.VOTE)
                    .toList();
        }
        return ResponseEntity.ok(invitations);
    }

    @PatchMapping("/changeStatus/{id}")
    public ResponseEntity<UserInvitationStatus> changeInvitationStatus(@PathVariable Long invitationId, 
                                                                        Authentication auth, 
                                                                        @RequestParam UserInvitationStatus.Status status) {
        UserInvitationStatus invitation= invitationsService.
                                                updateInvitationStatus(invitationId, userController.currentUser(auth).getId(),  status);

        return ResponseEntity.ok(invitation);
    }
}
