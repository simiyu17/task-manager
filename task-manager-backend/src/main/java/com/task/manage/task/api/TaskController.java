package com.task.manage.task.api;

import com.task.manage.task.dto.TaskRequestDto;
import com.task.manage.task.dto.TaskResponseDto;
import com.task.manage.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto requestDto) {
        TaskResponseDto response = taskService.createTask(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto requestDto) {
        TaskResponseDto response = taskService.updateTask(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable Long id) {
        TaskResponseDto response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        List<TaskResponseDto> response = taskService.getAllTasks();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<TaskResponseDto>> getAllTasksPaginated(
            @PageableDefault(size = 10, sort = "dateCreated") Pageable pageable) {
        Page<TaskResponseDto> response = taskService.getAllTasksPaginated(pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/assign-partner/{partnerId}")
    public ResponseEntity<TaskResponseDto> assignPartnerToTask(
            @PathVariable Long taskId,
            @PathVariable Long partnerId) {
        TaskResponseDto response = taskService.assignPartnerToTask(taskId, partnerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam String status) {
        TaskResponseDto response = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/title")
    public ResponseEntity<TaskResponseDto> updateTitle(
            @PathVariable Long taskId,
            @RequestParam String title) {
        TaskResponseDto response = taskService.updateTitle(taskId, title);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/donor/{donorId}")
    public ResponseEntity<TaskResponseDto> updateDonor(
            @PathVariable Long taskId,
            @PathVariable Long donorId) {
        TaskResponseDto response = taskService.updateDonor(taskId, donorId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/description")
    public ResponseEntity<TaskResponseDto> updateDescription(
            @PathVariable Long taskId,
            @RequestParam String description) {
        TaskResponseDto response = taskService.updateDescription(taskId, description);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/validated-budget")
    public ResponseEntity<TaskResponseDto> updateValidatedBudget(
            @PathVariable Long taskId,
            @RequestParam BigDecimal validatedBudget) {
        TaskResponseDto response = taskService.updateValidatedBudget(taskId, validatedBudget);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/request-received-at")
    public ResponseEntity<TaskResponseDto> updateRequestReceivedAt(
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestReceivedAt) {
        TaskResponseDto response = taskService.updateRequestReceivedAt(taskId, requestReceivedAt);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/accepted-at")
    public ResponseEntity<TaskResponseDto> updateAcceptedAt(
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime acceptedAt) {
        TaskResponseDto response = taskService.updateAcceptedAt(taskId, acceptedAt);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/deadline")
    public ResponseEntity<TaskResponseDto> updateDeadline(
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline) {
        TaskResponseDto response = taskService.updateDeadline(taskId, deadline);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/allocate-notes")
    public ResponseEntity<TaskResponseDto> updateAllocateNotes(
            @PathVariable Long taskId,
            @RequestParam String allocateNotes) {
        TaskResponseDto response = taskService.updateAllocateNotes(taskId, allocateNotes);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/acceptance-notes")
    public ResponseEntity<TaskResponseDto> updateAcceptanceNotes(
            @PathVariable Long taskId,
            @RequestParam String acceptanceNotes) {
        TaskResponseDto response = taskService.updateAcceptanceNotes(taskId, acceptanceNotes);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/rejection-notes")
    public ResponseEntity<TaskResponseDto> updateRejectionNotes(
            @PathVariable Long taskId,
            @RequestParam String rejectionNotes) {
        TaskResponseDto response = taskService.updateRejectionNotes(taskId, rejectionNotes);
        return ResponseEntity.ok(response);
    }
}
