package com.task.manage.task.mapper;

import com.task.manage.task.domain.TaskStatusHistory;
import com.task.manage.task.dto.TaskStatusHistoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;

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

    @Named("sortedStatusHistory")
    default List<TaskStatusHistoryResponseDto> sortedStatusHistory(List<TaskStatusHistory> statusHistory) {
        if (statusHistory == null || statusHistory.isEmpty()) {
            return List.of();
        }

        return statusHistory.stream()
                .map(this::toResponseDto)
                .sorted(Comparator.comparing(TaskStatusHistoryResponseDto::changedAt).reversed())
                .toList();
    }
}

