package com.taskmanagement.service;

import com.taskmanagement.dto.*;
import com.taskmanagement.entity.TaskPriority;
import com.taskmanagement.entity.TaskStatus;
import com.taskmanagement.entity.User;

import java.util.List;

public interface TaskService {
    
    TaskResponse createTask(Long projectId, CreateTaskRequest request, User currentUser);
    
    TaskResponse getTaskById(Long taskId, User currentUser);
    
    List<TaskListResponse> getProjectTasks(Long projectId, User currentUser);
    
    List<TaskListResponse> getUserTasks(User currentUser);
    
    TaskResponse updateTask(Long taskId, UpdateTaskRequest request, User currentUser);
    
    void deleteTask(Long taskId, User currentUser);
    
    TaskResponse assignTask(Long taskId, Long assigneeId, User currentUser);
    
    List<TaskListResponse> getTasksByStatus(Long projectId, TaskStatus status, User currentUser);
    
    List<TaskListResponse> getTasksByPriority(Long projectId, TaskPriority priority, User currentUser);
    
    List<TaskListResponse> getOverdueTasks(Long projectId, User currentUser);
}