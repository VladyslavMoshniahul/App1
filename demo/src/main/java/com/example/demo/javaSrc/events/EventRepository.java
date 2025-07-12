package com.example.demo.javaSrc.events;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findBySchoolId(Long schoolId);
    List<Event> findBySchoolIdAndClassId(Long schoolId, Long classId);
    List<Event> findByCreatedByAndStartEventAfter(Long userId, LocalDateTime now);
    List<Event> findByCreatedByAndStartEventBefore(Long userId, LocalDateTime now);
    List<Event> findByCreatedByAndTitleContainingIgnoreCase(Long userId, String keyword);
    List<Event> findByCreatedByAndStartEventBetween(Long userId, LocalDateTime from, LocalDateTime to);
}
