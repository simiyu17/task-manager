package com.task.manage.dashboard.dto;

import com.task.manage.task.domain.Task;

/**
 * DTO for status distribution (for donut/pie charts)
 */
public record StatusDistributionDto(
    Task.TaskStatus status,
    String statusDisplay,
    Long taskCount,
    Double percentage,
    Double averageBudget
) {
}

