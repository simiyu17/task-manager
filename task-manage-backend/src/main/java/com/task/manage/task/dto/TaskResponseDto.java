package com.task.manage.task.dto;

import com.task.manage.partner.dto.PartnerResponseDto;
import com.task.manage.task.domain.Task.TaskStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record TaskResponseDto(
        Long id,
        String title,
        String taskProviderName,
        String description,
        PartnerResponseDto assignedPartner,
        TaskStatus taskStatus,
        BigDecimal validatedBudget,
        LocalDateTime requestReceivedAt,
        LocalDateTime acceptedAt,
        LocalDateTime deadline,
        Instant dateCreated,
        Instant lastModified,
        String createdBy,
        String lastModifiedBy
) {
}
