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
  FormSelectDirective
} from '@coreui/angular';
import { TaskRequestDto } from '../dto/task-request-dto';
import { TaskService } from '../../../../services/task/task.service';
import { DonorService, DonorResponseDto } from '../../../../services/donor/donor.service';

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
    FormFeedbackComponent,
    FormSelectDirective
  ],
  templateUrl: './initiate-task.component.html',
  styleUrl: './initiate-task.component.scss'
})
export class InitiateTaskComponent implements OnInit {
  taskId?: string; // Task ID from route or parent - if provided, component is in edit mode
  @Input() readonlyMode: boolean = false; // If true, form is readonly
  @Input() initialFormData?: TaskRequestDto; // Initial form data to populate
  @Output() taskCreated = new EventEmitter<{ success: boolean; taskId?: string; data?: TaskRequestDto }>();

  taskForm: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  minDate: string;
  isEditMode = false;
  donors: DonorResponseDto[] = [];
  isLoadingDonors = false;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private donorService: DonorService,
    private route: ActivatedRoute
  ) {
    // Set minimum date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.minDate = tomorrow.toISOString().split('T')[0];

    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(1000)]],
      donorId: [null, [Validators.required]],
      validatedBudget: [null, [Validators.min(0)]],
      deadline: ['', [Validators.required, this.futureDateValidator.bind(this)]]
    });
  }

  ngOnInit(): void {
    // Get taskId from route params if available
    this.route.params.subscribe(params => {
      if (params['id'] || params['taskId']) {
        this.taskId = params['id'] || params['taskId'];
      }
    });
    
    // Populate form with initial data if provided
    if (this.initialFormData) {
      this.taskForm.patchValue({
        title: this.initialFormData.title || '',
        description: this.initialFormData.description || '',
        donorId: this.initialFormData.donorId || null,
        validatedBudget: this.initialFormData.validatedBudget || null,
        deadline: this.initialFormData.deadline ? new Date(this.initialFormData.deadline).toISOString().split('T')[0] : ''
      });
    }
    
    this.loadDonors();
    if (this.readonlyMode) {
      this.taskForm.disable();
    }
  }

  loadDonors(): void {
    this.isLoadingDonors = true;
    this.donorService.getAllDonors().subscribe({
      next: (donors) => {
        this.donors = donors;
        this.isLoadingDonors = false;
      },
      error: (error) => {
        console.error('Error loading donors:', error);
        this.errorMessage = 'Failed to load donors. Please refresh the page.';
        this.isLoadingDonors = false;
      }
    });
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
      donorId: this.taskForm.value.donorId,
      description: this.taskForm.value.description?.trim() || undefined,
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
        this.errorMessage = error.error?.detail || `Failed to ${this.taskId ? 'update' : 'create'} task. Please try again.`;
        this.taskCreated.emit({ success: false, data: formData }); // Emit data even on failure to preserve form
      }
    });
  }
}
