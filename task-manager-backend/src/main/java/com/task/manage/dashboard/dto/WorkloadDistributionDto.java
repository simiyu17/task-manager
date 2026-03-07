package com.task.manage.dashboard.dto;

/**
 * DTO for workload distribution by status over time
 */
public record WorkloadDistributionDto(
    String date,
    String status,
    String statusDisplay,
    Long taskCount
) {
}

