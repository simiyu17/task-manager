package com.task.manage.task.domain;

import com.task.manage.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;


    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private Task.TaskStatus reviewStatus;

    @Column(name = "review_cycle")
    private Integer reviewCycle;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "overall_comment", columnDefinition = "TEXT")
    private String overallComment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskReview that = (TaskReview) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(task, that.task)
                .append(reviewCycle, that.reviewCycle)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(task)
                .append(reviewCycle)
                .toHashCode();
    }
}
