package com.example.demo.javaSrc.invitations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInvitationStatusRepository extends JpaRepository<UserInvitationStatus,Long>{
    List<UserInvitationStatus> findByUserId(Long userId);

    Optional<UserInvitationStatus> findByInvitationIdAndUserId(Long invitationId, Long userId);

    List<UserInvitationStatus> findAllByEventId(Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT u.userId FROM UserInvitationStatus u WHERE u.invitation.eventId = :eventId AND u.status = :status")
    List<Long> findUserIdsByEventIdAndStatus(Long eventId, UserInvitationStatus.Status status);

    void deleteByInvitationId(Long invitationId);

    @Query("SELECT u.status, COUNT(u) FROM UserInvitationStatus u WHERE u.invitation.eventId = :eventId GROUP BY u.status")
    Map<UserInvitationStatus.Status, Long> countStatusesByEventId(Long eventId);

    List<UserInvitationStatus>  findByUserIdAndStatus(Long userId, UserInvitationStatus.Status status);
}
