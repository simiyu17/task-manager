import { Component, ViewChild, Output, EventEmitter, ChangeDetectorRef, Input, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  ButtonDirective,
  SpinnerComponent
} from '@coreui/angular';
import { InitiateTaskComponent } from './initiate-task/initiate-task.component';
import { UploadTaskDocumentComponent } from './upload-task-document/upload-task-document.component';
import { TaskRequestDto } from '../dto/task-request-dto';

interface Step {
  id: number;
  title: string;
  completed: boolean;
  locked: boolean;
}

@Component({
  selector: 'app-tasks-stepper',
  imports: [
    CommonModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    SpinnerComponent,
    InitiateTaskComponent,
    UploadTaskDocumentComponent
  ],
  templateUrl: './tasks-stepper.component.html',
  styleUrl: './tasks-stepper.component.scss',
})
export class TasksStepperComponent implements AfterViewInit {
  @ViewChild(InitiateTaskComponent) initiateTaskComponent?: InitiateTaskComponent;
  @ViewChild(UploadTaskDocumentComponent) uploadTaskDocumentComponent?: UploadTaskDocumentComponent;
  @Output() closeRequested = new EventEmitter<void>();
  @Output() taskCreated = new EventEmitter<string>(); // Emit taskId when task is created/updated
  @Input() createMode: boolean = true; // true = only 2 steps (create task), false = full 19-step workflow
  @Input() taskId?: string; // Task ID for edit mode
  @Input() readonlyMode: boolean = false; // true = first 2 steps are readonly (for update progress)

  currentStep = 1;
  createdTaskId: string = '';
  taskData?: TaskRequestDto;
  isSubmitting = false;

  constructor(
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngAfterViewInit(): void {
    // Trigger change detection after view initialization to avoid ExpressionChangedAfterItHasBeenCheckedError
    this.cdr.detectChanges();
  }
  
  steps: Step[] = [
    { id: 1, title: 'Initiate Task', completed: false, locked: false },
    { id: 2, title: 'Upload Document', completed: false, locked: true },
    { id: 3, title: 'Internal Review Meeting', completed: false, locked: true },
    { id: 4, title: 'Task Allocation', completed: false, locked: true },
    { id: 5, title: 'Task Acceptance', completed: false, locked: true },
    { id: 6, title: 'Preliminary Scoping', completed: false, locked: true },
    { id: 7, title: 'WBS Submission', completed: false, locked: true },
    { id: 8, title: 'Concept Note Development', completed: false, locked: true },
    { id: 9, title: 'Internal Review', completed: false, locked: true },
    { id: 10, title: 'Finalization of Concept Note', completed: false, locked: true },
    { id: 11, title: 'Inception Report Development', completed: false, locked: true },
    { id: 12, title: 'Quality Review and Enhancement Meeting', completed: false, locked: true },
    { id: 13, title: 'Inception Report Finalization and Submission', completed: false, locked: true },
    { id: 14, title: 'Inception Meeting', completed: false, locked: true },
    { id: 15, title: 'Task Execution', completed: false, locked: true },
    { id: 16, title: 'Implementation and Data Collection', completed: false, locked: true },
    { id: 17, title: 'Draft Reporting and Validation', completed: false, locked: true },
    { id: 18, title: 'Final Reporting and Completion', completed: false, locked: true },
    { id: 19, title: 'Exit and Sustainability Planning', completed: false, locked: true }
  ];

  onTaskCreated(event: { success: boolean; taskId?: string; data?: TaskRequestDto }): void {
    if (event.success && event.taskId) {
      this.createdTaskId = event.taskId;
      this.taskData = event.data;
      this.steps[0].completed = true;
      this.steps[1].locked = false; // Unlock step 2
      this.currentStep = 2;
      this.isSubmitting = false;
      this.cdr.detectChanges();
    } else {
      this.isSubmitting = false;
      this.cdr.detectChanges();
    }
  }

  onDocumentUploaded(event: { success: boolean; message?: string }): void {
    if (event.success) {
      this.steps[1].completed = true;
      this.isSubmitting = false;
      
      if (this.createMode) {
        // In create mode, navigate back to tasks list after document upload
        this.cdr.detectChanges();
        this.router.navigate(['/base/tasks']);
      } else {
        // In full workflow mode, continue to next step
        this.steps[2].locked = false; // Unlock step 3
        this.currentStep = 3;
        this.cdr.detectChanges();
      }
    } else {
      this.isSubmitting = false;
      this.cdr.detectChanges();
    }
  }

  nextStep(): void {
    // Handle step-specific next button logic
    if (this.currentStep === 1) {
      // For step 1, trigger form submission in initiate-task component
      this.isSubmitting = true;
      if (this.initiateTaskComponent) {
        this.initiateTaskComponent.onSubmit();
      }
    } else if (this.currentStep === 2) {
      // For step 2, trigger form submission in upload-task-document component
      this.isSubmitting = true;
      if (this.uploadTaskDocumentComponent) {
        this.uploadTaskDocumentComponent.onSubmit();
      }
    } else if (this.currentStep < this.steps.length) {
      // For other steps, just mark complete and move forward
      this.steps[this.currentStep - 1].completed = true;
      if (this.currentStep < this.steps.length) {
        this.steps[this.currentStep].locked = false; // Unlock next step
      }
      this.currentStep++;
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  goToStep(stepId: number): void {
    // Only allow going to unlocked steps
    const targetStep = this.steps[stepId - 1];
    if (!targetStep.locked && (stepId < this.currentStep || targetStep.completed)) {
      this.currentStep = stepId;
    }
  }

  isStepAccessible(stepId: number): boolean {
    // Can access current step or any unlocked completed step
    const step = this.steps[stepId - 1];
    return !step.locked && (stepId <= this.currentStep || step.completed);
  }

  getStepStatus(step: Step): string {
    if (step.completed) return 'completed';
    if (step.id === this.currentStep) return 'active';
    return 'pending';
  }

  isNextButtonDisabled(): boolean {
    // Use parent's isSubmitting instead of checking child components
    if (this.isSubmitting) {
      return true;
    }
    
    if (this.currentStep === 1 && this.initiateTaskComponent) {
      return this.initiateTaskComponent.taskForm.invalid;
    }
    if (this.currentStep === 2 && this.uploadTaskDocumentComponent) {
      return this.uploadTaskDocumentComponent.uploadForm.invalid;
    }
    // For other steps, allow proceeding
    return this.currentStep === this.steps.length;
  }

  getNextButtonLabel(): string {
    if (this.currentStep === 1 || this.currentStep === 2) {
      return 'Submit & Next';
    }
    if (this.currentStep === this.steps.length) {
      return 'Complete Task';
    }
    return 'Next';
  }

  saveAsDraft(): void {
    // TODO: Implement save as draft logic
    console.log('Saving draft with taskId:', this.createdTaskId);
    console.log('Draft data:', this.taskData);
    
    // Simulate save and close
    // In production, this would call an API to save draft
    alert('Draft saved successfully!');
    this.router.navigate(['/base/tasks']);
  }

  cancel(): void {
    if (confirm('Are you sure you want to cancel? Any unsaved changes will be lost.')) {
      this.router.navigate(['/base/tasks']);
    }
  }

  showSaveAsDraftButton(): boolean {
    // Show save as draft button only after step 1 is completed
    return this.currentStep > 1 && this.createdTaskId !== '';
  }
}
