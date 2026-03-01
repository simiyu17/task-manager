import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ColComponent,
  RowComponent,
  ButtonDirective,
  AccordionComponent,
  AccordionItemComponent,
  TemplateIdDirective,
  DropdownComponent,
  DropdownToggleDirective,
  DropdownMenuDirective,
  DropdownItemDirective,
  DropdownDividerDirective,
  ModalComponent,
  ModalHeaderComponent,
  ModalTitleDirective,
  ModalBodyComponent,
  ModalFooterComponent,
  ButtonCloseDirective
} from '@coreui/angular';
import { TaskService, TaskResponse } from '../../../../services/task/task.service';
import { DocumentService, DocumentResponseDto } from '../../../../services/document/document.service';
import { TaskCommentsComponent } from '../task-comments/task-comments.component';
import { UploadTaskDocumentComponent } from '../upload-task-document/upload-task-document.component';
import { UpdateTaskStatusComponent } from '../update-task-status/update-task-status.component';
import { AllocateTaskComponent } from '../allocate-task/allocate-task.component';

@Component({
  selector: 'app-view-task',
  standalone: true,
  imports: [
    CommonModule,
    RowComponent,
    ColComponent,
    ButtonDirective,
    AccordionComponent,
    AccordionItemComponent,
    TemplateIdDirective,
    DropdownComponent,
    DropdownToggleDirective,
    DropdownMenuDirective,
    DropdownItemDirective,
    DropdownDividerDirective,
    ModalComponent,
    ModalHeaderComponent,
    ModalTitleDirective,
    ModalBodyComponent,
    ModalFooterComponent,
    ButtonCloseDirective,
    TaskCommentsComponent,
    UploadTaskDocumentComponent,
    UpdateTaskStatusComponent,
    AllocateTaskComponent
  ],
  providers: [DatePipe],
  templateUrl: './view-task.component.html',
  styleUrl: './view-task.component.scss'
})
export class ViewTaskComponent implements OnInit {
  @ViewChild('uploadDocumentComponent') uploadDocumentComponent!: UploadTaskDocumentComponent;
  @ViewChild('updateStatusComponent') updateStatusComponent!: UpdateTaskStatusComponent;
  @ViewChild('allocateTaskComponent') allocateTaskComponent!: AllocateTaskComponent;

  taskId: number = 0;
  task: TaskResponse | null = null;
  documents: DocumentResponseDto[] = [];
  isLoadingTask = true;
  isLoadingDocuments = true;
  errorMessage = '';
  uploadDocumentModalVisible = false;
  updateStatusModalVisible = false;
  assignTaskModalVisible = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private documentService: DocumentService,
    private datePipe: DatePipe,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.taskId = parseInt(id, 10);
        this.loadTask();
        this.loadDocuments();
      }
    });
  }

  loadTask(): void {
    this.isLoadingTask = true;
    this.taskService.getTask(this.taskId.toString()).subscribe({
      next: (response) => {
        console.log('Task loaded:', response);
        this.task = response;
        this.isLoadingTask = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = 'Failed to load task details';
        this.isLoadingTask = false;
        console.error('Error loading task:', error);
        this.cdr.detectChanges();
      }
    });
  }

  loadDocuments(): void {
    this.isLoadingDocuments = true;
    this.documentService.getDocumentsByTaskId(this.taskId).subscribe({
      next: (response) => {
        console.log('Documents loaded:', response);
        this.documents = response;
        this.isLoadingDocuments = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this.isLoadingDocuments = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    return this.datePipe.transform(dateString, 'dd MMM, yyyy') || dateString;
  }

  formatDateTime(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    return this.datePipe.transform(dateString, 'dd MMM, yyyy HH:mm') || dateString;
  }

  getStatusColor(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'warning',
      'IN_PROGRESS': 'primary',
      'COMPLETED': 'success',
      'ON_HOLD': 'secondary',
      'REVIEW': 'info'
    };
    return statusMap[status] || 'secondary';
  }

  onEditBasicDetails(): void {
    this.router.navigate(['/base/tasks', this.taskId, 'edit']);
  }

  onUpdateProgress(): void {
    this.updateStatusModalVisible = true;
  }

  closeUpdateStatusModal(): void {
    this.updateStatusModalVisible = false;
  }

  handleUpdateStatusModalChange(event: boolean): void {
    this.updateStatusModalVisible = event;
  }

  onStatusUpdated(event: { success: boolean; message?: string }): void {
    if (event.success) {
      this.closeUpdateStatusModal();
      this.loadTask(); // Reload task to get updated status
      console.log('Task status updated successfully');
    }
  }

  submitUpdateStatus(): void {
    if (this.updateStatusComponent) {
      this.updateStatusComponent.onSubmit();
    }
  }

  onCancelTask(): void {
    if (confirm('Are you sure you want to cancel this task? This action cannot be undone.')) {
      // TODO: Implement cancel task API call
      console.log('Cancel task:', this.taskId);
      alert('Cancel task functionality not yet implemented');
    }
  }

  downloadDocument(documentId: number, fileName: string): void {
    this.documentService.downloadDocument(documentId).subscribe({
      next: (blob) => {
        this.documentService.triggerDownload(blob, fileName);
      },
      error: (error) => {
        console.error('Error downloading document:', error);
        alert('Failed to download document');
      }
    });
  }

  viewDocument(documentId: number): void {
    this.documentService.viewDocument(documentId).subscribe({
      next: (blob) => {
        this.documentService.openPdfInNewTab(blob);
      },
      error: (error) => {
        console.error('Error viewing document:', error);
        alert('Failed to view document. Only PDF documents can be viewed inline.');
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/base/tasks']);
  }

  getDocumentsByType(): { [key: string]: DocumentResponseDto[] } {
    const grouped: { [key: string]: DocumentResponseDto[] } = {};
    this.documents.forEach(doc => {
      const type = doc.documentType || 'Other';
      if (!grouped[type]) {
        grouped[type] = [];
      }
      grouped[type].push(doc);
    });
    return grouped;
  }

  getDocumentTypes(): string[] {
    return Object.keys(this.getDocumentsByType()).sort();
  }

  onUploadDocuments(): void {
    this.uploadDocumentModalVisible = true;
  }

  closeUploadDocumentModal(): void {
    this.uploadDocumentModalVisible = false;
  }

  handleUploadDocumentModalChange(event: boolean): void {
    this.uploadDocumentModalVisible = event;
  }

  onDocumentUploaded(event: { success: boolean; message?: string }): void {
    if (event.success) {
      this.closeUploadDocumentModal();
      this.loadDocuments(); // Reload documents to show the newly uploaded one
      // Optionally show success message
      console.log('Document uploaded successfully');
    }
  }

  submitUploadDocument(): void {
    if (this.uploadDocumentComponent) {
      this.uploadDocumentComponent.onSubmit();
    }
  }

  onAssignTask(): void {
    this.assignTaskModalVisible = true;
  }

  closeAssignTaskModal(): void {
    this.assignTaskModalVisible = false;
  }

  handleAssignTaskModalChange(event: boolean): void {
    this.assignTaskModalVisible = event;
  }

  onTaskAssigned(event: { success: boolean; message?: string }): void {
    if (event.success) {
      this.closeAssignTaskModal();
      this.loadTask(); // Reload task to show updated assignment
      console.log('Task assigned successfully');
    }
  }

  submitAssignTask(): void {
    if (this.allocateTaskComponent) {
      this.allocateTaskComponent.onSubmit();
    }
  }

  onCloneTask(): void {
    if (confirm('Are you sure you want to clone this task?')) {
      // TODO: Implement clone task API call
      console.log('Clone task:', this.taskId);
      alert('Clone task functionality not yet implemented');
    }
  }

  onDeleteTask(): void {
    if (confirm('Are you sure you want to delete this task? This action cannot be undone.')) {
      // TODO: Implement delete task API call
      console.log('Delete task:', this.taskId);
      alert('Delete task functionality not yet implemented');
    }
  }

  onExportTask(): void {
    // TODO: Implement export task functionality
    console.log('Export task:', this.taskId);
    alert('Export task functionality not yet implemented');
  }
}
