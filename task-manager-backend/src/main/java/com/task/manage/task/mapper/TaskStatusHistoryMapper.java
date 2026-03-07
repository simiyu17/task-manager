package com.task.manage.task.mapper;

import com.task.manage.task.domain.TaskStatusHistory;
import com.task.manage.task.dto.TaskStatusHistoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskStatusHistoryMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "taskTitle", source = "task.title")
    @Mapping(target = "fromStatusDisplay", expression = "java(getStatusDisplay(history.getFromStatus()))")
    @Mapping(target = "toStatusDisplay", expression = "java(getStatusDisplay(history.getToStatus()))")
    TaskStatusHistoryResponseDto toResponseDto(TaskStatusHistory history);

    default String getStatusDisplay(com.task.manage.task.domain.Task.TaskStatus status) {
        return status != null ? status.getDisplayName() : null;
    }
}

