package com.task.manage.dashboard.dto;

import java.time.LocalDate;

/**
 * DTO for status change activity over time
 */
public record StatusActivityDto(
    LocalDate date,
    String status,
    String statusDisplay,
    Long changesCount
) {
}

