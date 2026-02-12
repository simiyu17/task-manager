package com.task.manage.task.dto;

public record ReviewCommentResponseDto(
        Long id,
        Long taskReviewId,
        String commentText,
        String sectionReference,
        String createdBy
) {
}
