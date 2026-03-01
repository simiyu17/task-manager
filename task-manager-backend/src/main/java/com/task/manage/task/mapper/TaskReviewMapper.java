package com.task.manage.task.mapper;

import com.task.manage.task.domain.Task;
import com.task.manage.task.domain.TaskReview;
import com.task.manage.task.dto.TaskReviewResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = {ReviewCommentMapper.class, ClarifyingQuestionMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TaskReviewMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "reviewStatus", expression = "java(reviewStatusToString(review.getReviewStatus()))")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "clarifyingQuestions", ignore = true)
    TaskReviewResponseDto toResponseDto(TaskReview review);

    default String reviewStatusToString(Task.TaskStatus reviewStatus) {
        return reviewStatus != null ? reviewStatus.name() : null;
    }
}
