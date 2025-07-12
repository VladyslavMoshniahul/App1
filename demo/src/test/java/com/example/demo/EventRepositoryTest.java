package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.events.*;
import com.example.demo.javaSrc.users.*;

@SpringBootTest
public class EventRepositoryTest {
    
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository peopleRepository;

    @BeforeEach
    public void setUp() {
       eventRepository.deleteAll();
       peopleRepository.deleteAll();
    }

    @Test
    void testFindBySchoolId() {
        User creator = new User();
        creator.setFirstName("Creator");
        creator.setLastName("Test");
        creator.setEmail("creator@example.com");
        creator.setPassword("password123");
        creator.setRole(User.Role.TEACHER);
        creator.setSchoolId(1L);
        creator.setClassId(1L);
        peopleRepository.save(creator); 

        Event event1 = new Event();
        event1.setSchoolId(1L);
        event1.setCreatedBy(creator.getId());  
        event1.setStartEvent(LocalDateTime.now());
        event1.setTitle("Test Event");
        event1.setDuration(10);
        event1.setEventType(Event.EventType.EXAM);
        eventRepository.save(event1);

        List<Event> events = eventRepository.findBySchoolId(1L);

        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(event1.getSchoolId(), events.get(0).getSchoolId());
    }


    @Test
    void testFindBySchoolIdAndClassId() {
       User creator = new User();
        creator.setFirstName("Creator");
        creator.setLastName("Test");
        creator.setEmail("creator@example.com");
        creator.setPassword("password123");
        creator.setRole(User.Role.TEACHER);
        creator.setSchoolId(1L);
        creator.setClassId(1L);
        peopleRepository.save(creator); 

        Event event1 = new Event();
        event1.setSchoolId(1L);
        event1.setClassId(2L);
        event1.setCreatedBy(creator.getId());  
        event1.setStartEvent(LocalDateTime.now());
        event1.setTitle("Test Event");
        event1.setDuration(10);
        event1.setEventType(Event.EventType.EXAM);
        eventRepository.save(event1);

        List<Event> events = eventRepository.findBySchoolIdAndClassId(1L, 2L);
        
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(event1.getClassId(), events.get(0).getClassId());
    }

    @Test
    void testFindByCreatedByAndStartEventAfter() {
        User creator = new User();
        creator.setFirstName("Creator");
        creator.setLastName("Test");
        creator.setEmail("creator@example.com");
        creator.setPassword("password123");
        creator.setRole(User.Role.TEACHER);
        creator.setSchoolId(1L);
        creator.setClassId(1L);
        peopleRepository.save(creator); 

        Event event1 = new Event();
        event1.setSchoolId(1L);
        event1.setClassId(2L);
        event1.setCreatedBy(creator.getId());  
        event1.setTitle("Test Event");
        event1.setDuration(10);
        event1.setEventType(Event.EventType.EXAM);
        event1.setStartEvent(LocalDateTime.now().minusDays(1));
        eventRepository.save(event1);

        List<Event> events = eventRepository.findByCreatedByAndStartEventAfter(creator.getId(), LocalDateTime.now());
        
        Assertions.assertEquals(0, events.size());
    }

    @Test
    void testFindByCreatedByAndStartEventBefore() {
                User creator = new User();
        creator.setFirstName("Creator");
        creator.setLastName("Test");
        creator.setEmail("creator@example.com");
        creator.setPassword("password123");
        creator.setRole(User.Role.TEACHER);
        creator.setSchoolId(1L);
        creator.setClassId(1L);
        peopleRepository.save(creator); 

        Event event1 = new Event();
        event1.setSchoolId(1L);
        event1.setClassId(2L);
        event1.setCreatedBy(creator.getId());  
        event1.setTitle("Test Event");
        event1.setDuration(10);
        event1.setEventType(Event.EventType.EXAM);
        event1.setStartEvent(LocalDateTime.now().plusDays(1));
        eventRepository.save(event1);

        List<Event> events = eventRepository.findByCreatedByAndStartEventBefore(creator.getId(), LocalDateTime.now());
        
        Assertions.assertEquals(0, events.size());
    }

    @Test
    void testFindByCreatedByAndTitleContainingIgnoreCase() {
        User creator = new User();
        creator.setFirstName("Creator");
        creator.setLastName("Test");
        creator.setEmail("creator@example.com");
        creator.setPassword("password123");
        creator.setRole(User.Role.TEACHER);
        creator.setSchoolId(1L);
        creator.setClassId(1L);
        peopleRepository.save(creator); 

        Event event1 = new Event();
        event1.setSchoolId(1L);
        event1.setClassId(2L);
        event1.setCreatedBy(creator.getId());  
        event1.setTitle("Test Event");
        event1.setDuration(10);
        event1.setEventType(Event.EventType.EXAM);
        event1.setStartEvent(LocalDateTime.now().minusDays(1));
        eventRepository.save(event1);
        

        List<Event> events = eventRepository.findByCreatedByAndTitleContainingIgnoreCase(creator.getId(), "test");
        
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(event1.getTitle(), events.get(0).getTitle());
    }

    @Test
    void testFindByCreatedByAndStartEventBetween() {
        User creator = new User();
        creator.setFirstName("Creator");
        creator.setLastName("Test");
        creator.setEmail("creator@example.com");
        creator.setPassword("password123");
        creator.setRole(User.Role.TEACHER);
        creator.setSchoolId(1L);
        creator.setClassId(1L);
        peopleRepository.save(creator); 

        Event event1 = new Event();
        event1.setSchoolId(1L);
        event1.setClassId(2L);
        event1.setCreatedBy(creator.getId());  
        event1.setTitle("Test Event");
        event1.setDuration(10);
        event1.setEventType(Event.EventType.EXAM);
        event1.setStartEvent(LocalDateTime.now().withNano(0));
        eventRepository.save(event1);

        List<Event> events = eventRepository.findByCreatedByAndStartEventBetween(creator.getId(), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(event1.getStartEvent(), events.get(0).getStartEvent());
    }
}
