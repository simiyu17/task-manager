package com.task.manage.task.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TaskRequestDto(
        String title,

        Long donorId,

        String description,

        Long assignedPartnerId,

        BigDecimal validatedBudget,

        LocalDateTime requestReceivedAt,

        LocalDateTime acceptedAt,

        LocalDateTime deadline,

        String allocateNotes,

        String acceptanceNotes,

        String rejectionNotes
) {
}
