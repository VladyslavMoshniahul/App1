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
import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.peoples.PeopleService;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.SchoolClass;

@RestController
@RequestMapping("/api/invitations")
public class InvitationsController {
    @Autowired
    private final PeopleController peopleController;
    @Autowired
    private final InvitationsService invitationsService;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private final PeopleService peopleService;
    @Autowired
    private final ClassService classService;

    public InvitationsController(PeopleController peopleController, 
            InvitationsService invitationsService,
            SimpMessagingTemplate messagingTemplate, PeopleService peopleService, ClassService classService) {
        this.peopleController = peopleController;
        this.invitationsService = invitationsService;
        this.messagingTemplate = messagingTemplate;
        this.peopleService = peopleService;
        this.classService = classService;
    }

    @PostMapping("/createEventInvitation")
    public ResponseEntity<Invitation> createEventInvitation(Authentication auth,
            @RequestBody List<Long> userIds,
            @RequestParam Long eventId) {
        Invitation saved = invitationsService.createEventInvitation(peopleController.currentUser(auth).getId(), eventId,
                userIds);
        messagingTemplate.convertAndSend("/topic/invitations/" + eventId, "new invitation");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PostMapping("/createEventInvitationForSchool")
    public ResponseEntity<Invitation> createEventInvitationForSchool(Authentication auth,
            @RequestParam Long eventId) {
        People me = peopleController.currentUser(auth);
        Long schoolId = me.getSchoolId();
        List<Long> userIds = peopleService.getPeopleBySchool(schoolId).stream()
                .map(People::getId)
                .toList();
        Invitation saved = invitationsService.createEventInvitation(peopleController.currentUser(auth).getId(), eventId,
                userIds);
        messagingTemplate.convertAndSend("/topic/schollInvitations/event/" + eventId, "new invitation");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @PostMapping("/createEventInvitationForClass")
    public ResponseEntity<Invitation> createEventInvitationForClass(Authentication auth,
            @RequestParam Long eventId,
            @RequestParam String className) {
        People me = peopleController.currentUser(auth);
        Long schoolId = me.getSchoolId();
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(schoolId, className);
        List<Long> userIds = peopleService.getBySchoolAndClass(schoolId, schoolClass.getId()).stream()
                .map(People::getId)
                .toList();
        Invitation saved = invitationsService.createEventInvitation(peopleController.currentUser(auth).getId(), eventId,
                userIds);
        messagingTemplate.convertAndSend("/topic/classInvitations/event/" + eventId, "new invitation");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/createVoteInvitation")
    public ResponseEntity<Invitation> createVoteInvitation(Authentication auth,
            @RequestBody List<Long> userIds,
            @RequestParam Long voteId) {
        Invitation saved = invitationsService.createVoteInvitation(peopleController.currentUser(auth).getId(), voteId,
                userIds);
        messagingTemplate.convertAndSend("/topic/invitations/vote/" + voteId, "new invitation");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/createVoteInvitationForSchool")
    public ResponseEntity<Invitation> createVoteInvitationForSchool(Authentication auth,
            @RequestParam Long voteId) {
        People me = peopleController.currentUser(auth);
        Long schoolId = me.getSchoolId();
        List<Long> userIds = peopleService.getPeopleBySchool(schoolId).stream()
                .map(People::getId)
                .toList();
        Invitation saved = invitationsService.createVoteInvitation(peopleController.currentUser(auth).getId(), voteId,
                userIds);
        messagingTemplate.convertAndSend("/topic/schollInvitations/vote/" + voteId, "new invitation");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @PostMapping("/createVoteInvitationForClass")
    public ResponseEntity<Invitation> createVoteInvitationForClass(Authentication auth,
            @RequestParam Long voteId,
            @RequestParam String className) {
        People me = peopleController.currentUser(auth);
        Long schoolId = me.getSchoolId();
        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName( schoolId,className);
        List<Long> userIds = peopleService.getBySchoolAndClass(schoolId, schoolClass.getId()).stream()
                .map(People::getId)
                .toList();
        Invitation saved = invitationsService.createVoteInvitation(peopleController.currentUser(auth).getId(), voteId,
                userIds);
        messagingTemplate.convertAndSend("/topic/classInvitations/vote/" + voteId, "new invitation");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/myInvitations/{type}")
    public ResponseEntity<List<InvitationDTO>> getMyInvitations(Authentication auth,
            @PathVariable String type) {

        Long userId = peopleController.currentUser(auth).getId();
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
                                                updateInvitationStatus(invitationId, peopleController.currentUser(auth).getId(),  status);
        messagingTemplate.convertAndSend("/topic/invitations/status/" + peopleController.currentUser(auth).getId(), "status changed");
        return ResponseEntity.ok(invitation);
    }
}
