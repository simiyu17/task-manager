import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ColComponent,
  RowComponent,
  FormDirective,
  FormFeedbackComponent,
  FormSelectDirective
} from '@coreui/angular';
import { TaskService, TaskStatusOption } from '../../../../services/task/task.service';

@Component({
  selector: 'app-update-task-status',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RowComponent,
    ColComponent,
    FormDirective,
    FormFeedbackComponent,
    FormSelectDirective
  ],
  templateUrl: './update-task-status.component.html',
  styleUrl: './update-task-status.component.scss'
})
export class UpdateTaskStatusComponent implements OnInit {
  @Input() taskId!: number;
  @Input() possibleNextStatuses: TaskStatusOption[] = [];
  @Output() statusUpdated = new EventEmitter<{ success: boolean; message?: string }>();

  statusForm: FormGroup;
  isSubmitting = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService
  ) {
    this.statusForm = this.fb.group({
      status: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Auto-select if only one option
    if (this.possibleNextStatuses.length === 1) {
      this.statusForm.patchValue({ status: this.possibleNextStatuses[0].statusCode });
    }
  }

  get f() {
    return this.statusForm.controls;
  }

  onSubmit(): void {
    if (this.statusForm.invalid) {
      this.statusForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const newStatus = this.statusForm.value.status;

    this.taskService.updateTaskStatus(this.taskId.toString(), newStatus).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.statusUpdated.emit({ success: true, message: 'Task status updated successfully' });
        this.statusForm.reset();
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || 'Failed to update task status. Please try again.';
        this.statusUpdated.emit({ success: false });
      }
    });
  }
}
