package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @InjectMocks
    private TaskServiceImpl taskService;
    
    private Task testTask;
    private TaskRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(0)
                .createdDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
        
        testRequest = TaskRequest.builder()
                .title("New Task")
                .description("New Description")
                .status(1)
                .dueDate(LocalDateTime.now().plusDays(5))
                .build();
    }
    
    @Test
    void getAllTasks_ShouldReturnOrderedList() throws ExecutionException, InterruptedException {
        Task task1 = Task.builder()
                .id(1L)
                .title("Task 1")
                .status(0)
                .createdDate(LocalDateTime.now().minusDays(1))
                .build();
        
        Task task2 = Task.builder()
                .id(2L)
                .title("Task 2")
                .status(1)
                .createdDate(LocalDateTime.now())
                .build();
        
        when(taskRepository.findAllByOrderByCreatedDateDesc())
                .thenReturn(Arrays.asList(task2, task1));
        
        CompletableFuture<List<TaskResponse>> future = taskService.getAllTasks();
        List<TaskResponse> result = future.get();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Task 2", result.get(0).getTitle());
        assertEquals("Task 1", result.get(1).getTitle());
        verify(taskRepository, times(1)).findAllByOrderByCreatedDateDesc();
    }
    
    @Test
    void getTaskById_ShouldReturnTask() throws ExecutionException, InterruptedException {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        
        CompletableFuture<TaskResponse> future = taskService.getTaskById(1L);
        TaskResponse result = future.get();
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Pending", result.getStatusText());
        verify(taskRepository, times(1)).findById(1L);
    }
    
    @Test
    void getTaskById_ShouldThrowTaskNotFoundException_WhenTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        CompletableFuture<TaskResponse> future = taskService.getTaskById(999L);
        
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof TaskNotFoundException);
        assertEquals("Task not found with id: 999", exception.getCause().getMessage());
        verify(taskRepository, times(1)).findById(999L);
    }
    
    @Test
    void createTask_ShouldSetDefaultsAndSave() throws ExecutionException, InterruptedException {
        TaskRequest request = TaskRequest.builder()
                .title("New Task")
                .description("Description")
                .build();
        
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        
        CompletableFuture<TaskResponse> future = taskService.createTask(request);
        TaskResponse result = future.get();
        
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        assertEquals(0, result.getStatus());
        assertEquals("Pending", result.getStatusText());
        assertNotNull(result.getCreatedDate());
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    
    @Test
    void updateTask_ShouldUpdateFieldsCorrectly() throws ExecutionException, InterruptedException {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        CompletableFuture<TaskResponse> future = taskService.updateTask(1L, testRequest);
        TaskResponse result = future.get();
        
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(1, result.getStatus());
        assertEquals("In Progress", result.getStatusText());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    
    @Test
    void updateTask_ShouldThrowTaskNotFoundException_WhenTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        CompletableFuture<TaskResponse> future = taskService.updateTask(999L, testRequest);
        
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof TaskNotFoundException);
        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    void deleteTask_ShouldDeleteExistingTask() throws ExecutionException, InterruptedException {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).delete(any(Task.class));
        
        CompletableFuture<Void> future = taskService.deleteTask(1L);
        future.get();
        
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).delete(testTask);
    }
    
    @Test
    void deleteTask_ShouldThrowTaskNotFoundException_WhenTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        CompletableFuture<Void> future = taskService.deleteTask(999L);
        
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof TaskNotFoundException);
        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
