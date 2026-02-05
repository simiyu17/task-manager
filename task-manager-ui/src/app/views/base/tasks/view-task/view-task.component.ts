import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  ButtonDirective,
  TableDirective
} from '@coreui/angular';
import { TaskService, TaskResponse } from '../../../../services/task/task.service';
import { DocumentService, DocumentResponseDto } from '../../../../services/document/document.service';

@Component({
  selector: 'app-view-task',
  standalone: true,
  imports: [
    CommonModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    TableDirective
  ],
  providers: [DatePipe],
  templateUrl: './view-task.component.html',
  styleUrl: './view-task.component.scss'
})
export class ViewTaskComponent implements OnInit {
  taskId: number = 0;
  task: TaskResponse | null = null;
  documents: DocumentResponseDto[] = [];
  isLoadingTask = true;
  isLoadingDocuments = true;
  errorMessage = '';

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
    this.router.navigate(['/base/tasks', this.taskId, 'update-progress']);
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
}
