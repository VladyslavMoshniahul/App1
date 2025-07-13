package com.example.demo.javaSrc.comments;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsCommentRepository extends JpaRepository<EventsComment, Long>{
    List<EventsComment> findByEventId(Long eventId);
    List<EventsComment> findByUserId(Long userId);
    List<EventsComment> findByUserIdAndEventId(Long userId, Long eventId);
    List<EventsComment> findByDateBetween(LocalDateTime start, LocalDateTime end);
}
