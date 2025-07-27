package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Додано імпорт
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.tasks.Task;
import com.example.demo.javaSrc.tasks.TaskService;
import com.example.demo.javaSrc.users.User;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private final TaskService taskService;

    @Autowired
    private final UserController userController;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate; // Додано SimpMessagingTemplate

    public TaskController(TaskService taskService, UserController userController, SimpMessagingTemplate messagingTemplate) {
        this.taskService = taskService;
        this.userController = userController;
        this.messagingTemplate = messagingTemplate; // Ініціалізація
    }

    @PostMapping("/tasks/{taskId}/toggle-complete") // Виправлено назву змінної шляху з id на taskId
    public ResponseEntity<Void> toggleTask(@PathVariable Long taskId,
                                           Authentication auth) {
        try {
            User currentUser = userController.currentUser(auth);
            if (currentUser == null) {
                // --- WebSocket Integration ---
                messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Unauthorized access.");
                // --- End WebSocket Integration ---
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            taskService.toggleComplete(taskId, currentUser.getId());

            // --- WebSocket Integration ---
            // Сповіщаємо про зміну статусу завдання
            Task updatedTask = taskService.getTaskById(taskId); // Отримайте оновлене завдання
            if (updatedTask != null) {
               
                // Надсилаємо оновлення для класу/школи, якщо це завдання для всіх
                if (updatedTask.getClassId() != null) {
                    messagingTemplate.convertAndSend("/topic/class/" + updatedTask.getClassId() + "/tasks/updates", updatedTask);
                } else if (updatedTask.getSchoolId() != null) {
                    messagingTemplate.convertAndSend("/topic/school/" + updatedTask.getSchoolId() + "/tasks/updates", updatedTask);
                }
            }
            // --- End WebSocket Integration ---

            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/errors", "Task not found with ID: " + taskId);
            // --- End WebSocket Integration ---
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSendToUser(userController.currentUser(auth).getId().toString(), "/queue/errors", "Error toggling task: " + e.getMessage());
            // --- End WebSocket Integration ---
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(
            @RequestBody Task newTask,
            Authentication auth) {

        User teacher = userController.currentUser(auth);
        if (teacher == null) {
            // --- WebSocket Integration ---
            messagingTemplate.convertAndSendToUser(auth.getName(), "/queue/errors", "Unauthorized access.");
            // --- End WebSocket Integration ---
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        newTask.setSchoolId(teacher.getSchoolId());
        Task createdTask = taskService.createTask(newTask);

        // --- WebSocket Integration ---
        // Сповіщаємо про створення нового завдання
        if (createdTask.getClassId() != null) {
            messagingTemplate.convertAndSend("/topic/class/" + createdTask.getClassId() + "/tasks/new", createdTask);
        } else if (createdTask.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/school/" + createdTask.getSchoolId() + "/tasks/new", createdTask);
        }


       
        // --- End WebSocket Integration ---

        return ResponseEntity.ok(createdTask);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getTasks(
            Authentication auth,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Boolean onlyFuture
    ) {
        User me = userController.currentUser(auth);
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Long schoolId = me.getSchoolId();
        Long userClassId = me.getClassId();

        List<Task> result = new ArrayList<>();

        result.addAll(taskService.getBySchoolAndClass(schoolId, null));

        if (userClassId != null) {
            result.addAll(taskService.getBySchoolAndClass(schoolId, userClassId));
        }

        // Додаємо завдання, призначені безпосередньо цьому користувачу
        List<Task> assignedTasks = taskService.getTasksForUser(me.getId());
        result.addAll(assignedTasks);


        if (classId != null) {
            result.removeIf(task -> !classId.equals(task.getClassId()));
        }

        if (eventId != null) {
            // Фільтруємо за Event ID, якщо він є і завдання має прив'язку до події
            result.removeIf(task -> task.getEvent() == null || !eventId.equals(task.getEvent().getId()));
        }

        if (Boolean.TRUE.equals(onlyFuture)) {
            result.removeIf(task -> task.getDeadline() != null && task.getDeadline().before(new java.util.Date()));
        }

        result.sort(Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))); // Обробка null-значень для дедлайну

        // --- WebSocket Integration (Optional for GET methods) ---
        // messagingTemplate.convertAndSendToUser(me.getId().toString(), "/queue/myTasks", result);
        // --- End WebSocket Integration ---

        return ResponseEntity.ok(result);
    }

}