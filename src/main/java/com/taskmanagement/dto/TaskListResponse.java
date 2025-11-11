package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListResponse {

    private Long id;
    private String title;
    private String status;
    private String priority;
    private LocalDateTime deadline;
    private Boolean isOverdue;
    private String assigneeName;  // Sadece isim
    private String projectName;   // Sadece proje adı
    private LocalDateTime createdAt;
}