package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    public TaskController(TaskService taskService, UserController userController) {
        this.taskService = taskService;
          this.userController = userController;
    }


   @PostMapping("/tasks/{id}/toggle-complete")
    public ResponseEntity<Void> toggleTask(@PathVariable Long taskId,
                                            Authentication auth) {
        try {
            taskService.toggleComplete(taskId, userController.currentUser(auth).getId());
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

        User teacher = userController.currentUser(auth);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        newTask.setSchoolId(teacher.getSchoolId());

        return ResponseEntity.ok(taskService.createTask(newTask));
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

        if (classId != null) {
            result.removeIf(task -> !classId.equals(task.getClassId()));
        }

        if (eventId != null) {
            result.removeIf(task -> !eventId.equals(task.getEvent().getId()));
        }

        if (Boolean.TRUE.equals(onlyFuture)) {
            result.removeIf(task -> task.getDeadline().before(new java.util.Date()));
        }

        result.sort(Comparator.comparing(Task::getDeadline));

        return ResponseEntity.ok(result);
    }

}
