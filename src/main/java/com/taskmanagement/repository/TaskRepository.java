package com.taskmanagement.repository;

import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.TaskPriority;
import com.taskmanagement.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Project'e göre task'ları getir
    List<Task> findByProjectId(Long projectId);

    // Assignee'ye göre task'ları getir
    List<Task> findByAssigneeId(Long assigneeId);

    // Status'a göre task'ları getir
    List<Task> findByStatus(TaskStatus status);

    // Priority'ye göre task'ları getir
    List<Task> findByPriority(TaskPriority priority);

    // Kombine filtreleme (Project + Status)
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    // Kombine filtreleme (Project + Assignee)
    List<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId);

    // Kombine filtreleme (Project + Priority)
    List<Task> findByProjectIdAndPriority(Long projectId, TaskPriority priority);

    // Assignee olmayan task'lar (unassigned)
    List<Task> findByProjectIdAndAssigneeIsNull(Long projectId);

    // Overdue task'lar (deadline geçmiş ve done değil)
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND t.deadline < CURRENT_TIMESTAMP " +
           "AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("projectId") Long projectId);

    // User'ın tüm task'ları (tüm projelerden)
    @Query("SELECT t FROM Task t " +
           "JOIN ProjectMember pm ON t.project.id = pm.project.id " +
           "WHERE pm.user.id = :userId")
    List<Task> findTasksByUserId(@Param("userId") Long userId);
}