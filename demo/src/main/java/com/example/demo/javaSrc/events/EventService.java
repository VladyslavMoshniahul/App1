package com.example.demo.javaSrc.events;



import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getBySchoolAndClass(Long schoolId, Long classId) {
        if (classId == null) {
            return eventRepository.findBySchoolId(schoolId);
        }
        return eventRepository.findBySchoolIdAndClassId(schoolId, classId);
    }
    
      public List<Event> getFutureEvents(Long userId) {
        return eventRepository.findByCreatedByAndStartEventAfter(userId, LocalDateTime.now());
    }

    public List<Event> getPastEvents(Long userId) {
        return eventRepository.findByCreatedByAndStartEventBefore(userId, LocalDateTime.now());
    }

    public List<Event> searchByTitle(Long userId, String keyword) {
        return eventRepository.findByCreatedByAndTitleContainingIgnoreCase(userId, keyword);
    }

    public List<Event> searchByDateRange(Long userId, LocalDateTime from, LocalDateTime to) {
        return eventRepository.findByCreatedByAndStartEventBetween(userId, from, to);
    }
    public List<Event> getEventsForSchool(Long schoolId) {
        return eventRepository.findBySchoolId(schoolId);
    }
    public List<Event> getEventsForClass(Long schoolId, Long classId) {
        return eventRepository.findBySchoolIdAndClassId(schoolId, classId);
    }
}
