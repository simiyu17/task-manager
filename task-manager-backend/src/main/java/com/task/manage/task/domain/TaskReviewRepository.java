package com.task.manage.task.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskReviewRepository extends JpaRepository<TaskReview, Long> {

    List<TaskReview> findByTaskId(Long taskId);

    List<TaskReview> findByTaskIdOrderByReviewCycleDesc(Long taskId);

    @Query("SELECT MAX(tr.reviewCycle) FROM TaskReview tr WHERE tr.task.id = :taskId")
    Optional<Integer> findMaxReviewCycleByTaskId(Long taskId);

    Optional<TaskReview> findByTaskIdAndReviewCycle(Long taskId, Integer reviewCycle);
}
