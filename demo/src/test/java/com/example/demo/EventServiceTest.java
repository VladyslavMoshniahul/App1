package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.example.demo.javaSrc.eventsANDtask.Event;
import com.example.demo.javaSrc.eventsANDtask.EventRepository;
import com.example.demo.javaSrc.eventsANDtask.EventService;

public class EventServiceTest {

    @Mock 
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEvents() {
        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);
        List<Event> events = List.of(event1, event2);

        Mockito.when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.getAllEvents();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(event1.getId(), result.get(0).getId());
        Assertions.assertEquals(event2.getId(), result.get(1).getId());
    }

    @Test
    void testCreateEvent() {
        Event event = new Event();
        event.setId(1L);

        Mockito.when(eventRepository.save(event)).thenReturn(event);

        Event result = eventService.createEvent(event);

        Assertions.assertEquals(event.getId(), result.getId());
    }

    @Test
    void testGetBySchoolAndClass() {
        Long schoolId = 1L;
        Long classId = 2L;
        Event event1 = new Event();
        event1.setSchoolId(schoolId);
        event1.setClassId(classId);
        List<Event> events = List.of(event1);

        Mockito.when(eventRepository.findBySchoolIdAndClassId(schoolId, classId)).thenReturn(events);

        List<Event> result = eventService.getBySchoolAndClass(schoolId, classId);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(event1.getId(), result.get(0).getId());
    }

    @Test
    void testGetFutureEvents() {
        Long userId = 1L;
        Event event1 = new Event();
        event1.setId(100L); 

        event1.setCreatedBy(userId);
        List<Event> events = List.of(event1);

        Mockito.when(eventRepository.findByCreatedByAndStartEventAfter(eq(userId), any(LocalDateTime.class)))
            .thenReturn(events);

        List<Event> result = eventService.getFutureEvents(userId);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(event1.getId(), result.get(0).getId());
    }

    @Test
    void testGetPastEvents() {
        Long userId = 1L;
        Event event1 = new Event();
        event1.setId(100L); 
        event1.setCreatedBy(userId);

        List<Event> events = List.of(event1);

        Mockito.when(eventRepository.findByCreatedByAndStartEventBefore(eq(userId), any(LocalDateTime.class)))
            .thenReturn(events);

        List<Event> result = eventService.getPastEvents(userId);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(event1.getId(), result.get(0).getId());
    }


    @Test
    void testSearchByTitle() {
        Long userId = 1L;
        String keyword = "test";
        Event event1 = new Event();
        event1.setCreatedBy(userId);
        event1.setTitle("Test Event");
        List<Event> events = List.of(event1);

        Mockito.when(eventRepository.findByCreatedByAndTitleContainingIgnoreCase(userId, keyword)).thenReturn(events);

        List<Event> result = eventService.searchByTitle(userId, keyword);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(event1.getId(), result.get(0).getId());
    }

    @Test 
    void testSearchByDateRange() {
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        Event event1 = new Event();
        event1.setCreatedBy(userId);
        List<Event> events = List.of(event1);

        Mockito.when(eventRepository.findByCreatedByAndStartEventBetween(userId, from, to)).thenReturn(events);

        List<Event> result = eventService.searchByDateRange(userId, from, to);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(event1.getId(), result.get(0).getId());
    }

    @Test 
    void testGetEventsForSchool() {
        Long schoolId = 1L;
        Event event1 = new Event();
        event1.setSchoolId(schoolId);
        List<Event> events = List.of(event1);

        Mockito.when(eventRepository.findBySchoolId(schoolId)).thenReturn(events);

        List<Event> result = eventService.getEventsForSchool(schoolId);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(event1.getId(), result.get(0).getId());
    }

    @Test
    void testGetEventsForClass() {
        Long schoolId = 1L;
        Long classId = 2L;
        Event event1 = new Event();
        event1.setSchoolId(schoolId);
        event1.setClassId(classId);
        List<Event> events = List.of(event1);

        Mockito.when(eventRepository.findBySchoolIdAndClassId(schoolId, classId)).thenReturn(events);

        List<Event> result = eventService.getEventsForClass(schoolId, classId);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(event1.getId(), result.get(0).getId());
    }
}
