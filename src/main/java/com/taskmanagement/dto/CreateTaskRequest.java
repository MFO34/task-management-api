package com.taskmanagement.dto;

import com.taskmanagement.entity.TaskPriority;
import com.taskmanagement.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    private String description;

    private TaskStatus status;  // Opsiyonel, default: TODO

    private TaskPriority priority;  // Opsiyonel, default: MEDIUM

    private Long assigneeId;  // Opsiyonel (null olabilir)

    private LocalDateTime deadline;  // Opsiyonel
}