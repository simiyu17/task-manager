package com.task.manage.partner.dto;

import java.time.Instant;

public record PartnerResponseDto(
        Long id,
        String partnerName,
        Instant dateCreated,
        Instant lastModified,
        String createdBy,
        String lastModifiedBy
) {
}
