package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsResponse {
    
    private Long projectId;
    private String projectName;
    
    // Task Counts
    private Long totalTasks;
    private Long completedTasks;
    private Long pendingTasks;  // TODO + IN_PROGRESS + REVIEW
    private Long overdueTasks;
    
    // Status Distribution
    private Long todoTasks;
    private Long inProgressTasks;
    private Long reviewTasks;
    private Long doneTasks;
    
    // Priority Distribution
    private Long lowPriorityTasks;
    private Long mediumPriorityTasks;
    private Long highPriorityTasks;
    private Long criticalTasks;
    
    // Team Stats
    private Long totalMembers;
    private Long activeTasks;  // Tasks with recent updates
    
    // Completion Rate
    private Double completionRate;  // (done / total) * 100
    
    // Progress Indicator
    private String status;  // "ON_TRACK", "AT_RISK", "DELAYED"
}