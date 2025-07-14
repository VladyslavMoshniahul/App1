package com.example.demo.javaSrc.invitations;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationsRepository  extends JpaRepository<Invitation, Long>{
    List<Invitation> findByEventId(Long eventId);
    List<Invitation> findByUserId(Long userId);
    List<Invitation> findByEventIdAndUserId(Long eventId, Long userId);
    List<Invitation> findByEventIdAndStatus(Long eventId, Invitation.Status status);
    List<Invitation> findByUserIdAndStatus(Long userId, Invitation.Status status);
    List<Invitation> findByEventIdAndUserIdAndStatus(Long eventId, Long userId, Invitation.Status status);
}
