import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api.service';
import {
  TaskReviewRequestDto,
  TaskReviewResponseDto,
  ReviewCommentRequestDto,
  ReviewCommentResponseDto,
  ClarifyingQuestionRequestDto,
  ClarifyingQuestionResponseDto,
  AnswerQuestionRequestDto
} from './review-dtos';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private readonly API_PATH = 'reviews';

  constructor(private apiService: ApiService) {}

  /**
   * Create a new task review
   */
  createReview(reviewRequest: TaskReviewRequestDto): Observable<TaskReviewResponseDto> {
    return this.apiService.post<TaskReviewResponseDto>(this.API_PATH, reviewRequest);
  }

  /**
   * Get all reviews for a specific task
   */
  getReviewsByTaskId(taskId: number): Observable<TaskReviewResponseDto[]> {
    return this.apiService.get<TaskReviewResponseDto[]>(`${this.API_PATH}/task/${taskId}`);
  }

  /**
   * Get a specific review by ID
   */
  getReviewById(reviewId: number): Observable<TaskReviewResponseDto> {
    return this.apiService.get<TaskReviewResponseDto>(`${this.API_PATH}/${reviewId}`);
  }

  /**
   * Update review status
   */
  updateReviewStatus(reviewId: number, status: string): Observable<TaskReviewResponseDto> {
    return this.apiService.patch<TaskReviewResponseDto>(
      `${this.API_PATH}/${reviewId}/status`,
      null,
      { params: { status } }
    );
  }

  /**
   * Add a comment to a review
   */
  addComment(commentRequest: ReviewCommentRequestDto): Observable<ReviewCommentResponseDto> {
    return this.apiService.post<ReviewCommentResponseDto>(`${this.API_PATH}/comments`, commentRequest);
  }

  /**
   * Get all comments for a review
   */
  getCommentsByReviewId(reviewId: number): Observable<ReviewCommentResponseDto[]> {
    return this.apiService.get<ReviewCommentResponseDto[]>(`${this.API_PATH}/${reviewId}/comments`);
  }

  /**
   * Add a clarifying question to a review
   */
  addClarifyingQuestion(questionRequest: ClarifyingQuestionRequestDto): Observable<ClarifyingQuestionResponseDto> {
    return this.apiService.post<ClarifyingQuestionResponseDto>(`${this.API_PATH}/questions`, questionRequest);
  }

  /**
   * Answer a clarifying question
   */
  answerQuestion(questionId: number, answerRequest: AnswerQuestionRequestDto): Observable<ClarifyingQuestionResponseDto> {
    return this.apiService.patch<ClarifyingQuestionResponseDto>(
      `${this.API_PATH}/questions/${questionId}/answer`,
      answerRequest
    );
  }

  /**
   * Get all questions for a review
   */
  getQuestionsByReviewId(reviewId: number): Observable<ClarifyingQuestionResponseDto[]> {
    return this.apiService.get<ClarifyingQuestionResponseDto[]>(`${this.API_PATH}/${reviewId}/questions`);
  }

  /**
   * Get unanswered questions for a review
   */
  getUnansweredQuestionsByReviewId(reviewId: number): Observable<ClarifyingQuestionResponseDto[]> {
    return this.apiService.get<ClarifyingQuestionResponseDto[]>(`${this.API_PATH}/${reviewId}/questions/unanswered`);
  }
}
