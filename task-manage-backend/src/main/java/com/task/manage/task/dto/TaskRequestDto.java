package com.task.manage.task.dto;

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
        String taskStatus,

        BigDecimal validatedBudget,

        LocalDateTime requestReceivedAt,

        LocalDateTime acceptedAt,

        LocalDateTime deadline
) {
}
