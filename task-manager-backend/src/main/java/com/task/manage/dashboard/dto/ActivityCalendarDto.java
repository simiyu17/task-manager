package com.task.manage.dashboard.dto;

import java.time.LocalDate;

/**
 * DTO for activity calendar heatmap
 */
public record ActivityCalendarDto(
    LocalDate date,
    Long changesCount,
    Long uniqueTasks,
    Long uniqueUsers
) {
}

