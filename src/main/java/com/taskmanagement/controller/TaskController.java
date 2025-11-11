package com.taskmanagement.controller;

import com.taskmanagement.dto.*;
import com.taskmanagement.entity.TaskPriority;
import com.taskmanagement.entity.TaskStatus;
import com.taskmanagement.entity.User;
import com.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // 1. CREATE TASK (Project altında)
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskResponse response = taskService.createTask(projectId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. GET PROJECT TASKS
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskListResponse>> getProjectTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getProjectTasks(projectId, currentUser);
        return ResponseEntity.ok(tasks);
    }

    // 3. GET USER'S ALL TASKS (from all projects)
    @GetMapping("/tasks/my-tasks")
    public ResponseEntity<List<TaskListResponse>> getMyTasks(
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getUserTasks(currentUser);
        return ResponseEntity.ok(tasks);
    }

    // 4. GET TASK BY ID
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskResponse response = taskService.getTaskById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    // 5. UPDATE TASK
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskResponse response = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    // 6. DELETE TASK
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // 7. ASSIGN TASK
    @PutMapping("/tasks/{id}/assign/{assigneeId}")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @PathVariable Long assigneeId,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskResponse response = taskService.assignTask(id, assigneeId, currentUser);
        return ResponseEntity.ok(response);
    }

    // 8. FILTER BY STATUS
    @GetMapping("/projects/{projectId}/tasks/status/{status}")
    public ResponseEntity<List<TaskListResponse>> getTasksByStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getTasksByStatus(projectId, status, currentUser);
        return ResponseEntity.ok(tasks);
    }

    // 9. FILTER BY PRIORITY
    @GetMapping("/projects/{projectId}/tasks/priority/{priority}")
    public ResponseEntity<List<TaskListResponse>> getTasksByPriority(
            @PathVariable Long projectId,
            @PathVariable TaskPriority priority,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getTasksByPriority(projectId, priority, currentUser);
        return ResponseEntity.ok(tasks);
    }

    // 10. GET OVERDUE TASKS
    @GetMapping("/projects/{projectId}/tasks/overdue")
    public ResponseEntity<List<TaskListResponse>> getOverdueTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User currentUser
    ) {
        List<TaskListResponse> tasks = taskService.getOverdueTasks(projectId, currentUser);
        return ResponseEntity.ok(tasks);
    }
}