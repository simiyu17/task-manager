package com.task.manage.task.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, Long> {

    /**
     * Find all status history for a task ordered by changed date descending
     */
    List<TaskStatusHistory> findByTaskIdOrderByChangedAtDesc(Long taskId);

    /**
     * Find when a task reached a specific status (first occurrence)
     */
    @Query("SELECT h FROM TaskStatusHistory h WHERE h.task.id = :taskId AND h.toStatus = :status ORDER BY h.changedAt ASC")
    Optional<TaskStatusHistory> findFirstByTaskIdAndStatus(@Param("taskId") Long taskId, @Param("status") Task.TaskStatus status);

    /**
     * Find all times a task was in a specific status
     */
    @Query("SELECT h FROM TaskStatusHistory h WHERE h.task.id = :taskId AND h.toStatus = :status ORDER BY h.changedAt DESC")
    List<TaskStatusHistory> findByTaskIdAndStatus(@Param("taskId") Long taskId, @Param("status") Task.TaskStatus status);

    /**
     * Find task history within a date range
     */
    @Query("SELECT h FROM TaskStatusHistory h WHERE h.task.id = :taskId AND h.changedAt BETWEEN :startDate AND :endDate ORDER BY h.changedAt")
    List<TaskStatusHistory> findTaskHistoryBetweenDates(
        @Param("taskId") Long taskId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find the most recent status change for a task
     */
    Optional<TaskStatusHistory> findFirstByTaskIdOrderByChangedAtDesc(Long taskId);

    /**
     * Get all status changes for multiple tasks
     */
    @Query("SELECT h FROM TaskStatusHistory h WHERE h.task.id IN :taskIds ORDER BY h.task.id, h.changedAt DESC")
    List<TaskStatusHistory> findByTaskIdsOrderByChangedAtDesc(@Param("taskIds") List<Long> taskIds);

    /**
     * Count status changes for a specific task
     */
    long countByTaskId(Long taskId);
}

