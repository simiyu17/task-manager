package com.task.manage.dashboard.dto;

import java.time.LocalDate;

/**
 * DTO for completion trend over time
 */
public record CompletionTrendDto(
    LocalDate periodStart,
    Long completedTasks,
    Double averageDurationHours,
    Double averageDurationDays
) {
}

