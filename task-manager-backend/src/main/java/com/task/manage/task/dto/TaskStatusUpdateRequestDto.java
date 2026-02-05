package com.task.manage.task.dto;

import com.task.manage.task.domain.Task;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequestDto(
        @NotNull(message = "Task status is required")
        Task.TaskStatus taskStatus
) {
}
