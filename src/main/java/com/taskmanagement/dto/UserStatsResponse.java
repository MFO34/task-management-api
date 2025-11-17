package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    
    private Long userId;
    private String userName;
    private String userEmail;
    
    // Task Assignments
    private Long totalAssignedTasks;
    private Long completedTasks;
    private Long pendingTasks;
    private Long overdueTasks;
    
    // Status Breakdown
    private Long todoTasks;
    private Long inProgressTasks;
    private Long reviewTasks;
    private Long doneTasks;
    
    // Priority Breakdown
    private Long criticalTasks;
    private Long highPriorityTasks;
    
    // Performance Metrics
    private Double completionRate;
    private Double onTimeRate;  // Completed before deadline / Total completed
    
    // Activity
    private Long projectsCount;
}