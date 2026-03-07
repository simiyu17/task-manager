package com.task.manage.dashboard.dto;

/**
 * DTO for average time spent in each status
 */
public record StatusDurationDto(
    String status,
    String statusDisplay,
    Double averageHours,
    Double minHours,
    Double maxHours,
    Double medianHours,
    Long transitionCount
) {
}

