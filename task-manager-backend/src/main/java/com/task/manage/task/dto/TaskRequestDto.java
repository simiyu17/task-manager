package com.task.manage.task.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaskRequestDto(
        @NotBlank(message = "Title is required")
        String title,

        String taskProviderName,

        String description,

        Long assignedPartnerId,

        BigDecimal validatedBudget,

        LocalDateTime requestReceivedAt,

        LocalDateTime acceptedAt,

        LocalDateTime deadline
) {
}
