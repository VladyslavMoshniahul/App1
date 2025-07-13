package com.example.demo.javaSrc.controllers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.events.Event;
import com.example.demo.javaSrc.events.EventService;
import com.example.demo.javaSrc.users.User;
import com.example.demo.javaSrc.users.UserService;

@RestController
@RequestMapping("/api/event")
public class EventController {
    @Autowired
    private final UserController userController;
    @Autowired
    private final EventService eventService;
    @Autowired
    private final UserService userService;

    public EventController(UserController userController, EventService eventService, UserService userService) {
        this.userController = userController;
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping("/getEvents")
    public List<Event> getEvents(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long userId) {

        User me = userController.currentUser(auth);

        if (userId != null) {
            User target = userService.getAllUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst().orElse(null);
            if (target == null) {
                return List.of();
            }
            Long sch = target.getSchoolId();
            Long cls = target.getClassId();
            List<Event> events = eventService.getEventsForSchool(sch);
            return events.stream()
                .filter(e -> e.getClassId() == null || (cls != null && cls.equals(e.getClassId())))
                .sorted(Comparator.comparing(Event::getStartEvent))
                .collect(Collectors.toList());
        }

        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();
        List<Event> events = eventService.getEventsForSchool(sch);
        return events.stream()
            .filter(e -> e.getClassId() == null || (cls != null && cls.equals(e.getClassId())))
            .sorted(Comparator.comparing(Event::getStartEvent))
            .collect(Collectors.toList());
    }

    



    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(
            @RequestBody Map<String,Object> payload,
            Authentication auth) {

        String title      = (String) payload.get("title");
        String content    = (String) payload.get("content");
        String loc        = (String) payload.get("location_or_link");
        String startRaw   = (String) payload.get("start_event");
        String type       = (String) payload.get("event_type");

        Object durObj     = payload.get("duration");
        int duration = durObj instanceof Number
            ? ((Number) durObj).intValue()
            : Integer.parseInt((String) durObj);

        Long sid = payload.get("schoolId") != null
            ? (payload.get("schoolId") instanceof Number
                ? ((Number) payload.get("schoolId")).longValue()
                : Long.parseLong(payload.get("schoolId").toString()))
            : null; 

        if (sid == null) {
            return ResponseEntity.badRequest().body(null);
        }

        Long cid;
        if (payload.get("classId") == null || payload.get("classId").toString().isBlank()) {
            cid = null;
        } else if (payload.get("classId") instanceof Number) {
            cid = ((Number) payload.get("classId")).longValue();
        } else {
            cid = Long.parseLong(payload.get("classId").toString());
        }

        LocalDateTime startEvent = OffsetDateTime.parse(startRaw)
            .toLocalDateTime();

        Event e = new Event();
        e.setTitle(title);
        e.setContent(content);
        e.setLocationOrLink(loc);
        e.setStartEvent(startEvent);
        e.setDuration(duration);
        e.setEventType(Event.EventType.valueOf(type));
        e.setSchoolId(sid);
        e.setClassId(cid);
        e.setCreatedBy(userController.currentUser(auth).getId());

        Event saved = eventService.createEvent(e);
        return ResponseEntity.ok(saved);
    }

   

    @GetMapping("/future/{userId}")
    public List<Event> getFutureEvents(
            @PathVariable Long userId,
            Authentication auth) {

        return eventService.getFutureEvents(userId);
    }

    @GetMapping("/past/{userId}")
    public List<Event> getPastEvents(
            @PathVariable Long userId,
            Authentication auth) {

        return eventService.getPastEvents(userId);
    }

    @GetMapping("/search/title")
    public List<Event> searchByTitle(
            @RequestParam Long userId,
            @RequestParam String keyword,
            Authentication auth) {

        return eventService.searchByTitle(
            userId, keyword
        );
    }

    @GetMapping("/search/date")
    public List<Event> searchByDateRange(
            @RequestParam Long userId,
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth) {

        return eventService.searchByDateRange(
            userId,
            LocalDateTime.parse(from),
            LocalDateTime.parse(to)
        );
    }
}
