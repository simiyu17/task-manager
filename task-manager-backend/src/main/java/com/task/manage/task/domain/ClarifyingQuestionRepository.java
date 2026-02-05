package com.task.manage.task.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClarifyingQuestionRepository extends JpaRepository<ClarifyingQuestion, Long> {

    List<ClarifyingQuestion> findByTaskReviewId(Long taskReviewId);

    List<ClarifyingQuestion> findByTaskReviewIdAndIsAnswered(Long taskReviewId, boolean isAnswered);
}
