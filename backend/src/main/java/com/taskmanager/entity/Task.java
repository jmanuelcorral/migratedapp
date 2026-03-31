package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "TASKS", schema = "TASKMANAGER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_seq")
    @SequenceGenerator(name = "tasks_seq", sequenceName = "TASKMANAGER.TASKS_SEQ", allocationSize = 1)
    private Long id;
    
    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;
    
    @Column(name = "DESCRIPTION", length = 2000)
    private String description;
    
    @Column(name = "STATUS", nullable = false)
    private Integer status;
    
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "DUE_DATE")
    private LocalDateTime dueDate;
}
