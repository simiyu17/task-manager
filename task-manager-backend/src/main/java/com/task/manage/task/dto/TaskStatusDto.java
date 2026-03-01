package com.task.manage.task.dto;

/**
 * DTO for representing task status information in a simplified form.
 * This can be used for dropdowns, status displays, or any scenario where only the status code and display name are needed.
 *
 * @param stepValue        The integer value representing the step in the workflow.
 * @param statusCode       The code representing the task status (e.g., "RECEIVED", "ACCEPTED").
 * @param statusDisplayName The human-readable name for the task status (e.g., "Received", "Accepted").
 */
public record TaskStatusDto(
        Integer stepValue,
        String statusCode,
        String statusDisplayName
) {
}
