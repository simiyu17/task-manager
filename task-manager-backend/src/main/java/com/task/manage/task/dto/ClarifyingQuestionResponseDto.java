package com.task.manage.task.dto;

public record ClarifyingQuestionResponseDto(
        Long id,
        Long taskReviewId,
        String questionText,
        String answerText,
        String answeredBy,
        boolean isAnswered,
        String createdBy
) {
}
