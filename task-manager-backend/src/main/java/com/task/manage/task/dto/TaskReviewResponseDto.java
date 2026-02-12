package com.task.manage.task.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TaskReviewResponseDto(
        Long id,
        Long taskId,
        String reviewStatus,
        Integer reviewCycle,
        LocalDateTime reviewedAt,
        String overallComment,
        String createdBy,
        List<ReviewCommentResponseDto> comments,
        List<ClarifyingQuestionResponseDto> clarifyingQuestions
) {
}
