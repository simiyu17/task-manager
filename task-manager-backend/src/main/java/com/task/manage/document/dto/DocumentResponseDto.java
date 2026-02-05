package com.task.manage.document.dto;

import java.time.LocalDateTime;

public record DocumentResponseDto(
        Long id,
        Long taskId,
        Integer version,
        String documentType,
        String fileName,
        String filePath,
        String fileLocation,
        boolean isFinal,
        String uploadedBy,
        LocalDateTime uploadedAt
) {
}
