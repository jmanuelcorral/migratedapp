package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TaskService taskService;
    
    private ObjectMapper objectMapper;
    private TaskResponse testResponse;
    private TaskRequest testRequest;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        testResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(0)
                .statusText("Pending")
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
    void getAllTasks_ShouldReturn200WithList() throws Exception {
        TaskResponse task1 = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .status(0)
                .statusText("Pending")
                .createdDate(LocalDateTime.now())
                .build();
        
        TaskResponse task2 = TaskResponse.builder()
                .id(2L)
                .title("Task 2")
                .status(1)
                .statusText("In Progress")
                .createdDate(LocalDateTime.now())
                .build();
        
        List<TaskResponse> tasks = Arrays.asList(task1, task2);
        when(taskService.getAllTasks()).thenReturn(CompletableFuture.completedFuture(tasks));
        
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Task 2")));
    }
    
    @Test
    void getTaskById_ShouldReturn200() throws Exception {
        when(taskService.getTaskById(1L))
                .thenReturn(CompletableFuture.completedFuture(testResponse));
        
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")))
                .andExpect(jsonPath("$.statusText", is("Pending")));
    }
    
    @Test
    void getTaskById_ShouldReturn404ForMissingTask() throws Exception {
        CompletableFuture<TaskResponse> future = new CompletableFuture<>();
        future.completeExceptionally(new TaskNotFoundException(999L));
        
        when(taskService.getTaskById(999L)).thenReturn(future);
        
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Task not found with id: 999")));
    }
    
    @Test
    void createTask_WithValidBody_ShouldReturn201() throws Exception {
        TaskResponse createdResponse = TaskResponse.builder()
                .id(1L)
                .title("New Task")
                .description("New Description")
                .status(1)
                .statusText("In Progress")
                .createdDate(LocalDateTime.now())
                .build();
        
        when(taskService.createTask(any(TaskRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(createdResponse));
        
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("New Task")))
                .andExpect(jsonPath("$.statusText", is("In Progress")));
    }
    
    @Test
    void createTask_WithMissingTitle_ShouldReturn400() throws Exception {
        TaskRequest invalidRequest = TaskRequest.builder()
                .description("Description without title")
                .status(0)
                .build();
        
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }
    
    @Test
    void updateTask_ShouldReturn200() throws Exception {
        TaskResponse updatedResponse = TaskResponse.builder()
                .id(1L)
                .title("Updated Task")
                .description("Updated Description")
                .status(2)
                .statusText("Completed")
                .createdDate(LocalDateTime.now().minusDays(1))
                .build();
        
        when(taskService.updateTask(eq(1L), any(TaskRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(updatedResponse));
        
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.statusText", is("Completed")));
    }
    
    @Test
    void deleteTask_ShouldReturn204() throws Exception {
        when(taskService.deleteTask(1L))
                .thenReturn(CompletableFuture.completedFuture(null));
        
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }
}
