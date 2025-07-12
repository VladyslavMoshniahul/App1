package com.example.demo.javaSrc.tasks;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> getBySchoolAndClass(Long schoolId, Long classId) {
        if (classId == null) {
            return taskRepository.findBySchoolId(schoolId);
        }
        return taskRepository.findBySchoolIdAndClassId(schoolId, classId);
    }
    
    public Task updateTask(Long taskId, Task updatedTask) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Task existingTask = optionalTask.get();
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setContent(updatedTask.getContent());
            existingTask.setDeadline(updatedTask.getDeadline());
            return taskRepository.save(existingTask);
        } else {
            throw new RuntimeException("Task with ID " + taskId + " not found.");
        }
    }

    public void deleteTask(Long taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
        } else {
            throw new RuntimeException("Task with ID " + taskId + " not found.");
        }
    }

    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task with ID " + taskId + " not found."));
    }
    @Transactional
    public void toggleComplete(Long id) {
        Task t = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        t.setCompleted(!t.isCompleted());
        taskRepository.save(t);
    }
    public List<Task> getTasksForSchool(Long schoolId) {
        return taskRepository.findBySchoolId(schoolId);
    }
    public List<Task> getTasksForClass(Long schoolId, Long classId) {
        return taskRepository.findBySchoolIdAndClassId(schoolId, classId);
    }
}