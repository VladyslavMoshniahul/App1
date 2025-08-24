package com.example.demo.javaSrc.invitations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.javaSrc.events.Event;
import com.example.demo.javaSrc.events.EventRepository;
import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.peoples.PeopleRepository;
import com.example.demo.javaSrc.voting.Vote;
import com.example.demo.javaSrc.voting.VoteRepository;

import jakarta.transaction.Transactional;

@Service
public class InvitationsService {

    @Autowired
    private final InvitationsRepository invitationsRepository;
    @Autowired
    private final UserInvitationStatusRepository userInvitationStatusRepository;
    @Autowired
    private final PeopleRepository userRepository;
    @Autowired
    private final EventRepository eventRepository;
    @Autowired
    private final VoteRepository votingRepository;

    public InvitationsService(
            InvitationsRepository invitationsRepository,
            UserInvitationStatusRepository userInvitationStatusRepository,
            PeopleRepository userRepository,
            EventRepository eventRepository,
            VoteRepository votingRepository) {
        this.invitationsRepository = invitationsRepository;
        this.userInvitationStatusRepository = userInvitationStatusRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.votingRepository = votingRepository;
    }

    @Transactional
    private Invitation createInvitation(Long inviterId, Long eventId, Long voteId, List<Long> targetUserIds) {
        if ((eventId == null && voteId == null) || (eventId != null && voteId != null)) {
            throw new IllegalArgumentException("Invitation must have either eventId OR voteId, but not both");
        }

        People inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new IllegalArgumentException("Ініціатор запрошення не знайдений"));

        if (inviter.getRole() == People.Role.STUDENT) {
            List<People> targets = userRepository.findAllById(targetUserIds);
            boolean hasInvalidTargets = targets.stream()
                    .anyMatch(user -> user.getRole() != People.Role.STUDENT);
            if (hasInvalidTargets) {
                throw new SecurityException("Учень може запрошувати лише інших учнів.");
            }
        }

        Invitation invitation = new Invitation();
        invitation.setUserId(inviterId);
        invitation.setEventId(eventId);
        invitation.setVoteId(voteId);

        Invitation savedInvitation = invitationsRepository.save(invitation);

        List<UserInvitationStatus> statuses = targetUserIds.stream().map(userId -> {
            UserInvitationStatus status = new UserInvitationStatus();
            status.setInvitationId(savedInvitation.getId());
            status.setUserId(userId);
            status.setStatus(UserInvitationStatus.Status.PENDING);
            status.setUpdatedAt(LocalDateTime.now());
            return status;
        }).toList();

        userInvitationStatusRepository.saveAll(statuses);
        return savedInvitation;
    }

    public Invitation createEventInvitation(Long inviterId, Long eventId, List<Long> targetUserIds) {
        return createInvitation(inviterId, eventId, null, targetUserIds);
    }

    public Invitation createVoteInvitation(Long inviterId, Long voteId, List<Long> targetUserIds) {
        return createInvitation(inviterId, null, voteId, targetUserIds);
    }

    public List<Invitation> getInvitationsByEventId(Long eventId) {
        return invitationsRepository.findByEventId(eventId);
    }

    public List<Invitation> getInvitationsByVoteId(Long voteId) {
        return invitationsRepository.findByVoteId(voteId);
    }

    public List<InvitationDTO> getInvitationsForUser(Long userId) {
        List<UserInvitationStatus> statuses = userInvitationStatusRepository.findByUserId(userId);

        return statuses.stream().map(status -> {
            Invitation invitation = invitationsRepository.findById(status.getInvitationId()).orElse(null);
            if (invitation == null)
                return null;

            People creator = userRepository.findById(invitation.getUserId()).orElse(null);

            String title;
            LocalDateTime start;
            InvitationDTO.Type type;

            if (invitation.getEventId() != null) {
                Event event = eventRepository.findById(invitation.getEventId()).orElse(null);
                title = event != null ? event.getTitle() : "Невідома подія";
                start = event != null ? event.getStartEvent() : null;
                type = InvitationDTO.Type.EVENT;
            } else {
                Vote voting = votingRepository.findById(invitation.getVoteId()).orElse(null);
                title = voting != null ? voting.getTitle() : "Невідоме голосування";
                start = voting !=null ? voting.getStartDate() : null;
                type = InvitationDTO.Type.VOTE;
            }

            return new InvitationDTO(
                    invitation.getId(),
                    invitation.getEventId() != null ? invitation.getEventId() : invitation.getVoteId(),
                    title,
                    start,
                    creator != null ? creator.getEmail() : "Невідомий користувач",
                    status.getStatus(),
                    status.getUpdatedAt(),
                    type);
        }).filter(Objects::nonNull).toList();
    }

    public UserInvitationStatus updateInvitationStatus(Long invitationId, Long userId, UserInvitationStatus.Status status) {
        UserInvitationStatus invitationStatus = userInvitationStatusRepository
                .findByInvitationIdAndUserId(invitationId, userId).orElseThrow(() -> new RuntimeException("Not found"));
        invitationStatus.setStatus(status);
        LocalDateTime now = LocalDateTime.now();
        invitationStatus.setUpdatedAt(now);
        return userInvitationStatusRepository.save(invitationStatus);
    }

    public void deleteInvitation(Long invitationId) {
        userInvitationStatusRepository.deleteByInvitationId(invitationId);
        invitationsRepository.deleteById(invitationId);
    }

    public List<UserInvitationStatus> getUserInvitationsByStatus(Long userId, UserInvitationStatus.Status status) {
        return userInvitationStatusRepository.findByUserIdAndStatus(userId, status);
    }
}
