package com.task.manage.dashboard.dto;

import java.math.BigDecimal;

/**
 * DTO for overall dashboard KPI metrics
 */
public record DashboardKpiDto(
    Long totalActiveTasks,
    Long totalCompletedTasks,
    Long tasksStuck,  // Stuck > 72 hours
    Double averageCompletionTimeHours,
    BigDecimal totalActiveBudget,
    Long tasksNearingDeadline,  // Deadline within 7 days
    Long partnersActive,
    Long donorsActive,
    Double completionRatePercentage,
    Long tasksCreatedThisMonth,
    Long tasksCompletedThisMonth
) {
}

