package com.task.manage.task.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerQuestionRequestDto(
        @NotBlank(message = "Answer text is required")
        String answerText,

        @NotBlank(message = "Answered by is required")
        String answeredBy
) {
}
