package com.example.demo.javaSrc.controllers;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
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
import com.example.demo.javaSrc.invitations.Invitation;
import com.example.demo.javaSrc.invitations.InvitationsService;
import com.example.demo.javaSrc.invitations.UserInvitationStatus;
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
    public List<Event> getEvents(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long userId) {

        People me = userController.currentUser(auth);

        if (userId != null) {
            People target = userService.getAllPeoples().stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst().orElse(null);
            if (target == null) {
                return List.of();
            }
            Long sch = target.getSchoolId();
            Long cls = target.getClassId();
            List<Event> events = eventService.getEventsForSchool(sch);
            return events.stream()
                    .filter(event -> event.getClassId() == null || (cls != null && cls.equals(event.getClassId())))
                    .sorted(Comparator.comparing(Event::getStartEvent))
                    .collect(Collectors.toList());
        }

        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();
        List<Event> events = eventService.getEventsForSchool(sch);
        return events.stream()
                .filter(event -> event.getClassId() == null || (cls != null && cls.equals(event.getClassId())))
                .sorted(Comparator.comparing(Event::getStartEvent))
                .collect(Collectors.toList());
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
        Long classId = schoolClass != null ? schoolClass.getId() : null;
        if (userController.currentUser(auth).getRole() == People.Role.STUDENT
                && !Objects.equals(classId, userController.currentUser(auth).getClassId())) {
            return ResponseEntity.status(403).body("Учень може створювати події лише для свого класу");
        }
        if (schoolClass == null) {
            event.setClassId(null);
        } else {
            event.setClassId(schoolClass.getId());
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

    @GetMapping("/my-invitations")
    public List<UserInvitationStatus> getMyInvitations(
            Authentication auth,
            @RequestParam(required = false) UserInvitationStatus.Status status) {
        Long userId = userController.currentUser(auth).getId();
        if (status != null) {
            return invitationsService.getUserInvitationsByStatus(userId, status);
        }
        return invitationsService.getUserInvitations(userId);
    }

    @PostMapping("/invite")
    public ResponseEntity<?> sendInvitations(
            @RequestBody List<Long> userIds,
            @RequestParam Long eventId,
            Authentication auth) {

        People currentUser = userController.currentUser(auth);
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            return ResponseEntity.badRequest().body("Подія не знайдена");
        }

        switch (currentUser.getRole()) {
            case STUDENT -> {
                for (Long id : userIds) {
                    People invited = userService.getPeopleById(id);
                    if (invited.getRole() != People.Role.STUDENT) {
                        return ResponseEntity.status(403).body("Учень може запросити лише інших учнів");
                    }
                }
            }
            case PARENT -> {
                return ResponseEntity.status(403).body("Батьки не можуть надсилати запрошення");
            }
            case ADMIN -> {
                return ResponseEntity.status(403).body("Адміністратор не може створювати події і запрошення");
            }
            default -> {
                if (event.getCreatedBy() != currentUser.getId()) {
                    return ResponseEntity.status(403).body("Подія не створена цим користувачем");
                }
            }
        }

        Invitation invitation = invitationsService.createGroupInvitation(eventId, currentUser.getId(), userIds);
        userIds.forEach(userId -> {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/invitations/new",
                    invitation);
        });
        messagingTemplate.convertAndSend("/topic/invitations/new", "Нові запрошення створено для події: " + eventId);

        return ResponseEntity.ok(invitation);
    }

    @PostMapping("/respond")
    public ResponseEntity<?> respondToInvitation(
            @RequestParam Long invitationId,
            @RequestParam UserInvitationStatus.Status status,
            Authentication auth) {

        Long userId = userController.currentUser(auth).getId();
        boolean updated = invitationsService.respondToInvitation(invitationId, userId, status);

        if (!updated) {
            return ResponseEntity.status(403).body("Немає доступу до цього запрошення або вже відповіли");
        }
        messagingTemplate.convertAndSend("/topic/invitations/status/update",
                "Запрошення ID " + invitationId + " оновлено для користувача " + userId + " на статус "
                        + status.name());
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/invitations/status",
                "Запрошення ID " + invitationId + " оновлено на статус " + status.name());
        return ResponseEntity.ok("Відповідь прийнята");
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
