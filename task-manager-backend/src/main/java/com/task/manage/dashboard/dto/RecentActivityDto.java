package com.task.manage.dashboard.dto;

/**
 * DTO for recent activity feed
 */
public record RecentActivityDto(
    Long historyId,
    Long taskId,
    String taskTitle,
    String fromStatus,
    String fromStatusDisplay,
    String toStatus,
    String toStatusDisplay,
    String changedAt,
    String changedBy,
    Long durationInPreviousStatusHours
) {
}

