package com.task.manage.task.domain;

import com.task.manage.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "review_comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_review_id", nullable = false)
    private TaskReview taskReview;

    @Column(name = "comment_text", columnDefinition = "TEXT", nullable = false)
    private String commentText;

    @Column(name = "section_reference")
    private String sectionReference;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewComment that = (ReviewComment) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(taskReview, that.taskReview)
                .append(commentText, that.commentText)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(taskReview)
                .append(commentText)
                .toHashCode();
    }
}
