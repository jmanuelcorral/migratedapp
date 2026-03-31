package com.taskmanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Min(value = 0, message = "Status must be 0 (Pending), 1 (In Progress), or 2 (Completed)")
    @Max(value = 2, message = "Status must be 0 (Pending), 1 (In Progress), or 2 (Completed)")
    private Integer status;
    
    private LocalDateTime dueDate;
}
