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

    private void validateProjectAccess(Long projectId, User user) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new ForbiddenException("You don't have access to this project");
        }
    }

    @Override
    public TaskResponse createTask(Long projectId, CreateTaskRequest request, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        validateProjectAccess(projectId, currentUser);
        
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        
        task.setDeadline(request.getDeadline());
        
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + request.getAssigneeId()));
            
            if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId())) {
                throw new ForbiddenException("Cannot assign task to user who is not a project member");
            }
            
            task.setAssignee(assignee);
        }
        
        Task savedTask = taskRepository.save(task);
        
        return mapToTaskResponse(savedTask);
    }

    @Override
    public TaskResponse getTaskById(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        validateProjectAccess(task.getProject().getId(), currentUser);

        return mapToTaskResponse(task);
    }

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
        List<Task> tasks = taskRepository.findTasksByUserId(currentUser.getId());
        
        return tasks.stream()
                .map(this::mapToTaskListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        
        validateProjectAccess(task.getProject().getId(), currentUser);
        
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
        
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + request.getAssigneeId()));
            
            if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), assignee.getId())) {
                throw new ForbiddenException("Cannot assign task to user who is not a project member");
            }
            
            task.setAssignee(assignee);
        }
        
        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        
        validateProjectAccess(task.getProject().getId(), currentUser);
        
        taskRepository.delete(task);
    }

    @Override
    public TaskResponse assignTask(Long taskId, Long assigneeId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));
        
        validateProjectAccess(task.getProject().getId(), currentUser);
        
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + assigneeId));
        
        if (!projectMemberRepository.existsByProjectIdAndUserId(task.getProject().getId(), assignee.getId())) {
            throw new ForbiddenException("Cannot assign task to user who is not a project member");
        }
        
        task.setAssignee(assignee);
        Task updatedTask = taskRepository.save(task);
        
        return mapToTaskResponse(updatedTask);
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
        
        List<Task> tasks = taskRepository.findOverdueTasks(projectId);
        
        return tasks.stream()
                .map(this::mapToTaskListResponse)
                .collect(Collectors.toList());
    }

    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse.ProjectSummary projectSummary = TaskResponse.ProjectSummary.builder()
                .id(task.getProject().getId())
                .name(task.getProject().getName())
                .build();
        
        TaskResponse.UserSummary assigneeSummary = null;
        if (task.getAssignee() != null) {
            assigneeSummary = TaskResponse.UserSummary.builder()
                    .id(task.getAssignee().getId())
                    .email(task.getAssignee().getEmail())
                    .fullName(task.getAssignee().getFullName())
                    .build();
        }
        
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

    private TaskListResponse mapToTaskListResponse(Task task) {
        String assigneeName = task.getAssignee() != null 
                ? task.getAssignee().getFullName() 
                : null;
        
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