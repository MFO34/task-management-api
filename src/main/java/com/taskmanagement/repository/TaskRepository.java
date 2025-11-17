package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.TaskPriority;
import com.taskmanagement.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ✨ YENİ: Pageable versiyonlar
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
    
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);
    
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);
    
    Page<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status, Pageable pageable);
    
    Page<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId, Pageable pageable);
    
    Page<Task> findByProjectIdAndPriority(Long projectId, TaskPriority priority, Pageable pageable);
    
    Page<Task> findByProjectIdAndAssigneeIsNull(Long projectId, Pageable pageable);

    // ✨ YENİ: Pageable JPQL query
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND t.deadline < CURRENT_TIMESTAMP " +
           "AND t.status != 'DONE'")
    Page<Task> findOverdueTasks(@Param("projectId") Long projectId, Pageable pageable);
    
    @Query("SELECT t FROM Task t " +
           "JOIN ProjectMember pm ON t.project.id = pm.project.id " +
           "WHERE pm.user.id = :userId")
    Page<Task> findTasksByUserId(@Param("userId") Long userId, Pageable pageable);
    
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByPriority(TaskPriority priority);
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    List<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId);
    List<Task> findByProjectIdAndPriority(Long projectId, TaskPriority priority);
    List<Task> findByProjectIdAndAssigneeIsNull(Long projectId);

    // ========================================
    // ✨ YENİ: ADVANCED SEARCH QUERY
    // ========================================

    /**
     * Advanced search with multiple filters
     * - Keyword search in title and description (case-insensitive)
     * - Filter by status (optional)
     * - Filter by priority (optional)
     * - Filter by assignee (optional)
     * - Filter by project (optional)
     * - Only accessible tasks (via project membership)
     * 
     * @param userId Current user ID (for authorization)
     * @param keyword Search keyword (searches in title and description)
     * @param status Filter by status (optional)
     * @param priority Filter by priority (optional)
     * @param assigneeId Filter by assignee (optional)
     * @param projectId Filter by project (optional)
     * @param pageable Pagination and sorting
     * @return Page of tasks matching criteria
     */
    @Query("SELECT DISTINCT t FROM Task t " +
           "JOIN ProjectMember pm ON t.project.id = pm.project.id " +
           "WHERE pm.user.id = :userId " +
           "AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
           "AND (:projectId IS NULL OR t.project.id = :projectId)")
    Page<Task> searchTasks(
        @Param("userId") Long userId,
        @Param("keyword") String keyword,
        @Param("status") TaskStatus status,
        @Param("priority") TaskPriority priority,
        @Param("assigneeId") Long assigneeId,
        @Param("projectId") Long projectId,
        Pageable pageable
    );

   // ========================================
       // ✨ STATISTICS QUERIES (DÜZELTİLMİŞ!)
       // ========================================

       /**
        * Count tasks by status for user's accessible projects
        */
       @Query("SELECT COUNT(t) FROM Task t " +
              "JOIN ProjectMember pm ON t.project.id = pm.project.id " +
              "WHERE pm.user.id = :userId AND t.status = :status")
       Long countTasksByUserAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

       /**
        * Count tasks by priority for user's accessible projects
        */
       @Query("SELECT COUNT(t) FROM Task t " +
              "JOIN ProjectMember pm ON t.project.id = pm.project.id " +
              "WHERE pm.user.id = :userId AND t.priority = :priority")
       Long countTasksByUserAndPriority(@Param("userId") Long userId, @Param("priority") TaskPriority priority);

       /**
        * Count tasks assigned to user
        */
       Long countByAssigneeId(Long userId);

       /**
        * Count unassigned tasks in user's projects
        */
       @Query("SELECT COUNT(t) FROM Task t " +
              "JOIN ProjectMember pm ON t.project.id = pm.project.id " +
              "WHERE pm.user.id = :userId AND t.assignee IS NULL")
       Long countUnassignedTasksByUser(@Param("userId") Long userId);

       /**
        * Count overdue tasks for user
        */
       @Query("SELECT COUNT(t) FROM Task t " +
              "JOIN ProjectMember pm ON t.project.id = pm.project.id " +
              "WHERE pm.user.id = :userId " +
              "AND t.deadline < CURRENT_TIMESTAMP " +
              "AND t.status != 'DONE'")
       Long countOverdueTasksByUser(@Param("userId") Long userId);

       /**
        * Count tasks due today for user (NATIVE QUERY)
        */
       @Query(value = "SELECT COUNT(*) FROM tasks t " +
              "JOIN project_members pm ON t.project_id = pm.project_id " +
              "WHERE pm.user_id = :userId " +
              "AND DATE(t.deadline) = CURRENT_DATE " +
              "AND t.status != 'DONE'",
              nativeQuery = true)
       Long countTasksDueTodayByUser(@Param("userId") Long userId);

       /**
        * Count tasks due this week for user (NATIVE QUERY)
        */
       @Query(value = "SELECT COUNT(*) FROM tasks t " +
              "JOIN project_members pm ON t.project_id = pm.project_id " +
              "WHERE pm.user_id = :userId " +
              "AND t.deadline BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days' " +
              "AND t.status != 'DONE'",
              nativeQuery = true)
       Long countTasksDueThisWeekByUser(@Param("userId") Long userId);

       /**
        * Count total tasks in user's projects
        */
       @Query("SELECT COUNT(t) FROM Task t " +
              "JOIN ProjectMember pm ON t.project.id = pm.project.id " +
              "WHERE pm.user.id = :userId")
       Long countTotalTasksByUser(@Param("userId") Long userId);
       // ========================================
       // ✨ PROJECT STATISTICS QUERIES
       // ========================================

       /**
        * Count tasks by status in project
        */
       Long countByProjectIdAndStatus(Long projectId, TaskStatus status);

       /**
        * Count tasks by priority in project
        */
       Long countByProjectIdAndPriority(Long projectId, TaskPriority priority);

       /**
        * Count overdue tasks in project
        */
       @Query("SELECT COUNT(t) FROM Task t " +
              "WHERE t.project.id = :projectId " +
              "AND t.deadline < CURRENT_TIMESTAMP " +
              "AND t.status != 'DONE'")
       Long countOverdueTasksByProject(@Param("projectId") Long projectId);

       // ========================================
       // ✨ USER STATISTICS QUERIES (EKSİK OLANLAR)
       // ========================================

       /**
        * Count tasks by assignee and status
        */
       Long countByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);

       /**
        * Count tasks by assignee and priority
        */
       Long countByAssigneeIdAndPriority(Long assigneeId, TaskPriority priority);

       /**
        * Count overdue tasks for assignee
        */
       @Query("SELECT COUNT(t) FROM Task t " +
              "WHERE t.assignee.id = :assigneeId " +
              "AND t.deadline < CURRENT_TIMESTAMP " +
              "AND t.status != 'DONE'")
       Long countOverdueTasksByAssignee(@Param("assigneeId") Long assigneeId);

       /**
        * Count completed tasks on time (before deadline)
        */
       @Query("SELECT COUNT(t) FROM Task t " +
              "WHERE t.assignee.id = :assigneeId " +
              "AND t.status = 'DONE' " +
              "AND (t.deadline IS NULL OR t.updatedAt <= t.deadline)")
       Long countCompletedOnTimeByAssignee(@Param("assigneeId") Long assigneeId);

       /**
        * Count total tasks by project
        */
       Long countByProjectId(Long projectId);
}