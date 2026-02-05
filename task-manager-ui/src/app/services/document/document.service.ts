import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api.service';

export interface DocumentResponseDto {
  id: number;
  fileName: string;
  documentType: string;
  taskId: number;
  uploadedAt: string;
  isFinal: boolean;
  fileSize?: number;
  contentType?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly DOCUMENTS_ENDPOINT = 'documents';

  constructor(private apiService: ApiService) {}

  /**
   * Upload a document for a task
   * @param file - File to upload
   * @param taskId - Task ID
   * @param documentType - Type of document
   */
  uploadDocument(file: File, taskId: number, documentType: string): Observable<DocumentResponseDto> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('taskId', taskId.toString());
    formData.append('documentType', documentType);

    return this.apiService.post<DocumentResponseDto>(`${this.DOCUMENTS_ENDPOINT}/upload`, formData);
  }

  /**
   * Get all documents for a specific task
   * @param taskId - Task ID
   */
  getDocumentsByTaskId(taskId: number): Observable<DocumentResponseDto[]> {
    return this.apiService.get<DocumentResponseDto[]>(`${this.DOCUMENTS_ENDPOINT}/task/${taskId}`);
  }

  /**
   * Get document by ID
   * @param documentId - Document ID
   */
  getDocumentById(documentId: number): Observable<DocumentResponseDto> {
    return this.apiService.get<DocumentResponseDto>(`${this.DOCUMENTS_ENDPOINT}/${documentId}`);
  }

  /**
   * Download a document
   * @param documentId - Document ID
   * @returns Observable with blob data
   */
  downloadDocument(documentId: number): Observable<Blob> {
    return this.apiService.get<Blob>(
      `${this.DOCUMENTS_ENDPOINT}/${documentId}/download`,
      { responseType: 'blob' }
    );
  }

  /**
   * View a PDF document inline
   * @param documentId - Document ID
   * @returns Observable with blob data
   */
  viewDocument(documentId: number): Observable<Blob> {
    return this.apiService.get<Blob>(
      `${this.DOCUMENTS_ENDPOINT}/${documentId}/view`,
      { responseType: 'blob' }
    );
  }

  /**
   * Mark a document as final
   * @param documentId - Document ID
   */
  markDocumentAsFinal(documentId: number): Observable<void> {
    return this.apiService.patch<void>(`${this.DOCUMENTS_ENDPOINT}/${documentId}/mark-final`, {});
  }

  /**
   * Helper method to trigger file download in browser
   * @param blob - Blob data
   * @param filename - Suggested filename
   */
  triggerDownload(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  /**
   * Helper method to open PDF in new tab
   * @param blob - Blob data
   */
  openPdfInNewTab(blob: Blob): void {
    const url = window.URL.createObjectURL(blob);
    window.open(url, '_blank');
  }
}
