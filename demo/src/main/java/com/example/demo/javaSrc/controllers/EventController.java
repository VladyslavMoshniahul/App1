package com.example.demo.javaSrc.controllers;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.demo.javaSrc.comments.EventsComment;
import com.example.demo.javaSrc.events.Event;
import com.example.demo.javaSrc.events.EventCreateRequest;
import com.example.demo.javaSrc.events.EventFile;
import com.example.demo.javaSrc.events.EventService;
import com.example.demo.javaSrc.invitations.Invitation;
import com.example.demo.javaSrc.invitations.InvitationsService;
import com.example.demo.javaSrc.invitations.UserInvitationStatus;
import com.example.demo.javaSrc.school.ClassService;
import com.example.demo.javaSrc.school.SchoolClass;
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
    @Autowired
    private final ClassService classService;
    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public EventController(UserController userController, EventService eventService, UserService userService,
                           InvitationsService invitationsService,
                           TaskRepository taskRepository, EventsCommentService eventsCommentService, ClassService classService,SimpMessagingTemplate messagingTemplate) {
        this.userController = userController;
        this.eventService = eventService;
        this.userService = userService;
        this.invitationsService = invitationsService;
        this.taskRepository = taskRepository;
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

        User me = userController.currentUser(auth);
        List<Event> events;

        if (userId != null) {
            User target = userService.getAllUsers().stream()
                    .filter(u -> u.getId().equals(userId))
                    .findFirst().orElse(null);
            if (target == null) {
                // Додаємо виклик WebSocket для сповіщення про пустий список подій, якщо потрібно
                // messagingTemplate.convertAndSend("/topic/events/user/" + userId + "/empty", "No events found for user " + userId);
                return List.of();
            }
            Long sch = target.getSchoolId();
            Long cls = target.getClassId();
            events = eventService.getEventsForSchool(sch);
            List<Event> filteredEvents = events.stream()
                    .filter(event -> event.getClassId() == null || (cls != null && cls.equals(event.getClassId())))
                    .sorted(Comparator.comparing(Event::getStartEvent))
                    .collect(Collectors.toList());
            // Додаємо виклик WebSocket, наприклад, для оновлення списку подій для користувача
            // messagingTemplate.convertAndSend("/topic/events/user/" + userId, filteredEvents);
            return filteredEvents;
        }

        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();
        events = eventService.getEventsForSchool(sch);
        List<Event> filteredEvents = events.stream()
                .filter(event -> event.getClassId() == null || (cls != null && cls.equals(event.getClassId())))
                .sorted(Comparator.comparing(Event::getStartEvent))
                .collect(Collectors.toList());
        // Додаємо виклик WebSocket, наприклад, для оновлення загального списку подій
        // messagingTemplate.convertAndSend("/topic/events/all", filteredEvents);
        return filteredEvents;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        if (event == null) {
            // Можливо, сповіщення про те, що подію не знайдено
            // messagingTemplate.convertAndSend("/topic/event/" + id + "/notFound", "Event with ID " + id + " not found.");
            return ResponseEntity.notFound().build();
        }
        // Надсилаємо знайдену подію по WebSocket
        // messagingTemplate.convertAndSend("/topic/event/" + id, event);
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
        if (userController.currentUser(auth).getRole() == User.Role.STUDENT
                && !Objects.equals(classId, userController.currentUser(auth).getClassId())) {
            // Надсилаємо помилку по WebSocket, якщо потрібно
            // messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/errors", "Учень може створювати події лише для свого класу");
            return ResponseEntity.status(403).body("Учень може створювати події лише для свого класу");
        }
        if (schoolClass == null) {
            event.setClassId(null);
        } else {
            event.setClassId(schoolClass.getId());
        }
        event.setCreatedBy(userController.currentUser(auth).getId());

        Event saved = eventService.createEvent(event);
        // Сповіщаємо про нову подію
        if (saved.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/school/" + saved.getSchoolId() + "/events", saved);
        }
        if (saved.getClassId() != null) {
            messagingTemplate.convertAndSend("/topic/class/" + saved.getClassId() + "/events", saved);
        } else {
             messagingTemplate.convertAndSend("/topic/events/general", saved);
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
                // Сповіщення про помилку
                // messagingTemplate.convertAndSend("/topic/event/" + eventId + "/fileUploadError", "Event not found for file upload.");
                return ResponseEntity.notFound().build();
            }

            EventFile eventFile = new EventFile(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    event);
            EventFile saved = eventService.saveEventFile(eventFile);
            // Сповіщаємо про новий файл для події
            messagingTemplate.convertAndSend("/topic/event/" + eventId + "/files", saved);
            return ResponseEntity.ok(saved);
        } catch (Exception e) { // Змінено ім'я змінної з 'event' на 'e' для уникнення конфлікту
            // Сповіщення про помилку завантаження
            // messagingTemplate.convertAndSend("/topic/event/" + eventId + "/fileUploadFailed", "File upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/future/{userId}")
    public List<Event> getFutureEvents(
            @PathVariable Long userId,
            Authentication auth) {
        List<Event> futureEvents = eventService.getFutureEvents(userId);
        // Сповіщення про майбутні події для користувача
        // messagingTemplate.convertAndSend("/topic/user/" + userId + "/futureEvents", futureEvents);
        return futureEvents;
    }

    @GetMapping("/past/{userId}")
    public List<Event> getPastEvents(
            @PathVariable Long userId,
            Authentication auth) {
        List<Event> pastEvents = eventService.getPastEvents(userId);
        // Сповіщення про минулі події для користувача
        // messagingTemplate.convertAndSend("/topic/user/" + userId + "/pastEvents", pastEvents);
        return pastEvents;
    }

    @GetMapping("/search/title")
    public List<Event> searchByTitle(
            @RequestParam Long userId,
            @RequestParam String keyword,
            Authentication auth) {
        List<Event> foundEvents = eventService.searchByTitle(
                userId, keyword);
        // Сповіщення про результати пошуку за назвою
        // messagingTemplate.convertAndSend("/topic/user/" + userId + "/searchResults/title", foundEvents);
        return foundEvents;
    }

    @GetMapping("/search/date")
    public List<Event> searchByDateRange(
            @RequestParam Long userId,
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth) {
        List<Event> foundEvents = eventService.searchByDateRange(
                userId,
                LocalDateTime.parse(from),
                LocalDateTime.parse(to));
        // Сповіщення про результати пошуку за діапазоном дат
        // messagingTemplate.convertAndSend("/topic/user/" + userId + "/searchResults/date", foundEvents);
        return foundEvents;
    }

    @GetMapping("/{eventId}/files")
    public ResponseEntity<List<EventFile>> getFilesForEvent(@PathVariable Long eventId) {
        List<EventFile> files = eventService.getFilesForEvent(eventId);
        // Сповіщення про файли для події
        // messagingTemplate.convertAndSend("/topic/event/" + eventId + "/filesList", files);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        EventFile file = eventService.getEventFileById(fileId);
        if (file == null) {
            // Сповіщення, якщо файл не знайдено
            // messagingTemplate.convertAndSend("/topic/file/" + fileId + "/notFound", "File with ID " + fileId + " not found.");
            return ResponseEntity.notFound().build();
        }
        // Можливо, сповіщення про початок завантаження файлу (хоча це зазвичай не робиться через WebSocket)
        // messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/fileDownloadStarted", "File " + file.getFileName() + " download started.");
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
        List<UserInvitationStatus> invitations;
        if (status != null) {
            invitations = invitationsService.getUserInvitationsByStatus(userId, status);
        } else {
            invitations = invitationsService.getUserInvitations(userId);
        }
        // Сповіщення про запрошення користувача
        // messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/myInvitations", invitations);
        return invitations;
    }

    @PostMapping("/invite")
    public ResponseEntity<?> sendInvitations(
            @RequestBody List<Long> userIds,
            @RequestParam Long eventId,
            Authentication auth) {

        User currentUser = userController.currentUser(auth);
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            // Сповіщення про помилку
            // messagingTemplate.convertAndSendToUser(currentUser.getId().toString(), "/queue/errors", "Подія не знайдена");
            return ResponseEntity.badRequest().body("Подія не знайдена");
        }

        switch (currentUser.getRole()) {
            case STUDENT -> {
                for (Long id : userIds) {
                    User invited = userService.getUserById(id);
                    if (invited.getRole() != User.Role.STUDENT) {
                        // Сповіщення про помилку
                        // messagingTemplate.convertAndSendToUser(currentUser.getId().toString(), "/queue/errors", "Учень може запросити лише інших учнів");
                        return ResponseEntity.status(403).body("Учень може запросити лише інших учнів");
                    }
                }
            }
            case PARENT -> {
                // Сповіщення про помилку
                // messagingTemplate.convertAndSendToUser(currentUser.getId().toString(), "/queue/errors", "Батьки не можуть надсилати запрошення");
                return ResponseEntity.status(403).body("Батьки не можуть надсилати запрошення");
            }
            case ADMIN -> {
                // Сповіщення про помилку
                // messagingTemplate.convertAndSendToUser(currentUser.getId().toString(), "/queue/errors", "Адміністратор не може створювати події і запрошення");
                return ResponseEntity.status(403).body("Адміністратор не може створювати події і запрошення");
            }
            default -> {
                if (event.getCreatedBy() != currentUser.getId()) {
                    // Сповіщення про помилку
                    // messagingTemplate.convertAndSendToUser(currentUser.getId().toString(), "/queue/errors", "Подія не створена цим користувачем");
                    return ResponseEntity.status(403).body("Подія не створена цим користувачем");
                }
            }
        }

        Invitation invitation = invitationsService.createGroupInvitation(eventId, currentUser.getId(), userIds);
        // Сповіщаємо про нові запрошення
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/newInvitations", invitation);
        for (Long invitedUserId : userIds) {
             messagingTemplate.convertAndSendToUser(invitedUserId.toString(), "/queue/newInvitation", invitation);
        }
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
            // Сповіщення про помилку
            // messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/errors", "Немає доступу до цього запрошення або вже відповіли");
            return ResponseEntity.status(403).body("Немає доступу до цього запрошення або вже відповіли");
        }
        // Сповіщаємо про оновлення статусу запрошення
        messagingTemplate.convertAndSend("/topic/invitation/" + invitationId + "/status", status.name());
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/myInvitationsStatusUpdate", "Invitation " + invitationId + " updated to " + status.name());
        return ResponseEntity.ok("Відповідь прийнята");
    }

    @PostMapping("/event/{eventId}/task")
    public ResponseEntity<Task> addTaskToEvent(
            @PathVariable Long eventId,
            @RequestBody Task taskData) {

        Event event = eventService.getEventById(eventId);
        if (event == null) {
            // Сповіщення про помилку
            // messagingTemplate.convertAndSend("/topic/event/" + eventId + "/taskCreationError", "Event not found for task creation.");
            return ResponseEntity.notFound().build();
        }

        Task task = new Task();
        task.setTitle(taskData.getTitle());
        task.setContent(taskData.getContent());
        task.setEvent(event);

        Task saved = taskRepository.save(task);
        // Сповіщаємо про нове завдання для події
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/tasks", saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/event/{eventId}/tasks")
    public ResponseEntity<List<Task>> getTasksForEvent(@PathVariable Long eventId) {
        List<Task> tasks = taskRepository.findByEventId(eventId);
        // Сповіщення про список завдань для події
        // messagingTemplate.convertAndSend("/topic/event/" + eventId + "/tasksList", tasks);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/comments")
    public ResponseEntity<EventsComment> addComment(@RequestBody EventsComment comment) {
        EventsComment saved = eventsCommentService.createComment(comment);
        // Сповіщаємо про новий коментар
        if (saved.getEventId() != null) { // Перевірка, чи Event існує
            messagingTemplate.convertAndSend("/topic/event/" + saved.getEventId() + "/comments", saved);
        } else {
            // Якщо Event не встановлено в коментарі, використовуємо EventId напряму, якщо доступно
            messagingTemplate.convertAndSend("/topic/event/" + comment.getEventId() + "/comments", saved);
        }
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<EventsComment> getComment(@PathVariable Long id) {
        EventsComment comment = eventsCommentService.getCommentById(id);
        if (comment == null) {
            // Сповіщення, якщо коментар не знайдено
            // messagingTemplate.convertAndSend("/topic/comment/" + id + "/notFound", "Comment with ID " + id + " not found.");
            return ResponseEntity.notFound().build();
        }
        // Сповіщення про коментар
        // messagingTemplate.convertAndSend("/topic/comment/" + id, comment);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comments/event/{eventId}")
    public List<EventsComment> getCommentsByEvent(@PathVariable Long eventId) {
        List<EventsComment> comments = eventsCommentService.getCommentsByEventId(eventId);
        // Сповіщення про коментарі для події
        // messagingTemplate.convertAndSend("/topic/event/" + eventId + "/commentsList", comments);
        return comments;
    }

    @GetMapping("/comments/user/{userId}")
    public List<EventsComment> getCommentsByUser(@PathVariable Long userId) {
        List<EventsComment> comments = eventsCommentService.getCommentsByUserId(userId);
        // Сповіщення про коментарі від користувача
        // messagingTemplate.convertAndSend("/topic/user/" + userId + "/comments", comments);
        return comments;
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        EventsComment commentToDelete = eventsCommentService.getCommentById(id);
        if (commentToDelete != null) {
            eventsCommentService.deleteComment(id);
            // Сповіщаємо про видалення коментаря
            messagingTemplate.convertAndSend("/topic/event/" + commentToDelete.getEventId() + "/comments/deleted", id);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/event/{eventId}")
    public ResponseEntity<Void> deleteCommentsByEvent(@PathVariable Long eventId) {
        eventsCommentService.deleteCommentsByEventId(eventId);
        // Сповіщаємо про видалення всіх коментарів для події
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/comments/allDeleted", eventId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/user/{userId}")
    public ResponseEntity<Void> deleteCommentsByUser(@PathVariable Long userId) {
        eventsCommentService.deleteCommentsByUserId(userId);
        // Сповіщаємо про видалення всіх коментарів користувача
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/comments/allDeleted", userId);
        return ResponseEntity.noContent().build();
    }
}