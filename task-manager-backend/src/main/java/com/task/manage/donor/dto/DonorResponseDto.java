package com.task.manage.donor.dto;

import java.time.Instant;

public record DonorResponseDto(
        Long id,
        String donorName,
        String emailAddress,
        String contactNumber,
        Instant dateCreated,
        Instant lastModified,
        String createdBy,
        String lastModifiedBy
) {
}

