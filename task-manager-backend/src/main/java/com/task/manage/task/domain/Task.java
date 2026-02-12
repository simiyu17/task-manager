package com.task.manage.task.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.task.manage.donor.domain.Donor;
import com.task.manage.partner.domain.Partner;
import com.task.manage.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        INITIATED, TASK_UNDER_REVIEW, ALLOCATED, ACCEPTED, WBS_SUBMITTED,
        CN_DRAFTING, CN_UNDER_REVIEW, CN_APPROVED,
        INCEPTION_REPORT_PENDING, EXECUTION, COMPLETED;

        @JsonValue
        public String getName() {
            return this.name();
        }

        public String getDisplayName() {
            return this.name().charAt(0) + this.name().substring(1).toLowerCase().replace('_', ' ');
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
