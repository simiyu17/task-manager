package com.task.manage.task.api;

import com.task.manage.task.domain.Task;
import com.task.manage.task.dto.TaskStatusHistoryResponseDto;
import com.task.manage.task.service.TaskStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/status-history")
@RequiredArgsConstructor
@Slf4j
public class TaskStatusHistoryController {

    private final TaskStatusHistoryService taskStatusHistoryService;

    /**
     * Get all status history for a specific task
     */
    @GetMapping
    public ResponseEntity<List<TaskStatusHistoryResponseDto>> getTaskStatusHistory(@PathVariable Long taskId) {
        log.info("GET /api/v1/tasks/{}/status-history - Fetching status history", taskId);
        List<TaskStatusHistoryResponseDto> history = taskStatusHistoryService.getTaskStatusHistory(taskId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get when a task first reached a specific status
     */
    @GetMapping("/status/{status}/first-occurrence")
    public ResponseEntity<LocalDateTime> getDateWhenTaskReachedStatus(
            @PathVariable Long taskId,
            @PathVariable String status) {
        log.info("GET /api/v1/tasks/{}/status-history/status/{}/first-occurrence", taskId, status);

        Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
        if (taskStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        return taskStatusHistoryService.getDateWhenTaskReachedStatus(taskId, taskStatus)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get status history for a task within a date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<TaskStatusHistoryResponseDto>> getTaskHistoryBetweenDates(
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET /api/v1/tasks/{}/status-history/date-range?startDate={}&endDate={}", taskId, startDate, endDate);

        List<TaskStatusHistoryResponseDto> history = taskStatusHistoryService.getTaskHistoryBetweenDates(taskId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * Get all times a task was in a specific status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskStatusHistoryResponseDto>> getTaskHistoryByStatus(
            @PathVariable Long taskId,
            @PathVariable String status) {
        log.info("GET /api/v1/tasks/{}/status-history/status/{}", taskId, status);

        Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
        if (taskStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        List<TaskStatusHistoryResponseDto> history = taskStatusHistoryService.getTaskHistoryByStatus(taskId, taskStatus);
        return ResponseEntity.ok(history);
    }

    /**
     * Get the most recent status change for a task
     */
    @GetMapping("/latest")
    public ResponseEntity<TaskStatusHistoryResponseDto> getLatestStatusChange(@PathVariable Long taskId) {
        log.info("GET /api/v1/tasks/{}/status-history/latest", taskId);

        return taskStatusHistoryService.getLatestStatusChange(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get total duration a task spent in a specific status (in hours)
     */
    @GetMapping("/status/{status}/duration")
    public ResponseEntity<Long> getTotalDurationInStatus(
            @PathVariable Long taskId,
            @PathVariable String status) {
        log.info("GET /api/v1/tasks/{}/status-history/status/{}/duration", taskId, status);

        Task.TaskStatus taskStatus = Task.TaskStatus.fromString(status);
        if (taskStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        Long duration = taskStatusHistoryService.getTotalDurationInStatus(taskId, taskStatus);
        return ResponseEntity.ok(duration);
    }
}

