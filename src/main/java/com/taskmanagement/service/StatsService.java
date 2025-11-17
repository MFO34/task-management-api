package com.taskmanagement.service;

import com.taskmanagement.dto.DashboardStatsResponse;
import com.taskmanagement.dto.ProjectStatsResponse;
import com.taskmanagement.dto.UserStatsResponse;
import com.taskmanagement.entity.User;

import java.util.List;

public interface StatsService {
    
    /**
     * Get dashboard statistics for current user
     */
    DashboardStatsResponse getDashboardStats(User currentUser);
    
    /**
     * Get statistics for specific project
     */
    ProjectStatsResponse getProjectStats(Long projectId, User currentUser);
    
    /**
     * Get statistics for all user's projects
     */
    List<ProjectStatsResponse> getAllProjectsStats(User currentUser);
    
    /**
     * Get user performance statistics
     */
    UserStatsResponse getUserStats(Long userId, User currentUser);
}