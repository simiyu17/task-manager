import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api.service';
import { TaskRequestDto } from '../../views/base/tasks/dto/task-request-dto';

export interface Task {
  id: string;
  title: string;
  description?: string;
  taskProviderName: string;
  validatedBudget?: number;
  deadline?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface TaskResponse {
id: number;
title: string;
taskProviderName: string;
description: string;
assignedPartner?: PartnerResponseDto;
taskStatus: string;
validatedBudget?: number;
requestReceivedAt?: string;
acceptedAt?: string;
deadline?: string;
dateCreated: string;
}

export interface PartnerResponseDto {
id: number;
name: string;
// Add other partner fields as needed
}

export interface DocumentUploadResponse {
id: string;
  documentId: string;
  message: string;
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
}
