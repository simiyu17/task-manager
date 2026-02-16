import { Component, EventEmitter, Output, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ColComponent,
  RowComponent,
  FormControlDirective,
  FormDirective,
  FormFeedbackComponent,
  FormCheckComponent,
  FormCheckLabelDirective,
  FormCheckInputDirective,
  FormLabelDirective,
  ButtonDirective
} from '@coreui/angular';
import { TaskService } from '../../../../../services/task/task.service';

@Component({
  selector: 'app-task-acceptance',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RowComponent,
    ColComponent,
    FormDirective,
    FormControlDirective,
    FormFeedbackComponent,
    FormCheckComponent,
    FormCheckLabelDirective,
    FormCheckInputDirective,
    FormLabelDirective,
    ButtonDirective
  ],
  templateUrl: './task-acceptance.component.html',
  styleUrl: './task-acceptance.component.scss'
})
export class TaskAcceptanceComponent implements OnInit {
  taskId?: string; // Task ID from route
  @Input() readonlyMode: boolean = false; // If true, form is readonly
  @Output() stepSaved = new EventEmitter<{ success: boolean; message?: string }>();

  acceptanceForm: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  taskStatus?: string;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private route: ActivatedRoute
  ) {
    this.acceptanceForm = this.fb.group({
      decision: ['', [Validators.required]],
      notes: ['', [Validators.required, Validators.maxLength(2000)]]
    });
  }

  ngOnInit(): void {
    // Get taskId from route params
    this.route.params.subscribe(params => {
      if (params['id'] || params['taskId']) {
        this.taskId = params['id'] || params['taskId'];
        this.loadTaskData();
      }
    });
  }

  loadTaskData(): void {
    if (!this.taskId) return;

    this.taskService.getTask(this.taskId).subscribe({
      next: (task) => {
        // Store task status
        this.taskStatus = task.taskStatus;
        
        // Pre-populate form if task already has acceptance/rejection data
        if (task.acceptanceNotes) {
          this.acceptanceForm.patchValue({
            decision: 'accept',
            notes: task.acceptanceNotes
          });
          
          // Make form readonly if already accepted
          if (this.readonlyMode) {
            this.acceptanceForm.disable();
          }
        } else if (task.rejectionNotes) {
          this.acceptanceForm.patchValue({
            decision: 'reject',
            notes: task.rejectionNotes
          });
          
          // Make form readonly if already rejected
          if (this.readonlyMode) {
            this.acceptanceForm.disable();
          }
        }
      },
      error: (error) => {
        console.error('Error loading task:', error);
        this.errorMessage = 'Failed to load task data. Please try again.';
      }
    });
  }

  get f() {
    return this.acceptanceForm.controls;
  }

  onSubmit(): void {
    if (this.acceptanceForm.invalid || this.isSubmitting) {
      // Mark all fields as touched to show validation errors
      Object.keys(this.acceptanceForm.controls).forEach(key => {
        this.acceptanceForm.get(key)?.markAsTouched();
      });
      return;
    }

    if (!this.taskId) {
      this.errorMessage = 'Task ID is missing. Cannot submit.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const decision = this.acceptanceForm.get('decision')?.value;
    const notes = this.acceptanceForm.get('notes')?.value;
    const isAccepted = decision === 'accept';

    // Step 1: Update task with acceptance or rejection notes
    const updateData = isAccepted 
      ? { acceptanceNotes: notes }
      : { rejectionNotes: notes };

    this.taskService.updateTask(this.taskId, updateData).subscribe({
      next: () => {
        // Step 2: Move task to next status
        this.taskService.moveTaskToNextStatus(this.taskId!, !isAccepted).subscribe({
          next: () => {
            this.successMessage = isAccepted 
              ? 'Task accepted successfully and moved to next status!'
              : 'Task rejected and moved to previous status for review.';
            this.isSubmitting = false;
            
            // Emit success event to parent
            this.stepSaved.emit({ 
              success: true, 
              message: this.successMessage 
            });

            // Disable form after successful submission
            this.acceptanceForm.disable();
          },
          error: (error) => {
            console.error('Error moving task to next status:', error);
            this.errorMessage = 'Failed to update task status. Please try again.';
            this.isSubmitting = false;
            
            this.stepSaved.emit({ 
              success: false, 
              message: this.errorMessage 
            });
          }
        });
      },
      error: (error) => {
        console.error('Error updating task:', error);
        this.errorMessage = 'Failed to save task notes. Please try again.';
        this.isSubmitting = false;
        
        this.stepSaved.emit({ 
          success: false, 
          message: this.errorMessage 
        });
      }
    });
  }
}
