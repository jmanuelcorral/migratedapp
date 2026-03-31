package com.taskmanager.controller;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
@Tag(name = "Tasks", description = "Task management CRUD operations")
public class TaskController {
    
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    @GetMapping
    @Operation(summary = "Get all tasks", description = "Returns all tasks ordered by creation date descending")
    public CompletableFuture<ResponseEntity<List<TaskResponse>>> getAllTasks() {
        return taskService.getAllTasks()
                .thenApply(ResponseEntity::ok);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Returns a single task by its ID")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public CompletableFuture<ResponseEntity<TaskResponse>> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        return taskService.getTaskById(id)
                .thenApply(ResponseEntity::ok);
    }
    
    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a task with auto-generated ID and creation date")
    @ApiResponse(responseCode = "201", description = "Task created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public CompletableFuture<ResponseEntity<TaskResponse>> createTask(@Valid @RequestBody TaskRequest request) {
        return taskService.createTask(request)
                .thenApply(createdTask -> ResponseEntity.status(HttpStatus.CREATED).body(createdTask));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task", description = "Updates title, description, status and due date")
    @ApiResponse(responseCode = "200", description = "Task updated")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public CompletableFuture<ResponseEntity<TaskResponse>> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(id, request)
                .thenApply(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Deletes a task by ID")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public CompletableFuture<ResponseEntity<Void>> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        return taskService.deleteTask(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }
}
