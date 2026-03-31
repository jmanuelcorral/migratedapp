package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TaskService {
    
    CompletableFuture<List<TaskResponse>> getAllTasks();
    
    CompletableFuture<TaskResponse> getTaskById(Long id);
    
    CompletableFuture<TaskResponse> createTask(TaskRequest request);
    
    CompletableFuture<TaskResponse> updateTask(Long id, TaskRequest request);
    
    CompletableFuture<Void> deleteTask(Long id);
}
