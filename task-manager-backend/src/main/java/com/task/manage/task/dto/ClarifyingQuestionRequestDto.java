package com.task.manage.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClarifyingQuestionRequestDto(
        @NotNull(message = "Task review ID is required")
        Long taskReviewId,

        @NotBlank(message = "Question text is required")
        String questionText,

        @NotBlank(message = "Questioner name is required")
        String questionerName
) {
}
