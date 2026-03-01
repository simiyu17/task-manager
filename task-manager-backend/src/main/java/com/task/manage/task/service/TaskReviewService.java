package com.task.manage.task.service;

import com.task.manage.task.domain.*;
import com.task.manage.task.dto.*;
import com.task.manage.task.exception.QuestionNotFoundException;
import com.task.manage.task.exception.ReviewNotFoundException;
import com.task.manage.task.exception.TaskNotFoundException;
import com.task.manage.task.mapper.ClarifyingQuestionMapper;
import com.task.manage.task.mapper.ReviewCommentMapper;
import com.task.manage.task.mapper.TaskReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskReviewService {

    private final TaskReviewRepository taskReviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ClarifyingQuestionRepository clarifyingQuestionRepository;
    private final TaskRepository taskRepository;
    private final TaskReviewMapper taskReviewMapper;
    private final ReviewCommentMapper reviewCommentMapper;
    private final ClarifyingQuestionMapper clarifyingQuestionMapper;

    public TaskReviewResponseDto createReview(TaskReviewRequestDto requestDto) {
        log.info("Creating review for task: {}", requestDto.taskId());

        // Validate task exists
        Task task = taskRepository.findById(requestDto.taskId())
                .orElseThrow(() -> new TaskNotFoundException(requestDto.taskId()));

        // Convert string to enum
        var reviewStatus = Task.TaskStatus.fromString(requestDto.reviewStatus());
        if (reviewStatus == null) {
            throw new IllegalArgumentException("Invalid review status: " + requestDto.reviewStatus());
        }

        // Get next review cycle
        Integer reviewCycle = taskReviewRepository.findMaxReviewCycleByTaskId(requestDto.taskId())
                .map(cycle -> cycle + 1)
                .orElse(1);

        // Create review
        TaskReview review = TaskReview.builder()
                .task(task)
                .reviewStatus(reviewStatus)
                .reviewCycle(reviewCycle)
                .reviewedAt(LocalDateTime.now())
                .overallComment(requestDto.overallComment())
                .build();

        TaskReview savedReview = taskReviewRepository.save(review);
        log.info("Review created successfully with id: {}", savedReview.getId());

        return toResponseDtoWithChildren(savedReview);
    }

    @Transactional(readOnly = true)
    public List<TaskReviewResponseDto> getReviewsByTaskId(Long taskId) {
        log.info("Fetching reviews for task: {}", taskId);

        List<TaskReview> reviews = taskReviewRepository.findByTaskIdOrderByReviewCycleDesc(taskId);
        return reviews.stream()
                .map(this::toResponseDtoWithChildren)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskReviewResponseDto getReviewById(Long reviewId) {
        log.info("Fetching review with id: {}", reviewId);

        TaskReview review = taskReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        return toResponseDtoWithChildren(review);
    }

    public TaskReviewResponseDto updateReviewStatus(Long reviewId, Task.TaskStatus status) {
        log.info("Updating review status for id: {} to {}", reviewId, status);

        TaskReview review = taskReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        review.setReviewStatus(status);
        TaskReview updatedReview = taskReviewRepository.save(review);

        return toResponseDtoWithChildren(updatedReview);
    }

    public ReviewCommentResponseDto addComment(ReviewCommentRequestDto requestDto) {
        log.info("Adding comment to review: {}", requestDto.taskReviewId());

        TaskReview review = taskReviewRepository.findById(requestDto.taskReviewId())
                .orElseThrow(() -> new ReviewNotFoundException(requestDto.taskReviewId()));

        ReviewComment comment = ReviewComment.builder()
                .taskReview(review)
                .commentText(requestDto.commentText())
                .sectionReference(requestDto.sectionReference())
                .build();

        ReviewComment savedComment = reviewCommentRepository.save(comment);
        log.info("Comment added successfully with id: {}", savedComment.getId());

        return reviewCommentMapper.toResponseDto(savedComment);
    }

    @Transactional(readOnly = true)
    public List<ReviewCommentResponseDto> getCommentsByReviewId(Long reviewId) {
        log.info("Fetching comments for review: {}", reviewId);

        List<ReviewComment> comments = reviewCommentRepository.findByTaskReviewId(reviewId);
        return comments.stream()
                .map(reviewCommentMapper::toResponseDto)
                .toList();
    }

    public ClarifyingQuestionResponseDto addClarifyingQuestion(ClarifyingQuestionRequestDto requestDto) {
        log.info("Adding clarifying question to review: {}", requestDto.taskReviewId());

        TaskReview review = taskReviewRepository.findById(requestDto.taskReviewId())
                .orElseThrow(() -> new ReviewNotFoundException(requestDto.taskReviewId()));

        ClarifyingQuestion question = ClarifyingQuestion.builder()
                .taskReview(review)
                .questionText(requestDto.questionText())
                .isAnswered(false)
                .build();

        ClarifyingQuestion savedQuestion = clarifyingQuestionRepository.save(question);
        log.info("Clarifying question added successfully with id: {}", savedQuestion.getId());

        return clarifyingQuestionMapper.toResponseDto(savedQuestion);
    }

    public ClarifyingQuestionResponseDto answerQuestion(Long questionId, AnswerQuestionRequestDto requestDto) {
        log.info("Answering clarifying question with id: {}", questionId);

        ClarifyingQuestion question = clarifyingQuestionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        question.setAnswerText(requestDto.answerText());
        question.setAnswered(true);

        ClarifyingQuestion updatedQuestion = clarifyingQuestionRepository.save(question);
        log.info("Question answered successfully");

        return clarifyingQuestionMapper.toResponseDto(updatedQuestion);
    }

    @Transactional(readOnly = true)
    public List<ClarifyingQuestionResponseDto> getQuestionsByReviewId(Long reviewId) {
        log.info("Fetching clarifying questions for review: {}", reviewId);

        List<ClarifyingQuestion> questions = clarifyingQuestionRepository.findByTaskReviewId(reviewId);
        return questions.stream()
                .map(clarifyingQuestionMapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClarifyingQuestionResponseDto> getUnansweredQuestionsByReviewId(Long reviewId) {
        log.info("Fetching unanswered clarifying questions for review: {}", reviewId);

        List<ClarifyingQuestion> questions = clarifyingQuestionRepository.findByTaskReviewIdAndIsAnswered(reviewId, false);
        return questions.stream()
                .map(clarifyingQuestionMapper::toResponseDto)
                .toList();
    }

    private TaskReviewResponseDto toResponseDtoWithChildren(TaskReview review) {
        // Get base DTO from mapper
        TaskReviewResponseDto baseDto = taskReviewMapper.toResponseDto(review);

        // Load and map children
        List<ReviewCommentResponseDto> comments = reviewCommentRepository.findByTaskReviewId(review.getId())
                .stream()
                .map(reviewCommentMapper::toResponseDto)
                .toList();

        List<ClarifyingQuestionResponseDto> questions = clarifyingQuestionRepository.findByTaskReviewId(review.getId())
                .stream()
                .map(clarifyingQuestionMapper::toResponseDto)
                .toList();

        // Return DTO with children
        return new TaskReviewResponseDto(
                baseDto.id(),
                baseDto.taskId(),
                baseDto.reviewStatus(),
                baseDto.reviewCycle(),
                baseDto.reviewedAt(),
                baseDto.overallComment(),
                baseDto.createdBy(),
                comments,
                questions
        );
    }
}
