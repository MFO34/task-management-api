package com.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    
    // Project Statistics
    private Long totalProjects;
    private Long projectsIOwn;
    private Long projectsAsMember;
    
    // Task Statistics (All)
    private Long totalTasks;
    private Long tasksAssignedToMe;
    private Long unassignedTasks;
    
    // Task Statistics by Status
    private Long todoTasks;
    private Long inProgressTasks;
    private Long reviewTasks;
    private Long doneTasks;
    
    // Task Statistics by Priority
    private Long lowPriorityTasks;
    private Long mediumPriorityTasks;
    private Long highPriorityTasks;
    private Long criticalTasks;
    
    // Task Statistics by Deadline
    private Long overdueTasks;
    private Long dueTodayTasks;
    private Long dueThisWeekTasks;
    
    // Completion Rate
    private Double completionRate;  // (done / total) * 100
}