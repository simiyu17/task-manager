package com.task.manage.task.mapper;

import com.task.manage.task.domain.ReviewComment;
import com.task.manage.task.dto.ReviewCommentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReviewCommentMapper {

    @Mapping(target = "taskReviewId", source = "taskReview.id")
    ReviewCommentResponseDto toResponseDto(ReviewComment comment);
}
