import { Component, EventEmitter, Output, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ColComponent,
  RowComponent,
  FormControlDirective,
  FormDirective,
  FormFeedbackComponent
} from '@coreui/angular';
import { TaskRequestDto } from '../../dto/task-request-dto';
import { TaskService } from '../../../../../services/task/task.service';

@Component({
  selector: 'app-initiate-task',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RowComponent,
    ColComponent,
    FormDirective,
    FormControlDirective,
    FormFeedbackComponent
  ],
  templateUrl: './initiate-task.component.html',
  styleUrl: './initiate-task.component.scss'
})
export class InitiateTaskComponent implements OnInit {
  @Input() taskId?: string; // If provided, component is in edit mode
  @Input() readonlyMode: boolean = false; // If true, form is readonly
  @Output() taskCreated = new EventEmitter<{ success: boolean; taskId?: string; data?: TaskRequestDto }>();

  taskForm: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  minDate: string;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService
  ) {
    // Set minimum date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.minDate = tomorrow.toISOString().split('T')[0];

    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(1000)]],
      taskProviderName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      validatedBudget: [null, [Validators.min(0)]],
      deadline: ['', [Validators.required, this.futureDateValidator.bind(this)]]
    });
  }

  ngOnInit(): void {
    if (this.readonlyMode) {
      this.taskForm.disable();
    }
  }

  futureDateValidator(control: any) {
    if (!control.value) {
      return null;
    }
    
    const selectedDate = new Date(control.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (selectedDate <= today) {
      return { pastDate: true };
    }
    
    return null;
  }

  get f() {
    return this.taskForm.controls;
  }

  onSubmit() {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const formData: TaskRequestDto = {
      title: this.taskForm.value.title.trim(),
      description: this.taskForm.value.description?.trim() || undefined,
      taskProviderName: this.taskForm.value.taskProviderName.trim(),
      validatedBudget: this.taskForm.value.validatedBudget || undefined,
      deadline: this.taskForm.value.deadline ? new Date(this.taskForm.value.deadline).toISOString() : undefined
    };

    // Determine if we are in edit mode or create mode
    const taskOperation = this.taskId 
      ? this.taskService.updateTask(this.taskId, formData)
      : this.taskService.createTask(formData);

    taskOperation.subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.taskCreated.emit({ success: true, taskId: response.id.toString(), data: formData });
      },
      error: (error) => {
        this.isSubmitting = false;
        this.errorMessage = error.error?.message || `Failed to ${this.taskId ? 'update' : 'create'} task. Please try again.`;
        this.taskCreated.emit({ success: false });
      }
    });
  }
}
