package com.task.manage.task.domain;

import com.fasterxml.jackson.annotation.JsonValue;
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

    @Column(name = "reviewer_name", nullable = false)
    private String reviewerName;

    @Column(name = "reviewer_email")
    private String reviewerEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private ReviewStatus reviewStatus;

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
                .append(reviewerName, that.reviewerName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(task)
                .append(reviewCycle)
                .append(reviewerName)
                .toHashCode();
    }

    @Getter
    public enum ReviewStatus {
        PENDING, IN_PROGRESS, APPROVED, REJECTED, REQUIRES_CLARIFICATION;

        @JsonValue
        public String getName() {
            return this.name();
        }

        public String getDisplayName() {
            return this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
        }

        /**
         * Get ReviewStatus from string value (case-insensitive)
         * @param value the string value
         * @return ReviewStatus or null if not found
         */
        public static ReviewStatus fromString(String value) {
            if (value == null) {
                return null;
            }
            for (ReviewStatus status : ReviewStatus.values()) {
                if (status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
            return null;
        }
    }
}
