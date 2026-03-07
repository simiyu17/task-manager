package com.task.manage.task.service;

import com.task.manage.task.domain.Task;
import com.task.manage.task.dto.TaskStatusHistoryResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskStatusHistoryService {

    /**
     * Get all status history for a specific task
     */
    List<TaskStatusHistoryResponseDto> getTaskStatusHistory(Long taskId);

    /**
     * Get when a task first reached a specific status
     */
    Optional<LocalDateTime> getDateWhenTaskReachedStatus(Long taskId, Task.TaskStatus status);

    /**
     * Get status history for a task within a date range
     */
    List<TaskStatusHistoryResponseDto> getTaskHistoryBetweenDates(Long taskId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get all times a task was in a specific status
     */
    List<TaskStatusHistoryResponseDto> getTaskHistoryByStatus(Long taskId, Task.TaskStatus status);

    /**
     * Get the most recent status change for a task
     */
    Optional<TaskStatusHistoryResponseDto> getLatestStatusChange(Long taskId);

    /**
     * Get total duration a task spent in a specific status (in hours)
     */
    Long getTotalDurationInStatus(Long taskId, Task.TaskStatus status);
}

