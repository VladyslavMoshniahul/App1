package com.example.demo.javaSrc.controllers;

import java.util.ArrayList;
import java.util.List;

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

        if (newTask.getSchoolId() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(taskService.createTask(newTask));
    }
        

    @GetMapping("/tasks")
    public List<Task> getTasks(
            Authentication auth,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId) {

        User me = userController.currentUser(auth);
        Long sch = schoolId != null ? schoolId : me.getSchoolId();
        Long cls = classId != null ? classId : me.getClassId();

        List<Task> tasksForClass = taskService.getBySchoolAndClass(sch, cls);
        List<Task> tasksForAll = taskService.getBySchoolAndClass(sch, null);

        if (classId == null) {
            return tasksForAll;
        } else {
            List<Task> result = new ArrayList<>(tasksForAll);
            result.addAll(tasksForClass);
            return result;
        }
    }
}
