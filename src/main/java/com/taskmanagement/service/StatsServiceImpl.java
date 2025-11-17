package com.taskmanagement.service;

import com.taskmanagement.dto.DashboardStatsResponse;
import com.taskmanagement.dto.ProjectStatsResponse;
import com.taskmanagement.dto.UserStatsResponse;
import com.taskmanagement.entity.*;
import com.taskmanagement.exception.ForbiddenException;
import com.taskmanagement.exception.ProjectNotFoundException;
import com.taskmanagement.repository.ProjectMemberRepository;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardStatsResponse getDashboardStats(User currentUser) {
        Long userId = currentUser.getId();
        
        // Project Stats
        Long totalProjects = (long) projectRepository.findProjectsByUserId(userId).size();
        Long projectsIOwn = projectRepository.countByOwnerId(userId);
        Long projectsAsMember = totalProjects - projectsIOwn;
        
        // Task Stats (Total)
        Long totalTasks = taskRepository.countTotalTasksByUser(userId);
        Long tasksAssignedToMe = taskRepository.countByAssigneeId(userId);
        Long unassignedTasks = taskRepository.countUnassignedTasksByUser(userId);
        
        // Task Stats by Status
        Long todoTasks = taskRepository.countTasksByUserAndStatus(userId, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countTasksByUserAndStatus(userId, TaskStatus.IN_PROGRESS);
        Long reviewTasks = taskRepository.countTasksByUserAndStatus(userId, TaskStatus.REVIEW);
        Long doneTasks = taskRepository.countTasksByUserAndStatus(userId, TaskStatus.DONE);
        
        // Task Stats by Priority
        Long lowPriorityTasks = taskRepository.countTasksByUserAndPriority(userId, TaskPriority.LOW);
        Long mediumPriorityTasks = taskRepository.countTasksByUserAndPriority(userId, TaskPriority.MEDIUM);
        Long highPriorityTasks = taskRepository.countTasksByUserAndPriority(userId, TaskPriority.HIGH);
        Long criticalTasks = taskRepository.countTasksByUserAndPriority(userId, TaskPriority.CRITICAL);
        
        // Task Stats by Deadline
        Long overdueTasks = taskRepository.countOverdueTasksByUser(userId);
        Long dueTodayTasks = taskRepository.countTasksDueTodayByUser(userId);
        Long dueThisWeekTasks = taskRepository.countTasksDueThisWeekByUser(userId);
        
        // Completion Rate
        Double completionRate = totalTasks > 0 
            ? (doneTasks.doubleValue() / totalTasks.doubleValue()) * 100 
            : 0.0;
        
        return DashboardStatsResponse.builder()
                .totalProjects(totalProjects)
                .projectsIOwn(projectsIOwn)
                .projectsAsMember(projectsAsMember)
                .totalTasks(totalTasks)
                .tasksAssignedToMe(tasksAssignedToMe)
                .unassignedTasks(unassignedTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .reviewTasks(reviewTasks)
                .doneTasks(doneTasks)
                .lowPriorityTasks(lowPriorityTasks)
                .mediumPriorityTasks(mediumPriorityTasks)
                .highPriorityTasks(highPriorityTasks)
                .criticalTasks(criticalTasks)
                .overdueTasks(overdueTasks)
                .dueTodayTasks(dueTodayTasks)
                .dueThisWeekTasks(dueThisWeekTasks)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)  // 2 decimal places
                .build();
    }

    @Override
    public ProjectStatsResponse getProjectStats(Long projectId, User currentUser) {
        // Validate project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        // Validate user has access
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId())) {
            throw new ForbiddenException("You don't have access to this project");
        }
        
        // Task Counts
        Long totalTasks = taskRepository.countByProjectId(projectId);
        Long completedTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.DONE);
        Long pendingTasks = totalTasks - completedTasks;
        Long overdueTasks = taskRepository.countOverdueTasksByProject(projectId);
        
        // Status Distribution
        Long todoTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.IN_PROGRESS);
        Long reviewTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.REVIEW);
        Long doneTasks = completedTasks;
        
        // Priority Distribution
        Long lowPriorityTasks = taskRepository.countByProjectIdAndPriority(projectId, TaskPriority.LOW);
        Long mediumPriorityTasks = taskRepository.countByProjectIdAndPriority(projectId, TaskPriority.MEDIUM);
        Long highPriorityTasks = taskRepository.countByProjectIdAndPriority(projectId, TaskPriority.HIGH);
        Long criticalTasks = taskRepository.countByProjectIdAndPriority(projectId, TaskPriority.CRITICAL);
        
        // Team Stats
        Long totalMembers = projectMemberRepository.countByProjectId(projectId);
        
        // Completion Rate
        Double completionRate = totalTasks > 0 
            ? (completedTasks.doubleValue() / totalTasks.doubleValue()) * 100 
            : 0.0;
        
        // Project Status
        String status = calculateProjectStatus(completionRate, overdueTasks, totalTasks);
        
        return ProjectStatsResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .reviewTasks(reviewTasks)
                .doneTasks(doneTasks)
                .lowPriorityTasks(lowPriorityTasks)
                .mediumPriorityTasks(mediumPriorityTasks)
                .highPriorityTasks(highPriorityTasks)
                .criticalTasks(criticalTasks)
                .totalMembers(totalMembers)
                .activeTasks(pendingTasks)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .status(status)
                .build();
    }

    @Override
    public List<ProjectStatsResponse> getAllProjectsStats(User currentUser) {
        // Get all user's projects
        List<Project> projects = projectRepository.findProjectsByUserId(currentUser.getId());
        
        // Generate stats for each project
        return projects.stream()
                .map(project -> getProjectStats(project.getId(), currentUser))
                .collect(Collectors.toList());
    }

    @Override
    public UserStatsResponse getUserStats(Long userId, User currentUser) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        
        // Validate current user can see this user's stats
        // (either same user or they work on same projects)
        if (!userId.equals(currentUser.getId())) {
            // Check if they share any project
            boolean shareProject = projectMemberRepository.existsByUserId(userId) 
                && projectMemberRepository.existsByUserId(currentUser.getId());
            
            if (!shareProject) {
                throw new ForbiddenException("You don't have access to this user's statistics");
            }
        }
        
        // Task Assignments
        Long totalAssignedTasks = taskRepository.countByAssigneeId(userId);
        Long completedTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.DONE);
        Long pendingTasks = totalAssignedTasks - completedTasks;
        Long overdueTasks = taskRepository.countOverdueTasksByAssignee(userId);
        
        // Status Breakdown
        Long todoTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        Long reviewTasks = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.REVIEW);
        Long doneTasks = completedTasks;
        
        // Priority Breakdown
        Long criticalTasks = taskRepository.countByAssigneeIdAndPriority(userId, TaskPriority.CRITICAL);
        Long highPriorityTasks = taskRepository.countByAssigneeIdAndPriority(userId, TaskPriority.HIGH);
        
        // Performance Metrics
        Double completionRate = totalAssignedTasks > 0 
            ? (completedTasks.doubleValue() / totalAssignedTasks.doubleValue()) * 100 
            : 0.0;
        
        // On-time rate (completed before deadline)
        Long completedOnTime = taskRepository.countCompletedOnTimeByAssignee(userId);
        Double onTimeRate = completedTasks > 0 
            ? (completedOnTime.doubleValue() / completedTasks.doubleValue()) * 100 
            : 0.0;
        
        // Activity
        Long projectsCount = (long) projectRepository.findProjectsByUserId(userId).size();
        
        return UserStatsResponse.builder()
                .userId(user.getId())
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .totalAssignedTasks(totalAssignedTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .reviewTasks(reviewTasks)
                .doneTasks(doneTasks)
                .criticalTasks(criticalTasks)
                .highPriorityTasks(highPriorityTasks)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .onTimeRate(Math.round(onTimeRate * 100.0) / 100.0)
                .projectsCount(projectsCount)
                .build();
    }

    /**
     * Calculate project status based on metrics
     */
    private String calculateProjectStatus(Double completionRate, Long overdueTasks, Long totalTasks) {
        if (totalTasks == 0) {
            return "NO_TASKS";
        }
        
        if (overdueTasks > totalTasks * 0.3) {  // More than 30% overdue
            return "DELAYED";
        } else if (overdueTasks > 0 || completionRate < 50) {
            return "AT_RISK";
        } else {
            return "ON_TRACK";
        }
    }
}