package com.taskmanagement.dto;

import com.taskmanagement.entity.TaskPriority;
import com.taskmanagement.entity.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private Long assigneeId;

    private LocalDateTime deadline;
}