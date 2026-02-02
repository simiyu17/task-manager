package com.task.manage.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskReviewRequestDto(
        @NotNull(message = "Task ID is required")
        Long taskId,

        @NotBlank(message = "Reviewer name is required")
        String reviewerName,

        String reviewerEmail,

        @NotNull(message = "Review status is required")
        String reviewStatus,

        String overallComment
) {
}
