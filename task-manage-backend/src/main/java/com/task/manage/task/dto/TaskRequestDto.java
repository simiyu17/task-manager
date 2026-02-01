package com.task.manage.task.dto;

import com.task.manage.task.domain.Task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaskRequestDto(
        @NotBlank(message = "Title is required")
        String title,

        String taskProviderName,

        String description,

        Long assignedPartnerId,

        @NotNull(message = "Task status is required")
        TaskStatus taskStatus,

        BigDecimal validatedBudget,

        LocalDateTime requestReceivedAt,

        LocalDateTime acceptedAt,

        LocalDateTime deadline
) {
}
