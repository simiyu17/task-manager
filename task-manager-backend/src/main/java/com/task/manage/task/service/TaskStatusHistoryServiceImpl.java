package com.task.manage.task.service;

import com.task.manage.task.domain.Task;
import com.task.manage.task.domain.TaskStatusHistory;
import com.task.manage.task.domain.TaskStatusHistoryRepository;
import com.task.manage.task.dto.TaskStatusHistoryResponseDto;
import com.task.manage.task.mapper.TaskStatusHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaskStatusHistoryServiceImpl implements TaskStatusHistoryService {

    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final TaskStatusHistoryMapper taskStatusHistoryMapper;

    @Override
    public List<TaskStatusHistoryResponseDto> getTaskStatusHistory(Long taskId) {
        log.info("Fetching status history for task {}", taskId);

        List<TaskStatusHistory> history = taskStatusHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId);

        return history.stream()
                .map(taskStatusHistoryMapper::toResponseDto)
                .toList();
    }

    @Override
    public Optional<LocalDateTime> getDateWhenTaskReachedStatus(Long taskId, Task.TaskStatus status) {
        log.info("Finding when task {} first reached status {}", taskId, status);

        Optional<TaskStatusHistory> history = taskStatusHistoryRepository.findFirstByTaskIdAndStatus(taskId, status);

        return history.map(TaskStatusHistory::getChangedAt);
    }

    @Override
    public List<TaskStatusHistoryResponseDto> getTaskHistoryBetweenDates(Long taskId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching status history for task {} between {} and {}", taskId, startDate, endDate);

        List<TaskStatusHistory> history = taskStatusHistoryRepository.findTaskHistoryBetweenDates(taskId, startDate, endDate);

        return history.stream()
                .map(taskStatusHistoryMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<TaskStatusHistoryResponseDto> getTaskHistoryByStatus(Long taskId, Task.TaskStatus status) {
        log.info("Fetching all occurrences of status {} for task {}", status, taskId);

        List<TaskStatusHistory> history = taskStatusHistoryRepository.findByTaskIdAndStatus(taskId, status);

        return history.stream()
                .map(taskStatusHistoryMapper::toResponseDto)
                .toList();
    }

    @Override
    public Optional<TaskStatusHistoryResponseDto> getLatestStatusChange(Long taskId) {
        log.info("Fetching latest status change for task {}", taskId);

        Optional<TaskStatusHistory> history = taskStatusHistoryRepository.findFirstByTaskIdOrderByChangedAtDesc(taskId);

        return history.map(taskStatusHistoryMapper::toResponseDto);
    }

    @Override
    public Long getTotalDurationInStatus(Long taskId, Task.TaskStatus status) {
        log.info("Calculating total duration task {} spent in status {}", taskId, status);

        List<TaskStatusHistory> allHistory = taskStatusHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId);

        // Reverse to get chronological order
        List<TaskStatusHistory> chronologicalHistory = allHistory.stream()
                .sorted(Comparator.comparing(TaskStatusHistory::getChangedAt))
                .toList();

        long totalHours = 0L;
        LocalDateTime statusStartTime = null;

        for (TaskStatusHistory current : chronologicalHistory) {
            // Check if we're entering the target status
            if (current.getToStatus() == status) {
                statusStartTime = current.getChangedAt();
            }

            // Check if we're leaving the target status
            if (current.getFromStatus() == status && statusStartTime != null) {
                long duration = ChronoUnit.HOURS.between(statusStartTime, current.getChangedAt());
                totalHours += duration;
                statusStartTime = null;
            }
        }

        // If still in the target status, calculate duration until now
        if (statusStartTime != null) {
            long duration = ChronoUnit.HOURS.between(statusStartTime, LocalDateTime.now());
            totalHours += duration;
        }

        return totalHours;
    }
}

