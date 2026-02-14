import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api.service';
import { TaskRequestDto } from '../../views/base/tasks/dto/task-request-dto';
import { DonorResponseDto } from '../donor/donor.service';

export interface TaskResponse {
  id: number;
  title: string;
  donor: DonorResponseDto;
  description: string;
  assignedPartner?: PartnerResponseDto;
  taskStatus: string;
  validatedBudget?: number;
  requestReceivedAt?: string;
  acceptedAt?: string;
  deadline?: string;
  dateCreated: string;
  lastModified: string;
  createdBy: string;
  lastModifiedBy: string;
  allocateNotes?: string;
  acceptanceNotes?: string;
  rejectionNotes?: string;
  progress?: number;
  priority?: string;
}

export interface PartnerResponseDto {
id: number;
partnerName: string;
// Add other partner fields as needed
}

export interface DocumentUploadResponse {
id: string;
  documentId: string;
  message: string;
}

export interface PagedTaskResponse {
  content: TaskResponse[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private readonly TASKS_ENDPOINT = 'tasks';

  constructor(private apiService: ApiService) {}

  /**
   * Create a new task
   * @param taskData - Task request data
   */
  createTask(taskData: TaskRequestDto): Observable<TaskResponse> {
    return this.apiService.post<TaskResponse>(this.TASKS_ENDPOINT, taskData);
  }

  /**
   * Get task by ID
   * @param taskId - Task ID
   */
  getTask(taskId: string): Observable<TaskResponse> {
    return this.apiService.get<TaskResponse>(`${this.TASKS_ENDPOINT}/${taskId}`);
  }

  /**
   * Get all tasks
   */
  getAllTasks(): Observable<TaskResponse[]> {
    return this.apiService.get<TaskResponse[]>(this.TASKS_ENDPOINT);
  }

  /**
   * Update task
   * @param taskId - Task ID
   * @param taskData - Updated task data
   */
  updateTask(taskId: string, taskData: Partial<TaskRequestDto>): Observable<TaskResponse> {
    return this.apiService.put<TaskResponse>(`${this.TASKS_ENDPOINT}/${taskId}`, taskData);
  }

  /**
   * Delete task
   * @param taskId - Task ID
   */
  deleteTask(taskId: string): Observable<void> {
    return this.apiService.delete<void>(`${this.TASKS_ENDPOINT}/${taskId}`);
  }

  /**
   * Upload document for a task
   * @param taskId - Task ID
   * @param formData - Form data containing file and metadata
   */
  uploadDocument(taskId: string, formData: FormData): Observable<DocumentUploadResponse> {
    return this.apiService.post<DocumentUploadResponse>(
      `${this.TASKS_ENDPOINT}/${taskId}/documents`,
      formData
    );
  }

  /**
   * Save task as draft
   * @param taskId - Task ID
   * @param draftData - Draft data
   */
  saveAsDraft(taskId: string, draftData: any): Observable<TaskResponse> {
    return this.apiService.post<TaskResponse>(
      `${this.TASKS_ENDPOINT}/${taskId}/draft`,
      draftData
    );
  }

  /**
   * Get all tasks with pagination
   * @param page - Page number (0-indexed)
   * @param size - Page size
   * @param sort - Sort criteria (e.g., 'dateCreated,desc')
   */
  getAllTasksPaginated(page: number = 0, size: number = 10, sort: string = 'dateCreated'): Observable<PagedTaskResponse> {
    const params = {
      page: page.toString(),
      size: size.toString(),
      sort: sort
    };
    return this.apiService.get<PagedTaskResponse>(`${this.TASKS_ENDPOINT}/paginated`, { params });
  }

  /**
   * Assign a partner to a task
   * @param taskId - Task ID
   * @param partnerId - Partner ID
   */
  assignPartnerToTask(taskId: string, partnerId: string): Observable<TaskResponse> {
    return this.apiService.patch<TaskResponse>(
      `${this.TASKS_ENDPOINT}/${taskId}/assign-partner/${partnerId}`,
      null
    );
  }

  /**
   * Update task status
   * @param taskId - Task ID
   * @param status - New status
   */
  updateTaskStatus(taskId: string, status: string): Observable<TaskResponse> {
    return this.apiService.patch<TaskResponse>(
      `${this.TASKS_ENDPOINT}/${taskId}/status`,
      null,
      { params: { status } }
    );
  }

  /**
   * Move task to next status in workflow
   * @param taskId - Task ID
   */
  moveTaskToNextStatus(taskId: string): Observable<TaskResponse> {
    return this.apiService.patch<TaskResponse>(
      `${this.TASKS_ENDPOINT}/${taskId}/next-status`,
      null
    );
  }
}
