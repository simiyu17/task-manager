package com.task.manage.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCommentRequestDto(
        @NotNull(message = "Task review ID is required")
        Long taskReviewId,

        @NotBlank(message = "Comment text is required")
        String commentText,


        String sectionReference
) {
}
