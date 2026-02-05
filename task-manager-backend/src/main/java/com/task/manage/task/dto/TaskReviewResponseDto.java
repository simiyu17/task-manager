package com.task.manage.task.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TaskReviewResponseDto(
        Long id,
        Long taskId,
        String reviewerName,
        String reviewerEmail,
        String reviewStatus,
        Integer reviewCycle,
        LocalDateTime reviewedAt,
        String overallComment,
        List<ReviewCommentResponseDto> comments,
        List<ClarifyingQuestionResponseDto> clarifyingQuestions
) {
}
