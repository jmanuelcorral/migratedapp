package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.repository.TaskRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {
    
    private final TaskRepository taskRepository;
    
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    @Async
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(tasks);
    }
    
    @Async
    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<TaskResponse> getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return CompletableFuture.completedFuture(TaskResponse.fromEntity(task));
    }
    
    @Async
    @Override
    @Transactional
    public CompletableFuture<TaskResponse> createTask(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : 0)
                .createdDate(LocalDateTime.now())
                .dueDate(request.getDueDate())
                .build();
        
        Task savedTask = taskRepository.save(task);
        return CompletableFuture.completedFuture(TaskResponse.fromEntity(savedTask));
    }
    
    @Async
    @Override
    @Transactional
    public CompletableFuture<TaskResponse> updateTask(Long id, TaskRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
        existingTask.setTitle(request.getTitle());
        existingTask.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            existingTask.setStatus(request.getStatus());
        }
        existingTask.setDueDate(request.getDueDate());
        
        Task updatedTask = taskRepository.save(existingTask);
        return CompletableFuture.completedFuture(TaskResponse.fromEntity(updatedTask));
    }
    
    @Async
    @Override
    @Transactional
    public CompletableFuture<Void> deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.delete(task);
        return CompletableFuture.completedFuture(null);
    }
}
