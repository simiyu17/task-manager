package com.task.manage.task.service;

import com.task.manage.document.domain.Document;
import com.task.manage.document.domain.DocumentRepository;
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
import com.task.manage.task.exception.InvalidStatusException;
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
    private final DocumentRepository documentRepository;
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

        // Validate status transitions
        validateStatusTransition(task, taskStatus);

        task.setTaskStatus(taskStatus);

        // Save task
        Task updatedTask = taskRepository.save(task);
        log.info("Task status updated successfully");

        return taskMapper.toResponseDto(updatedTask);
    }

    private void validateStatusTransition(Task task, TaskStatus newStatus) {
        // Validation: Cannot mark as ALLOCATED if assignee is empty
        if (newStatus == TaskStatus.ALLOCATED && task.getAssignedPartner() == null) {
            throw new InvalidStatusException("Cannot mark task as ALLOCATED: No partner has been assigned to this task");
        }

        // Validation: Cannot mark as ACCEPTED or REJECTED if current status is not ALLOCATED
        if ((newStatus == TaskStatus.ACCEPTED || newStatus == TaskStatus.REJECTED)
                && task.getTaskStatus() != TaskStatus.ALLOCATED) {
            throw new InvalidStatusException("Cannot mark task as " + newStatus.name() + ": Current status must be ALLOCATED");
        }

        // Validation: Cannot mark as WBS_SUBMITTED if no WBS document exists
        if (newStatus == TaskStatus.WBS_SUBMITTED
                && !documentRepository.existsByTaskIdAndDocumentType(task.getId(), Document.DocumentType.WBS)) {
            throw new InvalidStatusException("Cannot mark task as WBS_SUBMITTED: No WBS document has been uploaded for this task");
        }

        // Validation: Cannot mark as CONCEPT_NOTE statuses if no CONCEPT_NOTE document exists
        if ((newStatus == TaskStatus.CONCEPT_NOTE_SUBMITTED
                || newStatus == TaskStatus.CONCEPT_NOTE_UNDER_REVIEW
                || newStatus == TaskStatus.CONCEPT_NOTE_APPROVED
                || newStatus == TaskStatus.CONCEPT_NOTE_REJECTED)
                && !documentRepository.existsByTaskIdAndDocumentType(task.getId(), Document.DocumentType.CONCEPT_NOTE)) {
            throw new InvalidStatusException("Cannot mark task as " + newStatus.name() + ": No CONCEPT_NOTE document has been uploaded for this task");
        }

        // Validation: Cannot mark as INCEPTION_REPORT statuses if no INCEPTION_REPORT document exists
        if ((newStatus == TaskStatus.INCEPTION_REPORT_SUBMITTED
                || newStatus == TaskStatus.INCEPTION_REPORT_UNDER_REVIEW
                || newStatus == TaskStatus.INCEPTION_REPORT_APPROVED
                || newStatus == TaskStatus.INCEPTION_REPORT_REJECTED)
                && !documentRepository.existsByTaskIdAndDocumentType(task.getId(), Document.DocumentType.INCEPTION_REPORT)) {
            throw new InvalidStatusException("Cannot mark task as " + newStatus.name() + ": No INCEPTION_REPORT document has been uploaded for this task");
        }

        // Validation: Cannot mark as EXECUTION_UNDERWAY or COMPLETED if no WBS, CONCEPT_NOTE, or INCEPTION_REPORT exists
        if (newStatus == TaskStatus.EXECUTION_UNDERWAY || newStatus == TaskStatus.COMPLETED) {
            boolean hasWbs = documentRepository.existsByTaskIdAndDocumentType(task.getId(), Document.DocumentType.WBS);
            boolean hasConceptNote = documentRepository.existsByTaskIdAndDocumentType(task.getId(), Document.DocumentType.CONCEPT_NOTE);
            boolean hasInceptionReport = documentRepository.existsByTaskIdAndDocumentType(task.getId(), Document.DocumentType.INCEPTION_REPORT);

            if (!hasWbs && !hasConceptNote && !hasInceptionReport) {
                throw new InvalidStatusException("Cannot mark task as " + newStatus.name() + ": Task must have at least one of WBS, CONCEPT_NOTE, or INCEPTION_REPORT document");
            }
        }
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
