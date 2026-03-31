package com.taskmanager.dto;

import com.taskmanager.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    
    private Long id;
    private String title;
    private String description;
    private Integer status;
    private String statusText;
    private LocalDateTime createdDate;
    private LocalDateTime dueDate;
    
    public static TaskResponse fromEntity(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .statusText(getStatusText(task.getStatus()))
                .createdDate(task.getCreatedDate())
                .dueDate(task.getDueDate())
                .build();
    }
    
    private static String getStatusText(Integer status) {
        if (status == null) {
            return "Unknown";
        }
        return switch (status) {
            case 0 -> "Pending";
            case 1 -> "In Progress";
            case 2 -> "Completed";
            default -> "Unknown";
        };
    }
}
