package com.example.demo.javaSrc.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private final TaskRepository taskRepository;
    @Autowired
    private final UserTaskStatusRepository userTaskStatusRepository;

    public TaskService(TaskRepository taskRepository, UserTaskStatusRepository userTaskStatusRepository) {
        this.taskRepository = taskRepository;
        this.userTaskStatusRepository = userTaskStatusRepository;
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
    public void toggleComplete(Long taskId, Long userId) {
        UserTaskStatus status = userTaskStatusRepository
                .findByUserIdAndTaskId(userId, taskId)
                .orElseThrow(() -> new RuntimeException("Status not found"));

        status.setIsCompleted(!status.getIsCompleted());
        LocalDateTime now = LocalDateTime.now();
        status.setCompletedAt(status.getIsCompleted() ? now : null);
        userTaskStatusRepository.save(status);
    }

    public List<Task> getTasksForSchool(Long schoolId) {
        return taskRepository.findBySchoolId(schoolId);
    }

    public List<Task> getTasksForClass(Long schoolId, Long classId) {
        return taskRepository.findBySchoolIdAndClassId(schoolId, classId);
    }

    public List<Task> searchByKeyword(Long schoolId, Long classId, String keyword) {
        if (classId == null) {
            List<Task> byTitle = taskRepository.findBySchoolIdAndTitleContainingIgnoreCase(schoolId, keyword);
            List<Task> byContent = taskRepository.findBySchoolIdAndContentContainingIgnoreCase(schoolId, keyword);
            byContent.removeAll(byTitle);
            byTitle.addAll(byContent);
            return byTitle;
        } else {
            List<Task> byTitle = taskRepository.findBySchoolIdAndClassIdAndTitleContainingIgnoreCase(schoolId, classId,
                    keyword);
            List<Task> byContent = taskRepository.findBySchoolIdAndClassIdAndContentContainingIgnoreCase(schoolId,
                    classId, keyword);
            byContent.removeAll(byTitle);
            byTitle.addAll(byContent);
            return byTitle;
        }
    }

    public List<Task> getTasksForUser(Long userId) {
        return userTaskStatusRepository.findByUserId(userId).stream()
                .map(UserTaskStatus::getTaskId)
                .map(taskId -> taskRepository.findById(taskId).orElse(null))
                .filter(task -> task != null)
                .collect(Collectors.toList());
    }

}