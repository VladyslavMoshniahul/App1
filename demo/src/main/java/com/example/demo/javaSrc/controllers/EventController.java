package com.example.demo.javaSrc.controllers;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.javaSrc.comments.EventsCommentService;
import com.example.demo.javaSrc.comments.EventCommentDTO;
import com.example.demo.javaSrc.comments.EventsComment;
import com.example.demo.javaSrc.events.Event;
import com.example.demo.javaSrc.events.EventCreateRequest;
import com.example.demo.javaSrc.events.EventFile;
import com.example.demo.javaSrc.events.EventService;
import com.example.demo.javaSrc.invitations.InvitationDTO;
import com.example.demo.javaSrc.invitations.InvitationsService;
import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.peoples.PeopleService;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.SchoolClass;

@RestController
@RequestMapping("/api/event")
public class EventController {
    @Autowired
    private final PeopleController userController;
    @Autowired
    private final EventService eventService;
    @Autowired
    private final PeopleService userService;
    @Autowired
    private final InvitationsService invitationsService;
    @Autowired
    private final EventsCommentService eventsCommentService;
    @Autowired
    private final ClassService classService;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public EventController(PeopleController userController, EventService eventService, PeopleService userService,
            InvitationsService invitationsService,
            EventsCommentService eventsCommentService,
            ClassService classService,
            SimpMessagingTemplate messagingTemplate) {
        this.userController = userController;
        this.eventService = eventService;
        this.userService = userService;
        this.invitationsService = invitationsService;
        this.eventsCommentService = eventsCommentService;
        this.classService = classService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/getEvents")
    public List<Event> getEvents(Authentication auth) {

        People me = userController.currentUser(auth);

        List<InvitationDTO> invitations = invitationsService.getInvitationsForUser(me.getId());
        if (invitations == null) {
            return List.of();
        }
        List<Long> eventIds = invitations.stream()
                .filter(inv -> inv.getType() == InvitationDTO.Type.EVENT)
                .map(InvitationDTO::getEventOrVoteId)
                .collect(Collectors.toList());
        List<Event> events = eventIds.stream()
                .map(eventService::getEventById)
                .collect(Collectors.toList());
        return events;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        if (event == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(event);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR', 'STUDENT')")
    @PostMapping("/events")
    public ResponseEntity<?> createEvent(
            @RequestBody EventCreateRequest req,
            Authentication auth) {
        Long classId;
        Event event = new Event();
        event.setTitle(req.title());
        event.setContent(req.content());
        event.setLocationOrLink(req.locationORlink());
        event.setStartEvent(req.startEvent());
        event.setDuration(req.duration());
        event.setEventType(req.eventType());
        event.setSchoolId(userController.currentUser(auth).getSchoolId());

        SchoolClass schoolClass = classService.getClassesBySchoolIdAndName(
                userController.currentUser(auth).getSchoolId(),
                req.className());
        if (schoolClass == null) {
            event.setClassId(null);
            classId = null;
        } else {
            classId = schoolClass.getId();
            event.setClassId(classId);
        }
        if (userController.currentUser(auth).getRole() == People.Role.STUDENT
                && !Objects.equals(classId, userController.currentUser(auth).getClassId())) {
            return ResponseEntity.status(403).body("Учень може створювати події лише для свого класу");
        }
        if (userController.currentUser(auth).getRole() == People.Role.PARENT) {
            return ResponseEntity.status(403).body("Батьки не можуть створювати події");
        }
        
        event.setCreatedBy(userController.currentUser(auth).getId());

        Event saved = eventService.createEvent(event);

        messagingTemplate.convertAndSend("/topic/events/new", saved);
        if (saved.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/events/school/" + saved.getSchoolId(), saved);
        }
        if (saved.getClassId() != null) {
            messagingTemplate.convertAndSend("/topic/events/class/" + saved.getClassId(), saved);
        }
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{eventId}/upload")
    public ResponseEntity<EventFile> uploadFileToEvent(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file) {

        try {
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }

            EventFile eventFile = new EventFile(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    event);
            EventFile saved = eventService.saveEventFile(eventFile);
            messagingTemplate.convertAndSend("/topic/events/" + eventId + "/files", saved);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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
                userId, keyword);
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
                LocalDateTime.parse(to));
    }

    @GetMapping("/{eventId}/files")
    public ResponseEntity<List<EventFile>> getFilesForEvent(@PathVariable Long eventId) {
        List<EventFile> files = eventService.getFilesForEvent(eventId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/downloadFiles/{eventId}")
    public ResponseEntity<byte[]> downloadFilesZip(@PathVariable Long eventId) throws java.io.IOException {
        List<EventFile> files = eventService.getFilesForEvent(eventId);
        if (files == null || files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            for (EventFile file : files) {
                ZipEntry entry = new ZipEntry(file.getFileName());
                zipOut.putNextEntry(entry);
                zipOut.write(file.getFileData());
                zipOut.closeEntry();
            }
            zipOut.finish();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"event-" + eventId + "-files.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());
        }
    }

    @PostMapping("/writeComments/{eventId}")
    public ResponseEntity<EventsComment> addComment(@RequestBody EventsComment comment,
            @PathVariable Long eventId,
            Authentication auth) {
        EventsComment saved = new EventsComment();
        saved.setEventId(eventId);
        saved.setUserId(userController.currentUser(auth).getId());
        saved = eventsCommentService.createComment(comment);
        messagingTemplate.convertAndSend("/topic/events/" + saved.getEventId() + "/comments/new", saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<EventsComment> getComment(@PathVariable Long id) {
        EventsComment comment = eventsCommentService.getCommentById(id);
        if (comment == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comments/event/{eventId}")
    public List<EventCommentDTO> getCommentsByEvent(@PathVariable Long eventId) {
        List<EventsComment> comments = eventsCommentService.getCommentsByEventId(eventId);

        return comments.stream()
                .map(c -> {
                    People author = userService.getPeopleById(c.getUserId());
                    return new EventCommentDTO(c, author);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/comments/user/{userId}")
    public List<EventsComment> getCommentsByUser(@PathVariable Long userId) {
        return eventsCommentService.getCommentsByUserId(userId);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        Long eventId = null;
        EventsComment comment = eventsCommentService.getCommentById(id);
        if (comment != null && comment.getEventId() != null) {
            eventId = comment.getEventId();
        }
        eventsCommentService.deleteComment(id);
        if (eventId != null) {
            messagingTemplate.convertAndSend("/topic/events/" + eventId + "/comments/deleted",
                    "Коментар ID " + id + " видалено.");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/event/{eventId}")
    public ResponseEntity<Void> deleteCommentsByEvent(@PathVariable Long eventId) {
        eventsCommentService.deleteCommentsByEventId(eventId);
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/comments/allDeleted",
                "Усі коментарі для події ID " + eventId + " видалено.");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/user/{userId}")
    public ResponseEntity<Void> deleteCommentsByUser(@PathVariable Long userId) {
        List<EventsComment> commentsToDelete = eventsCommentService.getCommentsByUserId(userId);
        eventsCommentService.deleteCommentsByUserId(userId);
        commentsToDelete.forEach(comment -> {
            if (comment.getEventId() != null) {
                messagingTemplate.convertAndSend("/topic/events/" + comment.getEventId() + "/comments/deleted",
                        "Коментар ID " + comment.getId() + " користувача " + userId + " видалено.");
            }
        });
        return ResponseEntity.noContent().build();
    }
}
