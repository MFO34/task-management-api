package com.taskmanagement.controller;

import com.taskmanagement.dto.DashboardStatsResponse;
import com.taskmanagement.dto.ProjectStatsResponse;
import com.taskmanagement.dto.UserStatsResponse;
import com.taskmanagement.entity.User;
import com.taskmanagement.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * Get dashboard statistics for current user
     * 
     * Returns comprehensive statistics including:
     * - Project counts (total, owned, member)
     * - Task counts (total, assigned, unassigned)
     * - Task breakdown by status and priority
     * - Deadline statistics (overdue, due today, due this week)
     * - Completion rate
     * 
     * Example: GET /api/stats/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @AuthenticationPrincipal User currentUser
    ) {
        DashboardStatsResponse stats = statsService.getDashboardStats(currentUser);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get statistics for specific project
     * 
     * Example: GET /api/stats/projects/1
     */
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectStatsResponse> getProjectStats(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser
    ) {
        ProjectStatsResponse stats = statsService.getProjectStats(projectId, currentUser);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get statistics for all user's projects
     * 
     * Example: GET /api/stats/projects
     */
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectStatsResponse>> getAllProjectsStats(
            @AuthenticationPrincipal User currentUser
    ) {
        List<ProjectStatsResponse> stats = statsService.getAllProjectsStats(currentUser);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get user performance statistics
     * 
     * Example: GET /api/stats/users/2
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserStatsResponse> getUserStats(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        UserStatsResponse stats = statsService.getUserStats(userId, currentUser);
        return ResponseEntity.ok(stats);
    }
}