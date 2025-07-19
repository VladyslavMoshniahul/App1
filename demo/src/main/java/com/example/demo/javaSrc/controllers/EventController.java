package com.example.demo.javaSrc.controllers;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.example.demo.javaSrc.comments.EventsComment;
import com.example.demo.javaSrc.events.Event;
import com.example.demo.javaSrc.events.EventFile;
import com.example.demo.javaSrc.events.EventService;
import com.example.demo.javaSrc.invitations.Invitation;
import com.example.demo.javaSrc.invitations.InvitationsService;
import com.example.demo.javaSrc.invitations.UserInvitationStatus;
import com.example.demo.javaSrc.tasks.Task;
import com.example.demo.javaSrc.tasks.TaskRepository;
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
    @Autowired
    private final InvitationsService invitationsService;
    @Autowired
    private final TaskRepository taskRepository;
    @Autowired
    private final EventsCommentService eventsCommentService;

    public EventController(UserController userController, EventService eventService, UserService userService,InvitationsService invitationsService,
                                TaskRepository taskRepository, EventsCommentService eventsCommentService) {
        this.userController = userController;
        this.eventService = eventService;
        this.userService = userService;
        this.invitationsService = invitationsService;
        this.taskRepository = taskRepository;
        this.eventsCommentService = eventsCommentService;
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
        if (event == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(event);
    }


    @PreAuthorize("hasAnyRole('TEACHER', 'DIRECTOR', 'STUDENT')")
    @PostMapping("/events")
    public ResponseEntity<?> createEvent(
            @RequestBody Event req,
            Authentication auth) {

        Event event = new Event();
        event.setTitle(req.getTitle());
        event.setContent(req.getContent());
        event.setLocationOrLink(req.getLocationOrLink());
        event.setStartEvent(req.getStartEvent());
        event.setDuration(req.getDuration());
        event.setEventType(req.getEventType());
        event.setSchoolId(userController.currentUser(auth).getSchoolId());
        Long classId = req.getClassId() != null ? 
                                                        req.getClassId() : userController.currentUser(auth).getClassId();
        if (userController.currentUser(auth).getRole() == User.Role.STUDENT 
                            && !Objects.equals(req.getClassId(), userController.currentUser(auth).getClassId())) {
            return ResponseEntity.status(403).body("Учень може створювати події лише для свого класу");
        }
        event.setClassId(classId);
        event.setCreatedBy(userController.currentUser(auth).getId());

        Event saved = eventService.createEvent(event);
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
                    event
            );
            EventFile saved = eventService.saveEventFile(eventFile);
            return ResponseEntity.ok(saved);
        } catch (Exception event) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{eventId}/files")
    public ResponseEntity<List<EventFile>> getFilesForEvent(@PathVariable Long eventId) {
        List<EventFile> files = eventService.getFilesForEvent(eventId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        EventFile file = eventService.getEventFileById(fileId);
        if (file == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"")
            .contentType(org.springframework.http.MediaType.parseMediaType(file.getFileType()))
            .body(file.getFileData());
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

        User currentUser = userController.currentUser(auth);
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            return ResponseEntity.badRequest().body("Подія не знайдена");
        }

        switch (currentUser.getRole()) {
            case STUDENT -> {
                for (Long id : userIds) {
                    User invited = userService.getUserById(id);
                    if (invited.getRole() != User.Role.STUDENT) {
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

        Invitation invitation = invitationsService.createGroupInvitation( eventId,  currentUser.getId(), userIds);
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
        return ResponseEntity.ok("Відповідь прийнята");
    }

    @PostMapping("/event/{eventId}/task")
    public ResponseEntity<Task> addTaskToEvent(
            @PathVariable Long eventId,
            @RequestBody Task taskData) {

        Event event = eventService.getEventById(eventId);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }

        Task task = new Task();
        task.setTitle(taskData.getTitle());
        task.setContent(taskData.getContent());
        task.setEvent(event);

        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/event/{eventId}/tasks")
    public ResponseEntity<List<Task>> getTasksForEvent(@PathVariable Long eventId) {
        List<Task> tasks = taskRepository.findByEventId(eventId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/comments")
    public ResponseEntity<EventsComment> addComment(@RequestBody EventsComment comment) {
        EventsComment saved = eventsCommentService.createComment(comment);
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
    public List<EventsComment> getCommentsByEvent(@PathVariable Long eventId) {
        return eventsCommentService.getCommentsByEventId(eventId);
    }

    @GetMapping("/comments/user/{userId}")
    public List<EventsComment> getCommentsByUser(@PathVariable Long userId) {
        return eventsCommentService.getCommentsByUserId(userId);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        eventsCommentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/event/{eventId}")
    public ResponseEntity<Void> deleteCommentsByEvent(@PathVariable Long eventId) {
        eventsCommentService.deleteCommentsByEventId(eventId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/user/{userId}")
    public ResponseEntity<Void> deleteCommentsByUser(@PathVariable Long userId) {
        eventsCommentService.deleteCommentsByUserId(userId);
        return ResponseEntity.noContent().build();
    }

}
