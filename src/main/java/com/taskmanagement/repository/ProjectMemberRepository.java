package com.taskmanagement.repository;

import com.taskmanagement.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // Belirli bir projede belirli bir user var mı?
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    // Bir projedeki tüm member'lar
    List<ProjectMember> findByProjectId(Long projectId);

    // User'ın tüm membership'leri
    List<ProjectMember> findByUserId(Long userId);

    // User bu projede var mı? (boolean)
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectId(Long projectId);

    // Check if user is member of any project
    boolean existsByUserId(Long userId);
}