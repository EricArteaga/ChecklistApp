package com.example.checklistapp.task.controller;

import com.example.checklistapp.task.dto.CreateTaskDTO;
import com.example.checklistapp.task.dto.TaskResponseDTO;
import com.example.checklistapp.task.dto.TaskSummaryDTO;
import com.example.checklistapp.task.dto.UpdateTaskDTO;
import com.example.checklistapp.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar Tasks.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@Valid @RequestBody CreateTaskDTO dto) {
        TaskResponseDTO created = taskService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> findById(@PathVariable Integer id) {
        TaskResponseDTO task = taskService.findById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    public ResponseEntity<List<TaskSummaryDTO>> findAll() {
        List<TaskSummaryDTO> tasks = taskService.findAll();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<TaskSummaryDTO>> findByIdUsuario(@PathVariable Integer idUsuario) {
        List<TaskSummaryDTO> tasks = taskService.findByIdUsuario(idUsuario);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/usuario/{idUsuario}/pendientes")
    public ResponseEntity<List<TaskSummaryDTO>> findPendingTasks(@PathVariable Integer idUsuario) {
        List<TaskSummaryDTO> tasks = taskService.findPendingTasks(idUsuario);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/usuario/{idUsuario}/completadas")
    public ResponseEntity<List<TaskSummaryDTO>> findCompletedTasks(@PathVariable Integer idUsuario) {
        List<TaskSummaryDTO> tasks = taskService.findCompletedTasks(idUsuario);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody UpdateTaskDTO dto) {
        TaskResponseDTO updated = taskService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        taskService.delete(id);
    }
}
