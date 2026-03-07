package com.task.manage.task.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.task.manage.donor.domain.Donor;
import com.task.manage.partner.domain.Partner;
import com.task.manage.shared.domain.BaseEntity;
import com.task.manage.task.dto.TaskStatusDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table(name = "tasks", uniqueConstraints = { @UniqueConstraint(columnNames = { "title" }, name = "TITLE_UNIQUE")})
@Getter
@Setter
@NoArgsConstructor
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id")
    private Donor donor;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_partner_id")
    private Partner assignedPartner;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus taskStatus = TaskStatus.INITIATED;

    @Column(name = "validated_budget")
    private BigDecimal validatedBudget;

    @Column(name = "request_received_at")
    private LocalDateTime requestReceivedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "allocate_notes", columnDefinition = "TEXT")
    private String allocateNotes;

    @Column(name = "acceptance_notes", columnDefinition = "TEXT")
    private String acceptanceNotes;

    @Column(name = "rejection_notes", columnDefinition = "TEXT")
    private String rejectionNotes;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskStatusHistory> statusHistory = new ArrayList<>();

    /**
     * Change task status and record the change in history
     * @param newStatus The new status to change to
     * @param changedBy The user making the change
     * @param notes Optional notes about the status change
     */
    public void changeStatus(TaskStatus newStatus, String changedBy, String notes) {
        if (this.taskStatus != newStatus) {
            TaskStatusHistory history = new TaskStatusHistory(this, this.taskStatus, newStatus, changedBy);
            history.setNotes(notes);

            // Calculate duration in previous status
            if (this.taskStatus != null) {
                LocalDateTime lastChangeTime = getLastStatusChangeTime();
                if (lastChangeTime != null) {
                    long hours = ChronoUnit.HOURS.between(lastChangeTime, LocalDateTime.now());
                    history.setDurationInPreviousStatusHours(hours);
                }
            }

            this.statusHistory.add(history);
            this.taskStatus = newStatus;
        }
    }

    /**
     * Get the timestamp of the last status change
     */
    private LocalDateTime getLastStatusChangeTime() {
        if (statusHistory.isEmpty()) {
            return this.getDateCreated() != null ?
                LocalDateTime.ofInstant(this.getDateCreated(), java.time.ZoneId.systemDefault()) :
                LocalDateTime.now();
        }
        return statusHistory.getLast().getChangedAt();
    }

    public List<TaskStatusDto> getTaskPossibleNextStatuses() {
        return Stream.of(TaskStatus.values())
                .filter(status -> !status.equals(this.taskStatus))
                .map(status -> new TaskStatusDto(status.getStepValue(), status.name(), status.getDisplayName()))
                .sorted(Comparator.comparingInt(TaskStatusDto::stepValue))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(title, task.title)
                .append(donor, task.donor)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(title)
                .append(donor)
                .toHashCode();
    }


    @Getter
    public enum TaskStatus {
        INITIATED(1), TASK_UNDER_REVIEW(2), ALLOCATED(3), ACCEPTED(4), REJECTED(5),
        WBS_SUBMITTED(6),
        CONCEPT_NOTE_SUBMITTED(7), CONCEPT_NOTE_UNDER_REVIEW(8), CONCEPT_NOTE_APPROVED(9), CONCEPT_NOTE_REJECTED(10),
        INCEPTION_REPORT_SUBMITTED(11), INCEPTION_REPORT_UNDER_REVIEW(12), INCEPTION_REPORT_APPROVED(13), INCEPTION_REPORT_REJECTED(14),
        EXECUTION_UNDERWAY(15), COMPLETED(16);

        private final Integer stepValue;

        TaskStatus(Integer stepValue) {
            this.stepValue = stepValue;
        }

        @JsonValue
        public String getName() {
            return this.name();
        }

        public String getDisplayName() {
            return this.name().replace('_', ' ');
        }

        /**
         * Get TaskStatus from string value (case-insensitive)
         * @param value the string value
         * @return TaskStatus or null if not found
         */
        public static TaskStatus fromString(String value) {
            if (value == null) {
                return null;
            }
            for (TaskStatus status : TaskStatus.values()) {
                if (status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
            return null;
        }
    }

}
