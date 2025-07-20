package com.example.demo.javaSrc.invitations;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInvitationStatusRepository extends JpaRepository<UserInvitationStatus,Long>{
    List<UserInvitationStatus> findByUserId(Long userId);

    Optional<UserInvitationStatus> findByInvitationIdAndUserId(Long invitationId, Long userId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user_invitation_status u JOIN invitation i ON u.invitation_id = i.id WHERE u.user_id = :userId AND i.event_id = :eventId)", nativeQuery = true)
    boolean existsByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query(value = "SELECT u.user_id FROM user_invitation_status u JOIN invitation i ON u.invitation_id = i.id WHERE i.event_id = :eventId AND u.status = :status", nativeQuery = true)
    List<Long> findUserIdsByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);

    @Query(value = "SELECT u.status, COUNT(*) FROM user_invitation_status u JOIN invitation i ON u.invitation_id = i.id WHERE i.event_id = :eventId GROUP BY u.status", nativeQuery = true)
    List<Object[]> countStatusesByEventId(@Param("eventId") Long eventId);

    void deleteByInvitationId(Long invitationId);
    void deleteByUserId(Long userId); 

    List<UserInvitationStatus>  findByUserIdAndStatus(Long userId, UserInvitationStatus.Status status);
}
