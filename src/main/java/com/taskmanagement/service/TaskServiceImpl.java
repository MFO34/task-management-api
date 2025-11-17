package com.taskmanagement.service;

import com.taskmanagement.dto.*;
import com.taskmanagement.entity.*;
import com.taskmanagement.exception.ForbiddenException;
import com.taskmanagement.exception.ProjectNotFoundException;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.repository.ProjectMemberRepository;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Validates if the user has access to the project
     * @throws ForbiddenException if user is not a project member
     */
    private void validateProjectAccess(Long projectId, User user) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    /**
     * ✨ YENİ: Helper method to convert Page<Task> to PageResponse<TaskListResponse>
     * 🎯 AMAÇ: Code duplication'ı önlemek
     */
    private PageResponse<TaskListResponse> convertToPageResponse(Page<Task> taskPage) {
        List<TaskListResponse> content = taskPage.getContent().stream()
                .map(this::mapToTaskListResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<TaskListResponse>builder()
                .content(content)
                .pageNumber(taskPage.getNumber())
                .pageSize(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .first(taskPage.isFirst())
                .last(taskPage.isLast())
                .empty(taskPage.isEmpty())
                .build();
    }

    // ========================================
    // CRUD OPERATIONS
    // ========================================

    @Override
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, User currentUser) {
        // 1. Validate project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        // 2. Validate user has access to project
        validateProjectAccess(projectId, currentUser);
        
        // 3. Create task entity
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        
        // 4. Set optional fields (status & priority have defaults in @PrePersist)
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        
        task.setDeadline(request.getDeadline());
        
        // 5. Handle assignee (optional)
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + request.getAssigneeId()));
            
            // Validate assignee is project member
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId())) {
                throw new ForbiddenException("Cannot assign task to user who is not a project member");
            }
            
            task.setAssignee(assignee);
        }
        
        // 6. Save and return
        Task savedTask = taskRepository.save(task);
        return mapToTaskResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, User currentUser) {
        // 1. Fetch task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        // 2. Validate access
        validateProjectAccess(task.getProject().getId(), currentUser);

        // 3. Return response
        return mapToTaskResponse(task);
    }

    @Override
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, User currentUser) {
        // 1. Fetch task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        
        // 2. Validate access
        validateProjectAccess(task.getProject().getId(), currentUser);
        
        // 3. Partial update (only update non-null fields)
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        
        // 4. Handle assignee update
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + request.getAssigneeId()));
            
            // Validate assignee is project member
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), assignee.getId())) {
                throw new ForbiddenException("Cannot assign task to user who is not a project member");
            }
            
            task.setAssignee(assignee);
        }
        
        // 5. Save and return
        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long taskId, User currentUser) {
        // 1. Fetch task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        
        // 2. Validate access
        validateProjectAccess(task.getProject().getId(), currentUser);
        
        // 3. Delete
        taskRepository.delete(task);
    }

    @Override
    public TaskResponse assignTask(Long taskId, Long assigneeId, User currentUser) {
        // 1. Fetch task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        
        // 2. Validate access
        validateProjectAccess(task.getProject().getId(), currentUser);
        
        // 3. Fetch assignee
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + assigneeId));
        
        // 4. Validate assignee is project member
        if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), assignee.getId())) {
            throw new ForbiddenException("Cannot assign task to user who is not a project member");
        }
        
        // 5. Assign and save
        task.setAssignee(assignee);
        Task updatedTask = taskRepository.save(task);
        
        return mapToTaskResponse(updatedTask);
    }

    // ========================================
    // LIST OPERATIONS (Non-Pageable)
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<TaskListResponse> getProjectTasks(Long projectId, User currentUser) {
        validateProjectAccess(projectId, currentUser);
        
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        
        return tasks.stream()
                .map(this::mapToTaskListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskListResponse> getUserTasks(User currentUser) {
        Page<Task> taskPage = taskRepository.findTasksByUserId(currentUser.getId(), Pageable.unpaged());
        List<Task> tasks = taskPage.getContent();
        
        return tasks.stream()
                .map(this::mapToTaskListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskListResponse> getTasksByStatus(Long projectId, TaskStatus status, User currentUser) {
        validateProjectAccess(projectId, currentUser);
        
        List<Task> tasks = taskRepository.findByProjectIdAndStatus(projectId, status);
        
        return tasks.stream()
                .map(this::mapToTaskListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskListResponse> getTasksByPriority(Long projectId, TaskPriority priority, User currentUser) {
        validateProjectAccess(projectId, currentUser);
        
        List<Task> tasks = taskRepository.findByProjectIdAndPriority(projectId, priority);
        
        return tasks.stream()
                .map(this::mapToTaskListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskListResponse> getOverdueTasks(Long projectId, User currentUser) {
        validateProjectAccess(projectId, currentUser);
        
        // Use the pageable repository method with an unpaged Pageable and extract content
        Page<Task> taskPage = taskRepository.findOverdueTasks(projectId, Pageable.unpaged());
        List<Task> tasks = taskPage.getContent();
        
        return tasks.stream()
                .map(this::mapToTaskListResponse)
                .collect(Collectors.toList());
    }

    // ========================================
    // ✨ PAGEABLE OPERATIONS (YENİ & İYİLEŞTİRİLMİŞ!)
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getProjectTasksPageable(Long projectId, User currentUser, Pageable pageable) {
        validateProjectAccess(projectId, currentUser);
        
        Page<Task> taskPage = taskRepository.findByProjectId(projectId, pageable);
        
        // ✨ HELPER METODUNU KULLAN (Code duplication önlendi!)
        return convertToPageResponse(taskPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getUserTasksPageable(User currentUser, Pageable pageable) {
        Page<Task> taskPage = taskRepository.findTasksByUserId(currentUser.getId(), pageable);
        
        // ✨ HELPER METODUNU KULLAN
        return convertToPageResponse(taskPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getTasksByStatusPageable(Long projectId, TaskStatus status, User currentUser, Pageable pageable) {
        validateProjectAccess(projectId, currentUser);
        
        Page<Task> taskPage = taskRepository.findByProjectIdAndStatus(projectId, status, pageable);
        
        // ✨ HELPER METODUNU KULLAN
        return convertToPageResponse(taskPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getTasksByPriorityPageable(Long projectId, TaskPriority priority, User currentUser, Pageable pageable) {
        validateProjectAccess(projectId, currentUser);
        
        Page<Task> taskPage = taskRepository.findByProjectIdAndPriority(projectId, priority, pageable);
        
        // ✨ HELPER METODUNU KULLAN
        return convertToPageResponse(taskPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> getOverdueTasksPageable(Long projectId, User currentUser, Pageable pageable) {
        validateProjectAccess(projectId, currentUser);
        
        Page<Task> taskPage = taskRepository.findOverdueTasks(projectId, pageable);
        
        // ✨ HELPER METODUNU KULLAN
        return convertToPageResponse(taskPage);
    }

    // ========================================
    // ✨ SEARCH & ADVANCED FILTERING
    // ========================================

    /**
     * Advanced search with multiple filters
     * Searches in user's accessible projects only (authorization enforced)
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TaskListResponse> searchTasks(
            User currentUser,
            String keyword,
            TaskStatus status,
            TaskPriority priority,
            Long assigneeId,
            Long projectId,
            Pageable pageable
    ) {
        // Search tasks (authorization handled in query via ProjectMember join)
        Page<Task> taskPage = taskRepository.searchTasks(
            currentUser.getId(),
            keyword,
            status,
            priority,
            assigneeId,
            projectId,
            pageable
        );
        
        // Convert to response
        return convertToPageResponse(taskPage);
    }

    // ========================================
    // MAPPING METHODS
    // ========================================

    /**
     * Maps Task entity to TaskResponse DTO
     */
    private TaskResponse mapToTaskResponse(Task task) {
        // Project summary
        TaskResponse.ProjectSummary projectSummary = TaskResponse.ProjectSummary.builder()
                .id(task.getProject().getId())
                .name(task.getProject().getName())
                .build();
        
        // Assignee summary (nullable)
        TaskResponse.UserSummary assigneeSummary = null;
        if (task.getAssignee() != null) {
            assigneeSummary = TaskResponse.UserSummary.builder()
                    .id(task.getAssignee().getId())
                    .email(task.getAssignee().getEmail())
                    .fullName(task.getAssignee().getFullName())
                    .build();
        }
        
        // Calculate isOverdue
        boolean isOverdue = task.getDeadline() != null 
                && task.getDeadline().isBefore(LocalDateTime.now())
                && task.getStatus() != TaskStatus.DONE;
        
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .deadline(task.getDeadline())
                .isOverdue(isOverdue)
                .project(projectSummary)
                .assignee(assigneeSummary)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    /**
     * Maps Task entity to TaskListResponse DTO (simplified version)
     */
    private TaskListResponse mapToTaskListResponse(Task task) {
        // Assignee name (nullable)
        String assigneeName = task.getAssignee() != null 
                ? task.getAssignee().getFullName() 
                : null;
        
        // Calculate isOverdue
        boolean isOverdue = task.getDeadline() != null 
                && task.getDeadline().isBefore(LocalDateTime.now())
                && task.getStatus() != TaskStatus.DONE;
        
        return TaskListResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .deadline(task.getDeadline())
                .isOverdue(isOverdue)
                .assigneeName(assigneeName)
                .projectName(task.getProject().getName())
                .createdAt(task.getCreatedAt())
                .build();
    }
}