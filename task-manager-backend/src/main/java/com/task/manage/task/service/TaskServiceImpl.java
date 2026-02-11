package com.task.manage.task.service;

import com.task.manage.donor.domain.Donor;
import com.task.manage.donor.domain.DonorRepository;
import com.task.manage.donor.exception.DonorNotFoundException;
import com.task.manage.partner.domain.Partner;
import com.task.manage.partner.domain.PartnerRepository;
import com.task.manage.partner.exception.PartnerNotFoundException;
import com.task.manage.task.domain.Task;
import com.task.manage.task.domain.Task.TaskStatus;
import com.task.manage.task.domain.TaskRepository;
import com.task.manage.task.dto.TaskRequestDto;
import com.task.manage.task.dto.TaskResponseDto;
import com.task.manage.task.exception.TaskAlreadyExistsException;
import com.task.manage.task.exception.TaskNotFoundException;
import com.task.manage.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final PartnerRepository partnerRepository;
    private final DonorRepository donorRepository;
    private final TaskMapper taskMapper;

    @Override
    public TaskResponseDto createTask(TaskRequestDto requestDto) {
        log.info("Creating task with title: {}", requestDto.title());

        // Check if task with the same title already exists
        if (taskRepository.existsByTitle(requestDto.title())) {
            throw new TaskAlreadyExistsException("Task already exists with title: " + requestDto.title());
        }

        // Create task entity
        Task task = taskMapper.toEntity(requestDto);

        // Assign donor (mandatory)
        Donor donor = donorRepository.findById(requestDto.donorId())
                .orElseThrow(() -> new DonorNotFoundException(requestDto.donorId()));
        task.setDonor(donor);

        // Assign partner if partnerId is provided
        if (requestDto.assignedPartnerId() != null) {
            Partner partner = partnerRepository.findById(requestDto.assignedPartnerId())
                    .orElseThrow(() -> new PartnerNotFoundException(requestDto.assignedPartnerId()));
            task.setAssignedPartner(partner);
        }

        // Save task
        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with id: {}", savedTask.getId());

        return taskMapper.toResponseDto(savedTask);
    }

    @Override
    public TaskResponseDto updateTask(Long id, TaskRequestDto requestDto) {
        log.info("Updating task with id: {}", id);

        // Find existing task
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        // Check if title is being changed and if new title already exists
        if (!existingTask.getTitle().equals(requestDto.title())
                && taskRepository.existsByTitle(requestDto.title())) {
            throw new TaskAlreadyExistsException("Task already exists with title: " + requestDto.title());
        }

        // Update task fields
        taskMapper.updateEntityFromDto(requestDto, existingTask);

        // Update donor if donorId is provided
        if (requestDto.donorId() != null) {
            Donor donor = donorRepository.findById(requestDto.donorId())
                    .orElseThrow(() -> new DonorNotFoundException(requestDto.donorId()));
            existingTask.setDonor(donor);
        }

        // Update partner if partnerId is provided
        if (requestDto.assignedPartnerId() != null) {
            Partner partner = partnerRepository.findById(requestDto.assignedPartnerId())
                    .orElseThrow(() -> new PartnerNotFoundException(requestDto.assignedPartnerId()));
            existingTask.setAssignedPartner(partner);
        }

        // Save updated task
        Task updatedTask = taskRepository.save(existingTask);
        log.info("Task updated successfully with id: {}", updatedTask.getId());

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        log.info("Fetching task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return taskMapper.toResponseDto(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks() {

        List<Task> tasks = taskRepository.findAll();

        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasksPaginated(Pageable pageable) {
        Page<Task> taskPage = taskRepository.findAll(pageable);

        return taskPage.map(taskMapper::toResponseDto);
    }

    @Override
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        taskRepository.deleteById(id);
        log.info("Task deleted successfully with id: {}", id);
    }

    @Override
    public TaskResponseDto assignPartnerToTask(Long taskId, Long partnerId) {
        log.info("Assigning partner {} to task {}", partnerId, taskId);

        // Find task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // Find partner
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException(partnerId));

        // Assign partner to task
        task.setAssignedPartner(partner);

        // Save task
        Task updatedTask = taskRepository.save(task);
        log.info("Partner assigned successfully to task");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateTaskStatus(Long taskId, String status) {
        log.info("Updating status of task {} to {}", taskId, status);

        // Find task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // Update status using fromString method (case-insensitive)
        TaskStatus taskStatus = TaskStatus.fromString(status);
        if (taskStatus == null) {
            throw new IllegalArgumentException("Invalid task status: " + status);
        }
        task.setTaskStatus(taskStatus);

        // Save task
        Task updatedTask = taskRepository.save(task);
        log.info("Task status updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto moveTaskToNextStatus(Long taskId) {
        log.info("Moving task {} to next status", taskId);

        // Find task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskStatus currentStatus = task.getTaskStatus();
        TaskStatus nextStatus = getNextStatus(currentStatus);

        if (nextStatus == null) {
            throw new IllegalStateException("Task is already in final status: " + currentStatus);
        }

        task.setTaskStatus(nextStatus);
        Task updatedTask = taskRepository.save(task);
        log.info("Task moved from {} to {}", currentStatus, nextStatus);

        return taskMapper.toResponseDto(updatedTask);
    }

    private TaskStatus getNextStatus(TaskStatus currentStatus) {
        return switch (currentStatus) {
            case INITIATED -> TaskStatus.ALLOCATED;
            case ALLOCATED -> TaskStatus.ACCEPTED;
            case ACCEPTED -> TaskStatus.WBS_SUBMITTED;
            case WBS_SUBMITTED -> TaskStatus.CN_DRAFTING;
            case CN_DRAFTING -> TaskStatus.CN_UNDER_REVIEW;
            case CN_UNDER_REVIEW -> TaskStatus.CN_APPROVED;
            case CN_APPROVED -> TaskStatus.INCEPTION_REPORT_PENDING;
            case INCEPTION_REPORT_PENDING -> TaskStatus.EXECUTION;
            case EXECUTION -> TaskStatus.COMPLETED;
            case COMPLETED -> null; // Final status
        };
    }
}
