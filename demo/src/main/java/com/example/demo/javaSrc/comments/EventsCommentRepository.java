package com.example.demo.javaSrc.comments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EventsCommentRepository extends JpaRepository<EventsComment, Long>{
    List<EventsComment> findByEventId(Long eventId);
    List<EventsComment> findByUserId(Long userId);
    List<EventsComment> findByUserIdAndEventId(Long userId, Long eventId);

    @Transactional
    void deleteByEventId(Long eventId);
    @Transactional
    void deleteByUserId(Long userId);
}
