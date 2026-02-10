package com.task.manage.donor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DonorRequestDto(
        @NotBlank(message = "Donor name is required")
        String donorName,

        @NotBlank(message = "Email address is required")
        @Email(message = "Email address must be valid")
        String emailAddress,

        String contactNumber
) {
}

