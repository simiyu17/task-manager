package com.task.manage.dashboard.dto;

/**
 * DTO for tasks that are stuck in a status
 */
public record StuckTaskDto(
    Long taskId,
    String title,
    String currentStatus,
    String statusDisplay,
    String statusSince,
    Long hoursInStatus,
    String urgencyLevel,
    String partnerName,
    String donorName,
    String deadline,
    String changedBy
) {
}

