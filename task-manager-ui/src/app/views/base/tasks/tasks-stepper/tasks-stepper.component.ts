import { Component, ViewChild, Output, EventEmitter, ChangeDetectorRef, Input, AfterViewInit, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
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
import { ReviewTaskComponent } from './review-task/review-task.component';
import { AllocateTaskComponent } from './allocate-task/allocate-task.component';
import { TaskRequestDto } from '../dto/task-request-dto';
import { TaskService } from '../../../../services/task/task.service';

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
    UploadTaskDocumentComponent,
    ReviewTaskComponent,
    AllocateTaskComponent
  ],
  templateUrl: './tasks-stepper.component.html',
  styleUrl: './tasks-stepper.component.scss',
})
export class TasksStepperComponent implements AfterViewInit, OnInit {
  @ViewChild(InitiateTaskComponent) initiateTaskComponent?: InitiateTaskComponent;
  @ViewChild(UploadTaskDocumentComponent) uploadTaskDocumentComponent?: UploadTaskDocumentComponent;
  @ViewChild(ReviewTaskComponent) reviewTaskComponent?: ReviewTaskComponent;
  @ViewChild(AllocateTaskComponent) allocateTaskComponent?: AllocateTaskComponent;
  @Output() closeRequested = new EventEmitter<void>();
  @Output() taskCreated = new EventEmitter<string>(); // Emit taskId when task is created/updated
  @Input() createMode: boolean = true; // true = only 2 steps (create task), false = full 19-step workflow
  taskId?: string; // Task ID for edit mode (populated from route when not in createMode)
  @Input() readonlyMode: boolean = false; // true = first 2 steps are readonly (for update progress)
  @Input() updateProgressMode: boolean = false; // true = unlock first 3 steps by default

  currentStep = 1;
  createdTaskId: string = '';
  taskData?: TaskRequestDto;
  isSubmitting = false;
  step1FormData?: TaskRequestDto; // Store step 1 form data for preservation

  constructor(
    private cdr: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute,
    private taskService: TaskService
  ) {}

  ngOnInit(): void {
    // Get taskId from route params when not in createMode
    if (!this.createMode) {
      this.route.params.subscribe(params => {
        this.taskId = params['id'] || params['taskId'];
        if (this.taskId) {
          this.loadTaskAndCheckStatus(this.taskId);
        }
      });
    }
  }

  ngAfterViewInit(): void {
    // If in update progress mode, unlock first 3 steps
    if (this.updateProgressMode) {
      this.steps[0].locked = false;
      this.steps[1].locked = false;
      this.steps[2].locked = false;
    }
    
    // Check task status if taskId is already available (e.g., from createdTaskId)
    const taskIdToCheck = this.createdTaskId || this.taskId;
    if (taskIdToCheck && !this.createMode) {
      this.loadTaskAndCheckStatus(taskIdToCheck);
    }
    
    // Trigger change detection after view initialization to avoid ExpressionChangedAfterItHasBeenCheckedError
    this.cdr.detectChanges();
  }
  
  steps: Step[] = [
    { id: 1, title: 'Basic Information', completed: false, locked: false },
    { id: 2, title: 'Upload Documents', completed: false, locked: true },
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
      this.step1FormData = event.data; // Preserve form data
      this.steps[0].completed = true;
      this.steps[1].locked = false; // Unlock step 2
      this.currentStep = 2;
      this.isSubmitting = false;
      
      // Load task and check status to potentially unlock step 4
      if (!this.createMode) {
        this.loadTaskAndCheckStatus(event.taskId);
      }
      
      this.cdr.detectChanges();
    } else {
      // Even on failure, preserve the form data
      if (event.data) {
        this.step1FormData = event.data;
      }
      this.isSubmitting = false;
      this.cdr.detectChanges();
    }
  }

  onDocumentUploaded(event: { success: boolean; message?: string }): void {
    if (event.success) {
      // Document uploaded successfully - just show success, don't navigate
      this.isSubmitting = false;
      this.cdr.detectChanges();
      // You could show a success message here if needed
    } else {
      this.isSubmitting = false;
      this.cdr.detectChanges();
    }
  }

  onStepSaved(event: { success: boolean; message?: string }): void {
    if (event.success) {
      // Step saved successfully - mark as complete
      this.steps[this.currentStep - 1].completed = true;
      this.isSubmitting = false;
      this.cdr.detectChanges();
    } else {
      this.isSubmitting = false;
      this.cdr.detectChanges();
    }
  }

  uploadDocument(): void {
    // Trigger document upload from step 2
    if (this.currentStep === 2 && this.uploadTaskDocumentComponent) {
      this.isSubmitting = true;
      this.uploadTaskDocumentComponent.onSubmit();
    }
  }

  nextStep(): void {
    // Handle step-specific next button logic
    if (this.currentStep === 1) {
      // For step 1, trigger form submission in initiate-task component
      if (!this.updateProgressMode) {
        this.isSubmitting = true;
        if (this.initiateTaskComponent) {
          this.initiateTaskComponent.onSubmit();
        }
      } else {
        // In update progress mode, just move to next step
        this.steps[this.currentStep - 1].completed = true;
        this.currentStep = 2;
      }
    } else if (this.currentStep === 2) {
      // For step 2, check if we should finish or continue
      if (this.createMode) {
        // In create mode, finish and navigate back to tasks list
        this.router.navigate(['/base/tasks']);
      } else if (this.currentStep < this.steps.length) {
        // In full workflow mode, navigate to next step
        this.steps[this.currentStep - 1].completed = true;
        this.steps[this.currentStep].locked = false; // Unlock next step
        this.currentStep++;
      }
    } else if (this.currentStep === 3) {
      // For step 3 (review), just mark complete and move forward
      this.steps[this.currentStep - 1].completed = true;
      if (this.currentStep < this.steps.length) {
        this.steps[this.currentStep].locked = false; // Unlock next step
      }
      this.currentStep++;
    } else if (this.currentStep === 4) {
      // For step 4 (allocate), trigger form submission
      this.isSubmitting = true;
      if (this.allocateTaskComponent) {
        this.allocateTaskComponent.onSubmit();
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
    if (this.currentStep > 1 && !this.isPreviousButtonDisabled()) {
      // Save current form data before moving
      this.saveCurrentStepData();
      this.currentStep--;
    }
  }

  goToStep(stepId: number): void {
    // Allow navigation to any unlocked step
    const targetStep = this.steps[stepId - 1];
    if (!targetStep.locked) {
      // Save current form data before moving
      this.saveCurrentStepData();
      this.currentStep = stepId;
    }
  }

  isStepAccessible(stepId: number): boolean {
    // Can access any unlocked step
    const step = this.steps[stepId - 1];
    return !step.locked;
  }

  getStepStatus(step: Step): string {
    if (step.completed) return 'completed';
    if (step.id === this.currentStep) return 'active';
    return 'pending';
  }

  isPreviousButtonDisabled(): boolean {
    if (this.currentStep === 1) {
      return true;
    }
    // Check if previous step is locked
    const previousStep = this.steps[this.currentStep - 2];
    return previousStep.locked;
  }

  isNextButtonDisabled(): boolean {
    // Use parent's isSubmitting instead of checking child components
    if (this.isSubmitting) {
      return true;
    }
    
    // In createMode, only check form validity for step 1
    if (this.createMode) {
      if (this.currentStep === 1 && this.initiateTaskComponent) {
        return this.initiateTaskComponent.taskForm.invalid;
      }
      // For step 2 in create mode, just check if we can navigate
      if (this.currentStep === 2) {
        return false; // Next button is always enabled to navigate or exit
      }
      return false;
    }
    
    // For full workflow mode, apply all checks
    // Check if we're at the last step
    if (this.currentStep === this.steps.length) {
      return true;
    }
    
    // For step 2, next button is just for navigation
    if (this.currentStep === 2) {
      return false;
    }
    
    // Check if next step is locked
    const nextStep = this.steps[this.currentStep];
    if (nextStep.locked) {
      return true;
    }
    
    // For step 1, check form validity
    if (this.currentStep === 1 && this.initiateTaskComponent) {
      return this.initiateTaskComponent.taskForm.invalid;
    }
    
    // For step 4, check form validity
    if (this.currentStep === 4 && this.allocateTaskComponent) {
      return this.allocateTaskComponent.allocateForm.invalid;
    }
    
    return false;
  }

  getNextButtonLabel(): string {
    if (this.updateProgressMode) {
      // In update progress mode, always show "Next"
      if (this.currentStep === this.steps.length) {
        return 'Complete Task';
      }
      return 'Next';
    }
    
    if (this.currentStep === 1) {
      return 'Submit & Next';
    }
    if (this.currentStep === 2) {
      return this.createMode ? 'Finish' : 'Next';
    }
    if (this.currentStep === 4) {
      return 'Save & Continue';
    }
    if (this.currentStep === this.steps.length) {
      return 'Complete Task';
    }
    return 'Next';
  }

  saveStepData(): void {
    // Save data for step 3 (review)
    if (this.currentStep === 3) {
      console.log('Saving review step data for taskId:', this.createdTaskId || this.taskId);
      alert('Review data saved successfully!');
    }
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

  showSaveStepButton(): boolean {
    // Show "Save Step" button only for step 3
    return this.currentStep === 3;
  }

  showUploadDocumentButton(): boolean {
    // Show "Upload Document" button only for step 2
    return this.currentStep === 2;
  }

  showMoveToNextStatusButton(): boolean {
    // Show "Move Task To Next Step" button from step 3 onwards (except step 4) and only when not in createMode
    return this.currentStep >= 3 && this.currentStep !== 4 && !this.createMode && !!(this.createdTaskId || this.taskId);
  }

  showMoveToNextStatusButtonStep4(): boolean {
    // Show "Move Task To Next Step" button at step 4 as an additional button
    return this.currentStep === 4 && !this.createMode && !!(this.createdTaskId || this.taskId);
  }

  moveToNextStatus(): void {
    const taskIdToUse = this.createdTaskId || this.taskId;
    if (!taskIdToUse) {
      console.error('No task ID available');
      return;
    }

    // Show confirmation dialog
    if (!confirm('Are you sure you want to move this task to the next step? This action cannot be undone.')) {
      return;
    }

    this.isSubmitting = true;
    this.taskService.moveTaskToNextStatus(taskIdToUse).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        console.log('Task moved to next status:', response);
        alert('Task successfully moved to next status!');
        
        // Refetch task to get latest status and unlock appropriate steps
        this.loadTaskAndCheckStatus(taskIdToUse);
        
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.isSubmitting = false;
        const errorMessage = error.error?.message || 'Failed to move task to next status. Please try again.';
        console.error('Error moving task to next status:', error);
        alert(errorMessage);
        this.cdr.detectChanges();
      }
    });
  }

  onReviewSaved(): void {
    console.log('Review saved for task:', this.createdTaskId || this.taskId || 'unknown');
    // Optionally refresh reviews or show success message
  }

  saveCurrentStepData(): void {
    // Save form data from current step before navigating away
    if (this.currentStep === 1 && this.initiateTaskComponent) {
      const formValue = this.initiateTaskComponent.taskForm.value;
      this.step1FormData = {
        title: formValue.title?.trim() || undefined,
        description: formValue.description?.trim() || undefined,
        donorId: formValue.donorId || undefined,
        validatedBudget: formValue.validatedBudget || undefined,
        deadline: formValue.deadline ? new Date(formValue.deadline).toISOString() : undefined
      };
    }
  }

  loadTaskAndCheckStatus(taskId: string): void {
    // Load task details and check if status is REVIEW_COMPLETED to unlock step 4
    this.taskService.getTask(taskId).subscribe({
      next: (task) => {
        if (task.taskStatus === 'REVIEW_COMPLETED') {
          // Unlock step 4 (id: 4, index: 3)
          this.steps[3].locked = false;
          this.cdr.detectChanges();
        }
      },
      error: (error) => {
        console.error('Error loading task:', error);
      }
    });
  }
}
