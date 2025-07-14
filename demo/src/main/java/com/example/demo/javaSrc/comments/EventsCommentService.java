package com.example.demo.javaSrc.comments;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventsCommentService {

    @Autowired
    private final EventsCommentRepository eventsCommentRepository;

    public EventsCommentService(EventsCommentRepository eventsCommentRepository) {
        this.eventsCommentRepository = eventsCommentRepository;
    }

    public EventsComment createComment(EventsComment comment) {
        return eventsCommentRepository.save(comment);
    }

    public EventsComment getCommentById(Long id) {
        return eventsCommentRepository.findById(id).orElse(null);
    }

    public EventsComment updateComment(EventsComment comment) {
        return eventsCommentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        eventsCommentRepository.deleteById(id);
    }
    public List<EventsComment> getAllComments() {
        return eventsCommentRepository.findAll();
    }

    public List<EventsComment> getCommentsByEventId(Long eventId) {
        return eventsCommentRepository.findByEventId(eventId);
    }

    public List<EventsComment> getCommentsByUserId(Long userId) {
        return eventsCommentRepository.findByUserId(userId);
    }

    public List<EventsComment> getCommentsByUserIdAndEventId(Long userId, Long eventId) {
        return eventsCommentRepository.findByUserIdAndEventId(userId, eventId);
    }
    
    public void deleteCommentsByEventId(Long eventId) {
        eventsCommentRepository.deleteByEventId(eventId);
    }

    public void deleteCommentsByUserId(Long userId) {
        eventsCommentRepository.deleteByUserId(userId);
    }
}
