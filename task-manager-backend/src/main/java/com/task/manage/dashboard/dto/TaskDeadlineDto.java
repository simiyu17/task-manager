package com.task.manage.dashboard.dto;

/**
 * DTO for tasks approaching deadline
 */
public record TaskDeadlineDto(
    Long taskId,
    String title,
    String status,
    String statusDisplay,
    String deadline,
    Long daysRemaining,
    String partnerName,
    String donorName,
    String priority  // CRITICAL, HIGH, MEDIUM, LOW based on days remaining
) {
}

