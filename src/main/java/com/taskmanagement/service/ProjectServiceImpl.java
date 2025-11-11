package com.taskmanagement.service;

import com.taskmanagement.dto.*;
import com.taskmanagement.entity.*;
import com.taskmanagement.exception.ForbiddenException;
import com.taskmanagement.exception.ProjectNotFoundException;
import com.taskmanagement.exception.UserAlreadyExistsException;
import com.taskmanagement.repository.ProjectMemberRepository;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request, User currentUser) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(currentUser);
        
        Project savedProject = projectRepository.save(project);
        
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUser(currentUser);
        ownerMember.setRole(ProjectRole.OWNER);
        projectMemberRepository.save(ownerMember);
        
        return mapToProjectResponse(savedProject);
    }

    @Override
    public ProjectResponse getProjectById(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId())) {
            throw new ForbiddenException("You don't have access to this project");
        }

        return mapToProjectResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectListResponse> getUserProjects(User currentUser) {
        List<Project> projects = projectRepository.findProjectsByUserId(currentUser.getId());
        
        return projects.stream()
                .map(this::mapToProjectListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only project owner can update the project");
        }
        
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        
        Project updatedProject = projectRepository.save(project);
        return mapToProjectResponse(updatedProject);
    }

    @Override
    public void deleteProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only project owner can delete the project");
        }
        
        projectRepository.delete(project);
    }

    @Override
    public ProjectResponse addMember(Long projectId, AddMemberRequest request, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only project owner can add members");
        }
        
        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + request.getUserId()));
        
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userToAdd.getId())) {
            throw new UserAlreadyExistsException("User is already a member of this project");
        }
        
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(userToAdd);
        member.setRole(ProjectRole.MEMBER);
        projectMemberRepository.save(member);
        
        return mapToProjectResponse(projectRepository.findById(projectId).get());
    }

    @Override
    public void removeMember(Long projectId, Long userId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only project owner can remove members");
        }
        
        if (userId.equals(project.getOwner().getId())) {
            throw new ForbiddenException("Project owner cannot be removed");
        }
        
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ProjectNotFoundException("Member not found in this project"));
        
        projectMemberRepository.delete(member);
    }

    @Override
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, User currentUser) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId())) {
            throw new ForbiddenException("You don't have access to this project");
        }
        
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        
        return members.stream()
                .map(this::mapToProjectMemberResponse)
                .collect(Collectors.toList());
    }

    // Mapping methods
    private ProjectResponse mapToProjectResponse(Project project) {

        long memberCount = projectMemberRepository.countByProjectId(project.getId());

        ProjectResponse.UserSummary ownerSummary = ProjectResponse.UserSummary.builder()
                .id(project.getOwner().getId())
                .email(project.getOwner().getEmail())
                .fullName(project.getOwner().getFullName())
                .build();
        
        List<ProjectMemberResponse> memberResponses = project.getMembers().stream()
                .map(this::mapToProjectMemberResponse)
                .collect(Collectors.toList());
        
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .owner(ownerSummary)
                .members(memberResponses)
                .memberCount((int) memberCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private ProjectListResponse mapToProjectListResponse(Project project) {
        // Üye sayısını veritabanından doğrudan çekiyoruz
        long memberCount = projectMemberRepository.countByProjectId(project.getId());

        return ProjectListResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerName(project.getOwner().getFullName())
                .memberCount((int) memberCount) // long'u int'e cast ediyoruz
                .createdAt(project.getCreatedAt())
                .build();
    }

    private ProjectMemberResponse mapToProjectMemberResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .userId(member.getUser().getId())
                .email(member.getUser().getEmail())
                .fullName(member.getUser().getFullName())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}