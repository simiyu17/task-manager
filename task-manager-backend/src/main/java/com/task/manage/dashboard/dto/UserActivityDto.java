package com.task.manage.dashboard.dto;

/**
 * DTO for user activity metrics
 */
public record UserActivityDto(
    String username,
    String status,
    String statusDisplay,
    Long changesMade,
    String firstChange,
    String lastChange
) {
}

