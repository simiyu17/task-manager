package com.task.manage.task.service;

import com.task.manage.task.dto.TaskRequestDto;
import com.task.manage.task.dto.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto requestDto);

    TaskResponseDto updateTask(Long id, TaskRequestDto requestDto);

    TaskResponseDto getTaskById(Long id);

    List<TaskResponseDto> getAllTasks();

    Page<TaskResponseDto> getAllTasksPaginated(Pageable pageable);

    void deleteTask(Long id);

    TaskResponseDto assignPartnerToTask(Long taskId, Long partnerId);

    TaskResponseDto updateTaskStatus(Long taskId, String status);

    TaskResponseDto moveTaskToNextStatus(Long taskId, Boolean rejected);

    // Patch methods for individual fields
    TaskResponseDto updateTitle(Long taskId, String title);

    TaskResponseDto updateDonor(Long taskId, Long donorId);

    TaskResponseDto updateDescription(Long taskId, String description);

    TaskResponseDto updateValidatedBudget(Long taskId, BigDecimal validatedBudget);

    TaskResponseDto updateRequestReceivedAt(Long taskId, LocalDateTime requestReceivedAt);

    TaskResponseDto updateAcceptedAt(Long taskId, LocalDateTime acceptedAt);

    TaskResponseDto updateDeadline(Long taskId, LocalDateTime deadline);

    TaskResponseDto updateAllocateNotes(Long taskId, String allocateNotes);

    TaskResponseDto updateAcceptanceNotes(Long taskId, String acceptanceNotes);

    TaskResponseDto updateRejectionNotes(Long taskId, String rejectionNotes);
}
