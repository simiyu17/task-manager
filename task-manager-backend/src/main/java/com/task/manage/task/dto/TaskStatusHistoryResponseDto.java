package com.task.manage.task.dto;

import com.task.manage.task.domain.Task;

import java.time.LocalDateTime;

public record TaskStatusHistoryResponseDto(
    Long id,
    Long taskId,
    String taskTitle,
    Task.TaskStatus fromStatus,
    String fromStatusDisplay,
    Task.TaskStatus toStatus,
    String toStatusDisplay,
    LocalDateTime changedAt,
    String changedBy,
    String notes,
    Long durationInPreviousStatusHours
) {
}

