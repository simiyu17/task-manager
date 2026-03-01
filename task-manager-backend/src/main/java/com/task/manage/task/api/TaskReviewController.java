package com.task.manage.task.api;

import com.task.manage.task.domain.Task;
import com.task.manage.task.dto.*;
import com.task.manage.task.service.TaskReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class TaskReviewController {

    private final TaskReviewService reviewService;

    @PostMapping
    public ResponseEntity<TaskReviewResponseDto> createReview(@Valid @RequestBody TaskReviewRequestDto requestDto) {
        TaskReviewResponseDto response = reviewService.createReview(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskReviewResponseDto>> getReviewsByTaskId(
            @PathVariable Long taskId) {
        List<TaskReviewResponseDto> reviews = reviewService.getReviewsByTaskId(taskId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<TaskReviewResponseDto> getReviewById(
            @PathVariable Long reviewId) {
        TaskReviewResponseDto review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @PatchMapping("/{reviewId}/status")
    public ResponseEntity<TaskReviewResponseDto> updateReviewStatus(
            @PathVariable Long reviewId,
            @RequestParam String status) {
        var reviewStatus = Task.TaskStatus.fromString(status);
        if (reviewStatus == null) {
            throw new IllegalArgumentException("Invalid review status: " + status);
        }
        TaskReviewResponseDto response = reviewService.updateReviewStatus(reviewId, reviewStatus);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/comments")
    public ResponseEntity<ReviewCommentResponseDto> addComment(@Valid @RequestBody ReviewCommentRequestDto requestDto) {
        ReviewCommentResponseDto response = reviewService.addComment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<List<ReviewCommentResponseDto>> getCommentsByReviewId(
            @PathVariable Long reviewId) {
        List<ReviewCommentResponseDto> comments = reviewService.getCommentsByReviewId(reviewId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/questions")
    public ResponseEntity<ClarifyingQuestionResponseDto> addClarifyingQuestion(
            @Valid @RequestBody ClarifyingQuestionRequestDto requestDto) {
        ClarifyingQuestionResponseDto response = reviewService.addClarifyingQuestion(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/questions/{questionId}/answer")
    public ResponseEntity<ClarifyingQuestionResponseDto> answerQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerQuestionRequestDto requestDto) {
        ClarifyingQuestionResponseDto response = reviewService.answerQuestion(questionId, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}/questions")
    public ResponseEntity<List<ClarifyingQuestionResponseDto>> getQuestionsByReviewId(
            @PathVariable Long reviewId) {
        List<ClarifyingQuestionResponseDto> questions = reviewService.getQuestionsByReviewId(reviewId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{reviewId}/questions/unanswered")
    public ResponseEntity<List<ClarifyingQuestionResponseDto>> getUnansweredQuestionsByReviewId(
            @PathVariable Long reviewId) {
        List<ClarifyingQuestionResponseDto> questions = reviewService.getUnansweredQuestionsByReviewId(reviewId);
        return ResponseEntity.ok(questions);
    }
}
