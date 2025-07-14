package com.example.demo.javaSrc.events;



import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    @Autowired
    private final EventRepository eventRepository;
    @Autowired
    private final EventFileRepository eventFileRepository;

    public EventService(EventRepository eventRepository, EventFileRepository eventFileRepository) {
        this.eventRepository = eventRepository;
        this.eventFileRepository = eventFileRepository;
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

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElse(null);
    }

    public EventFile saveEventFile(EventFile file) {
        return eventFileRepository.save(file);
    }

    public List<EventFile> getFilesForEvent(Long eventId) {
        return eventFileRepository.findByEventId(eventId);
    }

    public EventFile getEventFileById(Long fileId) {
        return eventFileRepository.findById(fileId).orElse(null);
    }

}
