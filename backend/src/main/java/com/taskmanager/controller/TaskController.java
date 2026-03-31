package com.taskmanager.controller;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    @GetMapping
    public CompletableFuture<ResponseEntity<List<TaskResponse>>> getAllTasks() {
        return taskService.getAllTasks()
                .thenApply(ResponseEntity::ok);
    }
    
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<TaskResponse>> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .thenApply(ResponseEntity::ok);
    }
    
    @PostMapping
    public CompletableFuture<ResponseEntity<TaskResponse>> createTask(@Valid @RequestBody TaskRequest request) {
        return taskService.createTask(request)
                .thenApply(createdTask -> ResponseEntity.status(HttpStatus.CREATED).body(createdTask));
    }
    
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(id, request)
                .thenApply(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }
}
