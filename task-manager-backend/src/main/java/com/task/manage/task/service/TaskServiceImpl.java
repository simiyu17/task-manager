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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        if (null == requestDto.donorId() || null == requestDto.title() || requestDto.title().isBlank()) {
            throw new IllegalArgumentException("Donor ID and title are required to create a task");
        }

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
    public TaskResponseDto moveTaskToNextStatus(Long taskId, Boolean rejected) {
        log.info("Moving task {} to next status", taskId);

        // Find task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskStatus currentStatus = task.getTaskStatus();
        TaskStatus nextStatus = getNextStatus(currentStatus, rejected);

        if (nextStatus == null) {
            throw new IllegalStateException("Task is already in final status: " + currentStatus);
        }

        task.setTaskStatus(nextStatus);
        Task updatedTask = taskRepository.save(task);
        log.info("Task moved from {} to {}", currentStatus, nextStatus);

        return taskMapper.toResponseDto(updatedTask);
    }

    private TaskStatus getNextStatus(TaskStatus currentStatus, boolean rejected) {
        return switch (currentStatus) {
            case INITIATED -> TaskStatus.TASK_UNDER_REVIEW;
            case TASK_UNDER_REVIEW -> TaskStatus.REVIEW_COMPLETED;
            case REVIEW_COMPLETED, REJECTED -> TaskStatus.ALLOCATED;
            case ALLOCATED -> rejected ? TaskStatus.REJECTED : TaskStatus.ACCEPTED;
            case ACCEPTED -> TaskStatus.WBS_SUBMITTED;
            case WBS_SUBMITTED -> TaskStatus.CN_DRAFTING;
            case CN_DRAFTING, CN_REJECTED -> TaskStatus.CN_UNDER_REVIEW;
            case CN_UNDER_REVIEW -> rejected ? TaskStatus.CN_REJECTED : TaskStatus.CN_APPROVED;
            case CN_APPROVED -> TaskStatus.INCEPTION_REPORT_PENDING;
            case INCEPTION_REPORT_PENDING -> TaskStatus.EXECUTION;
            case EXECUTION -> TaskStatus.COMPLETED;
            case COMPLETED -> null; // Final status
        };
    }

    @Override
    public TaskResponseDto updateTitle(Long taskId, String title) {
        log.info("Updating title of task {} to {}", taskId, title);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // Check if new title already exists
        if (!task.getTitle().equals(title) && taskRepository.existsByTitle(title)) {
            throw new TaskAlreadyExistsException("Task already exists with title: " + title);
        }

        task.setTitle(title);
        Task updatedTask = taskRepository.save(task);
        log.info("Task title updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateDonor(Long taskId, Long donorId) {
        log.info("Updating donor of task {} to donor {}", taskId, donorId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new DonorNotFoundException(donorId));

        task.setDonor(donor);
        Task updatedTask = taskRepository.save(task);
        log.info("Task donor updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateDescription(Long taskId, String description) {
        log.info("Updating description of task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setDescription(description);
        Task updatedTask = taskRepository.save(task);
        log.info("Task description updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateValidatedBudget(Long taskId, BigDecimal validatedBudget) {
        log.info("Updating validated budget of task {} to {}", taskId, validatedBudget);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setValidatedBudget(validatedBudget);
        Task updatedTask = taskRepository.save(task);
        log.info("Task validated budget updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateRequestReceivedAt(Long taskId, LocalDateTime requestReceivedAt) {
        log.info("Updating request received at of task {} to {}", taskId, requestReceivedAt);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setRequestReceivedAt(requestReceivedAt);
        Task updatedTask = taskRepository.save(task);
        log.info("Task request received at updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateAcceptedAt(Long taskId, LocalDateTime acceptedAt) {
        log.info("Updating accepted at of task {} to {}", taskId, acceptedAt);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setAcceptedAt(acceptedAt);
        Task updatedTask = taskRepository.save(task);
        log.info("Task accepted at updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateDeadline(Long taskId, LocalDateTime deadline) {
        log.info("Updating deadline of task {} to {}", taskId, deadline);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setDeadline(deadline);
        Task updatedTask = taskRepository.save(task);
        log.info("Task deadline updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateAllocateNotes(Long taskId, String allocateNotes) {
        log.info("Updating allocate notes of task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setAllocateNotes(allocateNotes);
        Task updatedTask = taskRepository.save(task);
        log.info("Task allocate notes updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateAcceptanceNotes(Long taskId, String acceptanceNotes) {
        log.info("Updating acceptance notes of task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setAcceptanceNotes(acceptanceNotes);
        Task updatedTask = taskRepository.save(task);
        log.info("Task acceptance notes updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    public TaskResponseDto updateRejectionNotes(Long taskId, String rejectionNotes) {
        log.info("Updating rejection notes of task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setRejectionNotes(rejectionNotes);
        Task updatedTask = taskRepository.save(task);
        log.info("Task rejection notes updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }
}
