package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.javaSrc.peoples.People;
import com.example.demo.javaSrc.tasks.Task;
import com.example.demo.javaSrc.tasks.TaskService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private final TaskService taskService;

    @Autowired
    private final PeopleController userController;

    private final SimpMessagingTemplate messagingTemplate;

    public TaskController(TaskService taskService, PeopleController userController,
            SimpMessagingTemplate messagingTemplate) {
        this.taskService = taskService;
        this.userController = userController;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/tasks/{id}/toggle-complete")
    public ResponseEntity<Void> toggleTask(@PathVariable("id") Long taskId,
            Authentication auth) {
        try {
            Long userId = userController.currentUser(auth).getId();
            taskService.toggleComplete(taskId, userId);

            Task updatedTask = taskService.getTaskById(taskId);
            if (updatedTask != null) {

                messagingTemplate.convertAndSend("/topic/tasks/updated", updatedTask);
                messagingTemplate.convertAndSend("/topic/tasks/" + taskId + "/status", updatedTask);

                if (updatedTask.getSchoolId() != null) {
                    messagingTemplate.convertAndSend("/topic/tasks/school/" + updatedTask.getSchoolId() + "/updated",
                            updatedTask);
                }
                if (updatedTask.getClassId() != null) {
                    messagingTemplate.convertAndSend("/topic/tasks/class/" + updatedTask.getClassId() + "/updated",
                            updatedTask);
                }
                messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/my-tasks/status-changed",
                        updatedTask);
            }

            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(
            @RequestBody Task newTask,
            Authentication auth) {

        People teacher = userController.currentUser(auth);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        newTask.setSchoolId(teacher.getSchoolId());

        Task createdTask = taskService.createTask(newTask);

        messagingTemplate.convertAndSend("/topic/tasks/new", createdTask);
        if (createdTask.getSchoolId() != null) {
            messagingTemplate.convertAndSend("/topic/tasks/school/" + createdTask.getSchoolId() + "/new", createdTask);
        }
        if (createdTask.getClassId() != null) {
            messagingTemplate.convertAndSend("/topic/tasks/class/" + createdTask.getClassId() + "/new", createdTask);
        }

        return ResponseEntity.ok(createdTask);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getTasks(
            Authentication auth,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Boolean onlyFuture) {
        People me = userController.currentUser(auth);
        if (me == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Long schoolId = me.getSchoolId();
        Long userClassId = me.getClassId();

        List<Task> result = new ArrayList<>();

        result.addAll(taskService.getBySchoolAndClass(schoolId, null));

        if (userClassId != null) {
            result.addAll(taskService.getBySchoolAndClass(schoolId, userClassId));
        }

        if (classId != null) {
            result.removeIf(task -> !classId.equals(task.getClassId()));
        }

        if (Boolean.TRUE.equals(onlyFuture)) {
            result.removeIf(task -> task.getDeadline() != null && task.getDeadline().before(new java.util.Date()));
        }

        result.sort(Comparator.comparing(Task::getDeadline));

        return ResponseEntity.ok(result);

    }

}