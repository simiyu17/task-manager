package com.task.manage.dashboard.dto;

/**
 * DTO for status transition flow (Sankey diagram data)
 */
public record StatusTransitionDto(
    String fromStatus,
    String fromStatusDisplay,
    String toStatus,
    String toStatusDisplay,
    Long transitionCount,
    Double averageDurationHours
) {
}

