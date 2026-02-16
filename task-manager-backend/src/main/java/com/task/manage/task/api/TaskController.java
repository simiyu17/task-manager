package com.task.manage.task.api;

import com.task.manage.task.dto.TaskRequestDto;
import com.task.manage.task.dto.TaskResponseDto;
import com.task.manage.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/{taskId}/next-status")
    public ResponseEntity<TaskResponseDto> moveTaskToNextStatus(
            @PathVariable Long taskId, @RequestParam Boolean isRejected) {
        TaskResponseDto response = taskService.moveTaskToNextStatus(taskId, isRejected);
        return ResponseEntity.ok(response);
    }
}
