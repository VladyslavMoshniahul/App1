package com.example.demo;


import com.example.demo.javaSrc.tasks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTasks() {
        Task task1 = new Task();
        Task task2 = new Task();
        task1.setTitle("Math Homework");
        task2.setTitle("Science Project");

        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));

        List<Task> result = taskService.getAllTasks();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getTitle()).isEqualTo("Math Homework");
        assertThat(result.get(1).getTitle()).isEqualTo("Science Project");
    }

    @Test
    void testCreateTask() {
        Task task = new Task();
        task.setTitle("Science");

        when(taskRepository.save(task)).thenReturn(task);

        Task created = taskService.createTask(task);

        assertThat(created.getTitle()).isEqualTo("Science");
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testGetBySchoolAndClass() {
        Long schoolId = 1L;
        Long classId = 2L;
        Task task1 = new Task();
        task1.setTitle("Math Homework");
        task1.setSchoolId(schoolId);
        task1.setClassId(classId);

        when(taskRepository.findBySchoolIdAndClassId(schoolId, classId)).thenReturn(List.of(task1));

        List<Task> result = taskService.getBySchoolAndClass(schoolId, classId);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getTitle()).isEqualTo("Math Homework");

    }

    @Test
    void testUpdateTask() {
        Task existingTask = new Task();
        Long taskId = existingTask.getId();
        existingTask.setTitle("Old Title");
        existingTask.setDeadline(java.sql.Date.valueOf(LocalDate.parse("2023-10-01")));


        Task updatedTask = new Task();
        updatedTask.setTitle("New Title");
        updatedTask.setDeadline(java.sql.Date.valueOf(LocalDate.parse("2023-10-01")));

        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task result = taskService.updateTask(taskId, updatedTask);

        assertThat(result.getTitle()).isEqualTo("New Title");
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    void testDeleteTask() {
        Task task = new Task();
        Long taskId = task.getId();

        when(taskRepository.existsById(taskId)).thenReturn(true);

        taskService.deleteTask(taskId);

        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void testGetTaskById() {
        Task task = new Task();
        Long taskId = task.getId();
        task.setTitle("Task Title");

        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.of(task));
        Task result = taskService.getTaskById(taskId);
        assertThat(result.getTitle()).isEqualTo("Task Title");
    }

    @Test
    void testToggleComplete() {
        Task task = new Task();
        Long taskId = task.getId();
        task.setCompleted(false);

        when(taskRepository.findById(taskId)).thenReturn(java.util.Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.toggleComplete(taskId);

        assertThat(task.isCompleted()).isTrue();
        verify(taskRepository, times(1)).save(task);
    }
}