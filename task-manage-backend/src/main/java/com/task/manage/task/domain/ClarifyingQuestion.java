package com.task.manage.task.domain;

import com.task.manage.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "clarifying_questions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClarifyingQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_review_id", nullable = false)
    private TaskReview taskReview;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "questioner_name", nullable = false)
    private String questionerName;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "answered_by")
    private String answeredBy;

    @Column(name = "is_answered")
    private boolean isAnswered;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClarifyingQuestion that = (ClarifyingQuestion) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(taskReview, that.taskReview)
                .append(questionText, that.questionText)
                .append(questionerName, that.questionerName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(taskReview)
                .append(questionText)
                .append(questionerName)
                .toHashCode();
    }
}
