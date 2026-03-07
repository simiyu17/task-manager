package com.task.manage.task.dto;

import com.task.manage.donor.dto.DonorResponseDto;
import com.task.manage.partner.dto.PartnerResponseDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record TaskResponseDto(
        Long id,
        String title,
        DonorResponseDto donor,
        String description,
        PartnerResponseDto assignedPartner,
        String taskStatus,
        String taskStatusDisplayName,
        BigDecimal validatedBudget,
        LocalDateTime requestReceivedAt,
        LocalDateTime acceptedAt,
        LocalDateTime deadline,
        String allocateNotes,
        String acceptanceNotes,
        String rejectionNotes,
        Instant dateCreated,
        Instant lastModified,
        String createdBy,
        String lastModifiedBy,
        Integer stepValue,
        List<TaskStatusDto> possibleNextStatuses
) {
}
