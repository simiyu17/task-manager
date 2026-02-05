package com.task.manage.partner.dto;

import jakarta.validation.constraints.NotBlank;

public record PartnerRequestDto(
        @NotBlank(message = "Partner name is required")
        String partnerName
) {
}
