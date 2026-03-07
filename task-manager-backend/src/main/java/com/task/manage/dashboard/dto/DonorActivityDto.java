package com.task.manage.dashboard.dto;

/**
 * DTO for donor activity summary
 */
public record DonorActivityDto(
    Long donorId,
    String donorName,
    String emailAddress,
    Long totalRequests,
    Long activeRequests,
    Long completedRequests,
    String totalBudget,
    String lastRequestDate
) {
}

