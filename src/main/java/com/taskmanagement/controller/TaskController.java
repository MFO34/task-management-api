package com.taskmanagement.controller;

import com.taskmanagement.dto.*;
import com.taskmanagement.entity.TaskPriority;
import com.taskmanagement.entity.TaskStatus;
import com.taskmanagement.entity.User;
import com.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ========================================
    // ✨ HELPER METHOD (YENİ!)
    // ========================================

    /**
     * Creates Pageable object with validation
     * @param page Page number (0-indexed)
     * @param size Page size (max 100)
     * @param sortBy Sort field (validated)
     * @param sortDir Sort direction (asc/desc)
     * @return Pageable object
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        // ✅ Validate page size (max 100)
        if (size > 100) {
            size = 100;
        }
        
        // ✅ Validate sort field (prevent SQL injection via invalid field names)
        List<String> validSortFields = Arrays.asList(
            "id", "title", "status", "priority", "deadline", "createdAt", "updatedAt"
        );
        
        if (!validSortFields.contains(sortBy)) {
            sortBy = "createdAt"; // Default
        }
        
        // ✅ Create sort direction
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    // ========================================
    // CRUD OPERATIONS
    // ========================================

    /**
     * Create a new task in a project
     */
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskResponse response = taskService.createTask(projectId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get task by ID
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskResponse response = taskService.getTaskById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing task
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskResponse response = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a task
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign task to a user
     */
    @PutMapping("/tasks/{id}/assign/{assigneeId}")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @PathVariable Long assigneeId,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskResponse response = taskService.assignTask(id, assigneeId, currentUser);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // LIST OPERATIONS (Non-Pageable)
    // ========================================

    /**
     * Get all tasks in a project (non-pageable)
     */
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskListResponse>> getProjectTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getProjectTasks(projectId, currentUser);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get all user's tasks from all projects (non-pageable)
     */
    @GetMapping("/tasks/my-tasks")
    public ResponseEntity<List<TaskListResponse>> getMyTasks(
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getUserTasks(currentUser);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Filter tasks by status (non-pageable)
     */
    @GetMapping("/projects/{projectId}/tasks/status/{status}")
    public ResponseEntity<List<TaskListResponse>> getTasksByStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getTasksByStatus(projectId, status, currentUser);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Filter tasks by priority (non-pageable)
     */
    @GetMapping("/projects/{projectId}/tasks/priority/{priority}")
    public ResponseEntity<List<TaskListResponse>> getTasksByPriority(
            @PathVariable Long projectId,
            @PathVariable TaskPriority priority,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getTasksByPriority(projectId, priority, currentUser);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get overdue tasks (non-pageable)
     */
    @GetMapping("/projects/{projectId}/tasks/overdue")
    public ResponseEntity<List<TaskListResponse>> getOverdueTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getOverdueTasks(projectId, currentUser);
        return ResponseEntity.ok(tasks);
    }

    // ========================================
    // ✨ PAGEABLE OPERATIONS (YENİ & TAM!)
    // ========================================

    /**
     * Get project tasks with pagination and sorting
     * Example: /api/projects/1/tasks/paged?page=0&size=10&sortBy=priority&sortDir=desc
     */
    @GetMapping("/projects/{projectId}/tasks/paged")
    public ResponseEntity<PageResponse<TaskListResponse>> getProjectTasksPaged(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser
    ) {
        // ✨ HELPER METODUNU KULLAN
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        PageResponse<TaskListResponse> response = taskService.getProjectTasksPageable(projectId, currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user's all tasks with pagination and sorting
     * Example: /api/tasks/my-tasks/paged?page=0&size=10&sortBy=deadline&sortDir=asc
     */
    @GetMapping("/tasks/my-tasks/paged")
    public ResponseEntity<PageResponse<TaskListResponse>> getMyTasksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser
    ) {
        // ✨ HELPER METODUNU KULLAN
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        PageResponse<TaskListResponse> response = taskService.getUserTasksPageable(currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * ✨ YENİ: Get tasks by status with pagination
     * Example: /api/projects/1/tasks/status/TODO/paged?page=0&size=10
     */
    @GetMapping("/projects/{projectId}/tasks/status/{status}/paged")
    public ResponseEntity<PageResponse<TaskListResponse>> getTasksByStatusPaged(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        PageResponse<TaskListResponse> response = taskService.getTasksByStatusPageable(projectId, status, currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * ✨ YENİ: Get tasks by priority with pagination
     * Example: /api/projects/1/tasks/priority/HIGH/paged?page=0&size=10
     */
    @GetMapping("/projects/{projectId}/tasks/priority/{priority}/paged")
    public ResponseEntity<PageResponse<TaskListResponse>> getTasksByPriorityPaged(
            @PathVariable Long projectId,
            @PathVariable TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        PageResponse<TaskListResponse> response = taskService.getTasksByPriorityPageable(projectId, priority, currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * ✨ YENİ: Get overdue tasks with pagination
     * Example: /api/projects/1/tasks/overdue/paged?page=0&size=10
     */
    @GetMapping("/projects/{projectId}/tasks/overdue/paged")
    public ResponseEntity<PageResponse<TaskListResponse>> getOverdueTasksPaged(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        PageResponse<TaskListResponse> response = taskService.getOverdueTasksPageable(projectId, currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // ✨ SEARCH & ADVANCED FILTERING (YENİ!)
    // ========================================

    /**
     * Advanced search with multiple filters
     * 
     * Query Parameters:
     * - keyword: Search in title and description (optional)
     * - status: Filter by status (optional)
     * - priority: Filter by priority (optional)
     * - assigneeId: Filter by assignee (optional)
     * - projectId: Filter by project (optional)
     * - page: Page number (default: 0)
     * - size: Page size (default: 10, max: 100)
     * - sortBy: Sort field (default: createdAt)
     * - sortDir: Sort direction (default: desc)
     * 
     * Examples:
     * - Search keyword: /api/tasks/search?keyword=security
     * - Multiple filters: /api/tasks/search?keyword=bug&status=TODO&priority=HIGH
     * - Project specific: /api/tasks/search?projectId=1&status=IN_PROGRESS
     * - Assignee specific: /api/tasks/search?assigneeId=2&priority=CRITICAL
     */
    @GetMapping("/tasks/search")
    public ResponseEntity<PageResponse<TaskListResponse>> searchTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        PageResponse<TaskListResponse> response = taskService.searchTasks(
            currentUser,
            keyword,
            status,
            priority,
            assigneeId,
            projectId,
            pageable
        );
        
        return ResponseEntity.ok(response);
    }
}