package com.example.demo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.javaSrc.eventsANDtask.Task;
import com.example.demo.javaSrc.eventsANDtask.TaskRepository;

@SpringBootTest
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void testFindTasksBySchoolId() {
        taskRepository.deleteAll();
        Task task = new Task();
        task.setTitle("Math Homework");
        task.setSchoolId(1L);
        task.setClassId(5L);
        task.setDeadline(java.sql.Date.valueOf(LocalDate.parse("2023-10-01")));

        taskRepository.save(task);

        List<Task> result = taskRepository.findBySchoolId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Math Homework");
    }

    @Test
    void testFindTasksBySchoolIdAndClassId() {
        Task task = new Task();
        task.setTitle("Science Project");
        task.setSchoolId(1L);
        task.setClassId(5L);
        task.setDeadline(java.sql.Date.valueOf(LocalDate.parse("2023-10-01")));
        taskRepository.save(task);

        List<Task> result = taskRepository.findBySchoolIdAndClassId(1L, 5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Science Project");
    }

    @Test
    void testFindTaskById() {
        Task task = new Task();
        task.setTitle("History Essay");
        task.setSchoolId(1L);
        task.setClassId(5L);
        task.setDeadline(java.sql.Date.valueOf(LocalDate.parse("2023-10-01")));
        task = taskRepository.save(task);

        Task result = taskRepository.findById(task.getId()).orElse(null);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("History Essay");
    }
}
