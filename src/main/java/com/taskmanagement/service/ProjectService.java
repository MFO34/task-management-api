package com.taskmanagement.service;

import com.taskmanagement.dto.*;
import com.taskmanagement.entity.User;

import java.util.List;

public interface ProjectService {
    
    ProjectResponse createProject(CreateProjectRequest request, User currentUser);
    
    ProjectResponse getProjectById(Long projectId, User currentUser);
    
    List<ProjectListResponse> getUserProjects(User currentUser);
    
    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, User currentUser);
    
    void deleteProject(Long projectId, User currentUser);
    
    ProjectResponse addMember(Long projectId, AddMemberRequest request, User currentUser);
    
    void removeMember(Long projectId, Long userId, User currentUser);
    
    List<ProjectMemberResponse> getProjectMembers(Long projectId, User currentUser);
}