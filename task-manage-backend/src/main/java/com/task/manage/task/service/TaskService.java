package com.task.manage.task.service;

import com.task.manage.task.dto.TaskRequestDto;
import com.task.manage.task.dto.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}
