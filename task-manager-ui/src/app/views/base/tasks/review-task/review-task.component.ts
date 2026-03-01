import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  CardComponent,
  CardHeaderComponent,
  CardBodyComponent,
  ButtonDirective,
  FormDirective,
  FormControlDirective,
  FormSelectDirective,
  ColComponent,
  RowComponent,
  AccordionModule
} from '@coreui/angular';
import { ReviewService } from '../../../../services/review/review.service';
import {
  TaskReviewResponseDto,
  TaskReviewRequestDto,
  ReviewCommentRequestDto,
  ClarifyingQuestionRequestDto,
  AnswerQuestionRequestDto,
  ReviewStatus
} from '../../../../services/review/review-dtos';

@Component({
  selector: 'app-review-task',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    FormDirective,
    FormControlDirective,
    FormSelectDirective,
    ColComponent,
    RowComponent,
    AccordionModule
  ],
  templateUrl: './review-task.component.html',
  styleUrl: './review-task.component.scss'
})
export class ReviewTaskComponent implements OnInit {
  @Input() taskId!: number;
  @Output() reviewSaved = new EventEmitter<void>();

  reviews: TaskReviewResponseDto[] = [];
  isLoadingReviews = false;
  reviewForm: FormGroup;
  commentForm: FormGroup;
  questionForm: FormGroup;
  answerForm: FormGroup;
  
  selectedReviewId: number | null = null;
  selectedQuestionId: number | null = null;
  showReviewForm = false;
  showCommentForm: { [key: number]: boolean } = {};
  showQuestionForm: { [key: number]: boolean } = {};
  showAnswerForm: { [key: number]: boolean } = {};
  
  reviewStatuses = Object.values(ReviewStatus);
  createdBy = 'Current User'; // TODO: Get from auth service

  constructor(
    private fb: FormBuilder,
    private reviewService: ReviewService
  ) {
    this.reviewForm = this.fb.group({
      reviewStatus: ['', Validators.required],
      overallComment: ['']
    });

    this.commentForm = this.fb.group({
      commentText: ['', Validators.required],
      sectionReference: ['']
    });

    this.questionForm = this.fb.group({
      questionText: ['', Validators.required]
    });

    this.answerForm = this.fb.group({
      answerText: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.taskId) {
      this.loadReviews();
    }
  }

  loadReviews(): void {
    this.isLoadingReviews = true;
    this.reviewService.getReviewsByTaskId(this.taskId).subscribe({
      next: (reviews) => {
        // Sort reviews by date (latest first)
        this.reviews = reviews.sort((a, b) => 
          new Date(b.reviewedAt).getTime() - new Date(a.reviewedAt).getTime()
        );
        // Sort comments within each review (oldest first)
        this.reviews.forEach(review => {
          review.comments = review.comments.sort((a, b) => a.id - b.id);
          review.clarifyingQuestions = review.clarifyingQuestions.sort((a, b) => a.id - b.id);
        });
        this.isLoadingReviews = false;
      },
      error: (error) => {
        console.error('Error loading reviews:', error);
        this.isLoadingReviews = false;
      }
    });
  }

  toggleReviewForm(): void {
    this.showReviewForm = !this.showReviewForm;
    if (!this.showReviewForm) {
      this.reviewForm.reset();
    }
  }

  submitReview(): void {
    if (this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      return;
    }

    const reviewRequest: TaskReviewRequestDto = {
      taskId: this.taskId,
      reviewStatus: this.reviewForm.value.reviewStatus,
      overallComment: this.reviewForm.value.overallComment || undefined
    };

    this.reviewService.createReview(reviewRequest).subscribe({
      next: () => {
        this.loadReviews();
        this.reviewForm.reset();
        this.showReviewForm = false;
        this.reviewSaved.emit();
      },
      error: (error) => {
        console.error('Error creating review:', error);
        alert('Failed to create review. Please try again.');
      }
    });
  }

  toggleCommentForm(reviewId: number): void {
    this.showCommentForm[reviewId] = !this.showCommentForm[reviewId];
    if (!this.showCommentForm[reviewId]) {
      this.commentForm.reset();
    }
    this.selectedReviewId = reviewId;
  }

  submitComment(reviewId: number): void {
    if (this.commentForm.invalid) {
      this.commentForm.markAllAsTouched();
      return;
    }

    const commentRequest: ReviewCommentRequestDto = {
      taskReviewId: reviewId,
      commentText: this.commentForm.value.commentText,
      sectionReference: this.commentForm.value.sectionReference || undefined
    };

    this.reviewService.addComment(commentRequest).subscribe({
      next: () => {
        this.loadReviews();
        this.commentForm.reset();
        this.showCommentForm[reviewId] = false;
      },
      error: (error) => {
        console.error('Error adding comment:', error);
        alert('Failed to add comment. Please try again.');
      }
    });
  }

  toggleQuestionForm(reviewId: number): void {
    this.showQuestionForm[reviewId] = !this.showQuestionForm[reviewId];
    if (!this.showQuestionForm[reviewId]) {
      this.questionForm.reset();
    }
    this.selectedReviewId = reviewId;
  }

  submitQuestion(reviewId: number): void {
    if (this.questionForm.invalid) {
      this.questionForm.markAllAsTouched();
      return;
    }

    const questionRequest: ClarifyingQuestionRequestDto = {
      taskReviewId: reviewId,
      questionText: this.questionForm.value.questionText
    };

    this.reviewService.addClarifyingQuestion(questionRequest).subscribe({
      next: () => {
        this.loadReviews();
        this.questionForm.reset();
        this.showQuestionForm[reviewId] = false;
      },
      error: (error) => {
        console.error('Error adding question:', error);
        alert('Failed to add question. Please try again.');
      }
    });
  }

  toggleAnswerForm(questionId: number): void {
    this.showAnswerForm[questionId] = !this.showAnswerForm[questionId];
    if (!this.showAnswerForm[questionId]) {
      this.answerForm.reset({ answeredBy: this.createdBy });
    }
    this.selectedQuestionId = questionId;
  }

  submitAnswer(questionId: number): void {
    if (this.answerForm.invalid) {
      this.answerForm.markAllAsTouched();
      return;
    }

    const answerRequest: AnswerQuestionRequestDto = {
      answerText: this.answerForm.value.answerText,
      answeredBy: this.answerForm.value.answeredBy
    };

    this.reviewService.answerQuestion(questionId, answerRequest).subscribe({
      next: () => {
        this.loadReviews();
        this.answerForm.reset({ answeredBy: this.createdBy });
        this.showAnswerForm[questionId] = false;
      },
      error: (error) => {
        console.error('Error answering question:', error);
        alert('Failed to answer question. Please try again.');
      }
    });
  }

  getStatusBadgeColor(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'success';
      case 'CHANGES_REQUESTED':
        return 'warning';
      case 'REJECTED':
        return 'danger';
      case 'PENDING':
      default:
        return 'secondary';
    }
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
