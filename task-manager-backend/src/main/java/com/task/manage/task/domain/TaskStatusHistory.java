package com.task.manage.task.domain;

import com.task.manage.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "task_status_history", indexes = {
    @Index(name = "idx_task_id", columnList = "task_id"),
    @Index(name = "idx_to_status", columnList = "to_status"),
    @Index(name = "idx_changed_at", columnList = "changed_at")
})
@Getter
@Setter
@NoArgsConstructor
public class TaskStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private Task.TaskStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private Task.TaskStatus toStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "duration_in_previous_status_hours")
    private Long durationInPreviousStatusHours;

    public TaskStatusHistory(Task task, Task.TaskStatus fromStatus, Task.TaskStatus toStatus, String changedBy) {
        this.task = task;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TaskStatusHistory that = (TaskStatusHistory) o;
        return Objects.equals(task, that.task) &&
               Objects.equals(changedAt, that.changedAt) &&
               toStatus == that.toStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), task, changedAt, toStatus);
    }
}

