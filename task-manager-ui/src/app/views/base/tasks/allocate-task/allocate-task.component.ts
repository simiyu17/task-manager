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
  FormSelectDirective,
  FormLabelDirective
} from '@coreui/angular';
import { TaskService } from '../../../../services/task/task.service';
import { PartnerService, PartnerResponseDto } from '../../../../services/partner/partner.service';

@Component({
  selector: 'app-allocate-task',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RowComponent,
    ColComponent,
    FormDirective,
    FormControlDirective,
    FormFeedbackComponent,
    FormSelectDirective,
    FormLabelDirective
  ],
  templateUrl: './allocate-task.component.html',
  styleUrl: './allocate-task.component.scss'
})
export class AllocateTaskComponent implements OnInit {
  @Input() taskId?: string; // Task ID from route or parent component
  @Input() readonlyMode: boolean = false; // If true, form is readonly
  @Output() stepSaved = new EventEmitter<{ success: boolean; message?: string }>();

  allocateForm: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';
  partners: PartnerResponseDto[] = [];
  isLoadingPartners = false;
  taskStatus?: string;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private partnerService: PartnerService,
    private route: ActivatedRoute
  ) {
    this.allocateForm = this.fb.group({
      assignedPartnerId: [null, [Validators.required]],
      allocateNotes: ['', [Validators.maxLength(2000)]]
    });
  }

  ngOnInit(): void {
    // Get taskId from route params if not provided via @Input
    this.route.params.subscribe(params => {
      if (!this.taskId && (params['id'] || params['taskId'])) {
        this.taskId = params['id'] || params['taskId'];
      }
      if (this.taskId) {
        this.loadTaskData();
      }
    });

    this.loadPartners();
  }

  loadPartners(): void {
    this.isLoadingPartners = true;
    this.partnerService.getAllPartners().subscribe({
      next: (partners) => {
        this.partners = partners;
        this.isLoadingPartners = false;
      },
      error: (error) => {
        console.error('Error loading partners:', error);
        this.errorMessage = 'Failed to load partners. Please try again.';
        this.isLoadingPartners = false;
      }
    });
  }

  loadTaskData(): void {
    if (!this.taskId) return;

    this.taskService.getTask(this.taskId).subscribe({
      next: (task) => {
        // Store task status
        this.taskStatus = task.taskStatus;
        
        // Pre-populate form if task already has allocation data
        if (task.assignedPartner) {
          this.allocateForm.patchValue({
            assignedPartnerId: task.assignedPartner.id,
            allocateNotes: task.allocateNotes || ''
          });
        }
        
        // Disable form if task status is ALLOCATED
        if (this.taskStatus === 'ALLOCATED') {
          this.allocateForm.disable();
        }
      },
      error: (error) => {
        console.error('Error loading task data:', error);
      }
    });
  }

  get f() {
    return this.allocateForm.controls;
  }

  onSubmit(): void {
    if (this.allocateForm.invalid) {
      Object.keys(this.allocateForm.controls).forEach(key => {
        this.allocateForm.get(key)?.markAsTouched();
      });
      return;
    }

    if (!this.taskId) {
      this.errorMessage = 'Task ID is missing. Cannot save allocation.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updateData = {
      assignedPartnerId: this.allocateForm.value.assignedPartnerId,
      allocateNotes: this.allocateForm.value.allocateNotes
    };

    this.taskService.updateTask(this.taskId, updateData).subscribe({
      next: (response) => {
        this.successMessage = 'Task allocation saved successfully!';
        this.isSubmitting = false;
        this.stepSaved.emit({ success: true, message: this.successMessage });
      },
      error: (error) => {
        console.error('Error saving task allocation:', error);
        this.errorMessage = error.error?.detail || 'Failed to save task allocation. Please try again.';
        this.isSubmitting = false;
        this.stepSaved.emit({ success: false, message: this.errorMessage });
      }
    });
  }
}
