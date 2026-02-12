import { Component, OnInit, ViewChild, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TasksStepperComponent } from '../tasks-stepper/tasks-stepper.component';
import { TaskService } from '../../../../services/task/task.service';
import { DocumentService } from '../../../../services/document/document.service';
import { CardComponent, CardHeaderComponent, CardBodyComponent, TableDirective, BadgeComponent, ButtonDirective } from '@coreui/angular';
import { IconDirective } from '@coreui/icons-angular';

@Component({
  selector: 'app-update-task-progress',
  standalone: true,
  imports: [
    CommonModule, 
    TasksStepperComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    TableDirective,
    BadgeComponent,
    ButtonDirective,
    IconDirective
  ],
  templateUrl: './update-task-progress.component.html',
  styleUrl: './update-task-progress.component.scss'
})
export class UpdateTaskProgressComponent implements OnInit, AfterViewInit {
  @ViewChild(TasksStepperComponent) stepper!: TasksStepperComponent;
  
  taskId: string = '';
  task: any = null;
  documents: any[] = [];
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private documentService: DocumentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.taskId = this.route.snapshot.paramMap.get('id') || '';
    if (this.taskId) {
      this.loadTask();
      this.loadDocuments();
    }
  }

  ngAfterViewInit(): void {
    // Trigger change detection to avoid ExpressionChangedAfterItHasBeenCheckedError
    this.cdr.detectChanges();
  }

  loadTask(): void {
    this.loading = true;
    this.taskService.getTask(this.taskId).subscribe({
      next: (response) => {
        console.log('Task loaded for progress update:', response);
        this.task = response;
        this.loading = false;
        this.cdr.detectChanges();
        this.populateForm();
      },
      error: (error) => {
        console.error('Error loading task:', error);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadDocuments(): void {
    this.documentService.getDocumentsByTaskId(parseInt(this.taskId)).subscribe({
      next: (response) => {
        console.log('Documents loaded for task:', response);
        // Filter only TASK_DOCUMENT type
        this.documents = response.filter((doc: any) => doc.documentType === 'TASK_DOCUMENT');
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this.cdr.detectChanges();
      }
    });
  }

  populateForm(): void {
    // Wait for the stepper component to be initialized
    setTimeout(() => {
      if (this.stepper && this.stepper.initiateTaskComponent && this.task) {
        const form = this.stepper.initiateTaskComponent.taskForm;
        form.patchValue({
          title: this.task.title,
          description: this.task.description,
          donorId: this.task.donor?.id,
          validatedBudget: this.task.validatedBudget,
          deadline: this.task.deadline ? this.task.deadline.split('T')[0] : null
        });
      }
    }, 100);
  }

  onProgressUpdated(taskId: string): void {
    // Navigate back to view task after successful update
    this.router.navigate(['/base/tasks', taskId, 'view']);
  }

  onCancel(): void {
    this.router.navigate(['/base/tasks', this.taskId, 'view']);
  }

  formatDate(dateString: string): string {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB', { 
      day: '2-digit', 
      month: 'short', 
      year: 'numeric' 
    });
  }

  viewDocument(doc: any): void {
    this.documentService.viewDocument(doc.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
      },
      error: (error) => {
        console.error('Error viewing document:', error);
        alert('Error viewing document. Please try again.');
      }
    });
  }

  downloadDocument(doc: any): void {
    this.documentService.downloadDocument(doc.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = doc.fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading document:', error);
        alert('Error downloading document. Please try again.');
      }
    });
  }
}
