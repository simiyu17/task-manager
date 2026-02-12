export interface TaskReviewRequestDto {
  taskId: number;
  reviewStatus: string;
  overallComment?: string;
}

export interface ReviewCommentResponseDto {
  id: number;
  taskReviewId: number;
  commentText: string;
  sectionReference?: string;
  createdBy: string;
}

export interface ClarifyingQuestionResponseDto {
  id: number;
  taskReviewId: number;
  questionText: string;
  answerText?: string;
  answeredBy?: string;
  isAnswered: boolean;
  createdBy: string;
}

export interface TaskReviewResponseDto {
  id: number;
  taskId: number;
  reviewStatus: string;
  reviewCycle: number;
  reviewedAt: string;
  overallComment?: string;
  createdBy: string;
  comments: ReviewCommentResponseDto[];
  clarifyingQuestions: ClarifyingQuestionResponseDto[];
}

export interface TaskStatusUpdateRequestDto {
  taskStatus: string;
}

export interface ReviewCommentRequestDto {
  taskReviewId: number;
  commentText: string;
  sectionReference?: string;
}

export interface ClarifyingQuestionRequestDto {
  taskReviewId: number;
  questionText: string;
}

export interface AnswerQuestionRequestDto {
  answerText: string;
  answeredBy: string;
}

export enum ReviewStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  CHANGES_REQUESTED = 'CHANGES_REQUESTED',
  REJECTED = 'REJECTED'
}
