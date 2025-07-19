package com.example.demo.javaSrc.invitations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.javaSrc.events.Event;
import com.example.demo.javaSrc.events.EventRepository;
import com.example.demo.javaSrc.users.User;
import com.example.demo.javaSrc.users.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class InvitationsService {

    @Autowired private final InvitationsRepository invitationsRepository;
    @Autowired private final UserInvitationStatusRepository userInvitationStatusRepository;
    @Autowired private final UserRepository userRepository;
    @Autowired private final EventRepository eventRepository;
    public InvitationsService(
            InvitationsRepository invitationsRepository,
            UserInvitationStatusRepository userInvitationStatusRepository,
            UserRepository userRepository,
            EventRepository eventRepository
    ) {
        this.invitationsRepository = invitationsRepository;
        this.userInvitationStatusRepository = userInvitationStatusRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void createInvitations(Long inviterId, Long eventId, List<Long> targetUserIds) {
        User inviter = userRepository.findById(inviterId).orElseThrow(() ->
                new IllegalArgumentException("Ініціатор запрошення не знайдений"));

        if (inviter.getRole() == User.Role.STUDENT) {
            List<User> targets = userRepository.findAllById(targetUserIds);
            boolean hasInvalidTargets = targets.stream()
                    .anyMatch(user -> user.getRole() != User.Role.STUDENT);
            if (hasInvalidTargets) {
                throw new SecurityException("Учень може запрошувати лише інших учнів.");
            }
        }

        Invitation invitation = new Invitation();
        invitation.setEventId(eventId);
        invitation.setUserId(inviterId);
        Invitation savedInvitation = invitationsRepository.save(invitation);

        List<UserInvitationStatus> statuses = targetUserIds.stream().map(userId -> {
            UserInvitationStatus status = new UserInvitationStatus();
            status.setInvitationId(savedInvitation.getId());
            status.setUserId(userId);
            status.setStatus(UserInvitationStatus.Status.PENDING);
            return status;
        }).toList();

        userInvitationStatusRepository.saveAll(statuses);
    }

     public void updateInvitationStatus(Long invitationId, Long userId, UserInvitationStatus.Status status) {
        UserInvitationStatus invitationStatus = userInvitationStatusRepository
            .findByInvitationIdAndUserId(invitationId, userId)
            .orElseThrow(() -> new RuntimeException("Not found"));

        invitationStatus.setStatus(status);
        LocalDateTime now = LocalDateTime.now();
        invitationStatus.setUpdatedAt(now);
        userInvitationStatusRepository.save(invitationStatus);
    }

    public List<InvitationDTO> getInvitationsForUser(Long userId) {
        List<UserInvitationStatus> statuses = userInvitationStatusRepository.findByUserId(userId);
        return statuses.stream().map(status -> {
            Invitation invitation = invitationsRepository.findById(status.getInvitationId()).orElse(null);
            Event event = eventRepository.findById(invitation.getEventId()).orElse(null);
            User creator = userRepository.findById(invitation.getUserId()).orElse(null);
            return new InvitationDTO(
                invitation.getId(),
                invitation.getEventId(),
                event != null ? event.getTitle() : "Невідома подія",
                event != null ? event.getStartEvent() : null,
                creator != null ? creator.getFirstName() : "Невідомий користувач",
                status.getStatus(),
                status.getUpdatedAt()
            );
        }).toList();
    }


    public List<Invitation> getSentInvitationsByUser(Long inviterId) {
        return invitationsRepository.findByCreatedBy(inviterId);
    }

    public void deleteInvitation(Long invitationId) {
        userInvitationStatusRepository.deleteByInvitationId(invitationId);
        invitationsRepository.deleteById(invitationId);
    }

    public Map<UserInvitationStatus.Status, Long> getInvitationStatsForEvent(Long eventId) {
        return userInvitationStatusRepository.countStatusesByEventId(eventId);
    }

    public List<UserInvitationStatus> getAllStatusesForEvent(Long eventId) {
        return userInvitationStatusRepository.findAllByEventId(eventId);
    }

    public boolean hasUserBeenInvitedToEvent(Long userId, Long eventId) {
        return userInvitationStatusRepository.existsByUserIdAndEventId(userId, eventId);
    }

    public void resendInvitation(Long invitationId, Long userId) {
        UserInvitationStatus status = userInvitationStatusRepository
            .findByInvitationIdAndUserId(invitationId, userId)
            .orElseThrow(() -> new RuntimeException("Not found"));
        status.setStatus(UserInvitationStatus.Status.PENDING);
        LocalDateTime now = LocalDateTime.now();
        status.setUpdatedAt(now);
        userInvitationStatusRepository.save(status);
    }

    public List<User> getRespondedUsers(Long eventId, UserInvitationStatus.Status status) {
        List<Long> userIds = userInvitationStatusRepository.findUserIdsByEventIdAndStatus(eventId, status);
        return userRepository.findAllById(userIds);
    }

     public Invitation createGroupInvitation(Long eventId, Long inviterId, List<Long> userIds) {
        Invitation invitation = new Invitation();
        invitation.setEventId(eventId);
        invitation.setUserId(inviterId);
        LocalDateTime now = LocalDateTime.now();
        invitation.setCreatedAt(now);
        invitation = invitationsRepository.save(invitation);

        for (Long uid : userIds) {
            UserInvitationStatus status = new UserInvitationStatus();
            status.setInvitationId(invitation.getId());
            status.setUserId(uid);
            status.setStatus(UserInvitationStatus.Status.PENDING);
            status.setUpdatedAt(now);
            userInvitationStatusRepository.save(status);
        }
        return invitation;
    }

    public boolean respondToInvitation(Long invitationId, Long userId, UserInvitationStatus.Status status) {
        Optional<UserInvitationStatus> optional = userInvitationStatusRepository.findByInvitationIdAndUserId(invitationId, userId);
        if (optional.isEmpty()) return false;

        UserInvitationStatus userStatus = optional.get();
        if (userStatus.getStatus() != UserInvitationStatus.Status.PENDING) return false;

        userStatus.setStatus(status);
        LocalDateTime now = LocalDateTime.now();
        userStatus.setUpdatedAt(now);
        userInvitationStatusRepository.save(userStatus);
        return true;
    }

    public List<UserInvitationStatus> getUserInvitations(Long userId) {
        return userInvitationStatusRepository.findByUserId(userId);
    }

    public List<UserInvitationStatus> getUserInvitationsByStatus(Long userId, UserInvitationStatus.Status status) {
        return userInvitationStatusRepository.findByUserIdAndStatus(userId, status);
    }
}

