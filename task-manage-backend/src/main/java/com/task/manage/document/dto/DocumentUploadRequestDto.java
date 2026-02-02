package com.task.manage.document.dto;

import jakarta.validation.constraints.NotNull;

public record DocumentUploadRequestDto(
        @NotNull(message = "Document type is required")
        String documentType,

        @NotNull(message = "Task ID is required")
        Long taskId
) {
}
