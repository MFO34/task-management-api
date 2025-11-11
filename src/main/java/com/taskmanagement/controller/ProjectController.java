package com.taskmanagement.controller;

import com.taskmanagement.dto.*;
import com.taskmanagement.entity.User;
import com.taskmanagement.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 1. CREATE PROJECT
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ProjectResponse response = projectService.createProject(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. GET USER'S PROJECTS
    @GetMapping
    public ResponseEntity<List<ProjectListResponse>> getUserProjects(
            @AuthenticationPrincipal User currentUser
    ) {
        List<ProjectListResponse> projects = projectService.getUserProjects(currentUser);
        return ResponseEntity.ok(projects);
    }

    // 3. GET PROJECT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        ProjectResponse response = projectService.getProjectById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    // 4. UPDATE PROJECT
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ProjectResponse response = projectService.updateProject(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    // 5. DELETE PROJECT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // 6. ADD MEMBER
    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ProjectResponse response = projectService.addMember(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    // 7. REMOVE MEMBER
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser
    ) {
        projectService.removeMember(id, userId, currentUser);
        return ResponseEntity.noContent().build();
    }

    // 8. GET PROJECT MEMBERS
    @GetMapping("/{id}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        List<ProjectMemberResponse> members = projectService.getProjectMembers(id, currentUser);
        return ResponseEntity.ok(members);
    }
}