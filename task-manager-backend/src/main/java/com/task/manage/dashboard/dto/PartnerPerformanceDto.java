package com.task.manage.dashboard.dto;

/**
 * DTO for partner performance metrics
 */
public record PartnerPerformanceDto(
    Long partnerId,
    String partnerName,
    Long totalTasks,
    Long completedTasks,
    Long activeTasks,
    Double averageCompletionHours,
    Double completionRate,
    String totalBudget
) {
}

