package com.task.manage.task.mapper;

import com.task.manage.task.domain.ClarifyingQuestion;
import com.task.manage.task.dto.ClarifyingQuestionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ClarifyingQuestionMapper {

    @Mapping(target = "taskReviewId", source = "taskReview.id")
    @Mapping(target = "isAnswered", source = "answered")
    ClarifyingQuestionResponseDto toResponseDto(ClarifyingQuestion question);
}
