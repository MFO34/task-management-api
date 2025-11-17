package com.taskmanagement.repository;

import com.taskmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // User'ın üye olduğu tüm projeler
    @Query("SELECT p FROM Project p JOIN p.members pm WHERE pm.user.id = :userId")
    List<Project> findProjectsByUserId(@Param("userId") Long userId);

    // User'ın owner olduğu projeler
    List<Project> findByOwnerId(Long ownerId);

    // ========================================
    // ✨ STATISTICS QUERIES
    // ========================================

    /**
     * Count projects owned by user
     */
    Long countByOwnerId(Long userId);

    /**
     * Count projects where user is member
     */
    @Query("SELECT COUNT(DISTINCT pm.project) FROM ProjectMember pm WHERE pm.user.id = :userId")
    Long countProjectsByMemberId(@Param("userId") Long userId);
}
