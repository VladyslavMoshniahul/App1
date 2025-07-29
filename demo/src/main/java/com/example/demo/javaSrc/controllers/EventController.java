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
import com.example.demo.javaSrc.tasks.Task;
import com.example.demo.javaSrc.tasks.TaskRepository;

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
    private final TaskRepository taskRepository;
    @Autowired
    private final EventsCommentService eventsCommentService;
    @Autowired
    private final ClassService classService;

    public EventController(PeopleController userController, EventService eventService, PeopleService userService,
            InvitationsService invitationsService,
            TaskRepository taskRepository, EventsCommentService eventsCommentService, ClassService classService) {
        this.userController = userController;
        this.eventService = eventService;
        this.userService = userService;
        this.invitationsService = invitationsService;
        this.taskRepository = taskRepository;
        this.eventsCommentService = eventsCommentService;
        this.classService = classService;
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
            return ResponseEntity.ok(saved);
        } catch (Exception event) {
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

    @GetMapping("/file/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        EventFile file = eventService.getEventFileById(fileId);
        if (file == null)
            return ResponseEntity.notFound().build();

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
