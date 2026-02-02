package com.task.manage.task.dto;

public record ClarifyingQuestionResponseDto(
        Long id,
        Long taskReviewId,
        String questionText,
        String questionerName,
        String answerText,
        String answeredBy,
        boolean isAnswered
) {
}
