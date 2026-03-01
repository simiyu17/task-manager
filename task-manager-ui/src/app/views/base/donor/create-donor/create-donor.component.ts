import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import {
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  ButtonDirective,
  FormControlDirective,
  FormDirective,
  FormLabelDirective,
  FormFeedbackComponent
} from '@coreui/angular';
import { DonorService, DonorRequestDto } from '../../../../services/donor/donor.service';

@Component({
  selector: 'app-create-donor',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    FormDirective,
    FormControlDirective,
    FormLabelDirective,
    FormFeedbackComponent
  ],
  templateUrl: './create-donor.component.html',
  styleUrl: './create-donor.component.scss'
})
export class CreateDonorComponent {
  donorForm: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private donorService: DonorService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.donorForm = this.fb.group({
      donorName: ['', [Validators.required]],
      emailAddress: ['', [Validators.required, Validators.email]],
      contactNumber: ['']
    });
  }

  get f() {
    return this.donorForm.controls;
  }

  onSubmit(): void {
    if (this.donorForm.invalid) {
      Object.keys(this.donorForm.controls).forEach(key => {
        this.donorForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const donorRequest: DonorRequestDto = this.donorForm.value;

    this.donorService.createDonor(donorRequest).subscribe({
      next: (response) => {
        this.successMessage = 'Donor created successfully!';
        this.isSubmitting = false;
        this.cdr.detectChanges();
        
        // Navigate to view page after 1.5 seconds
        setTimeout(() => {
          this.router.navigate(['/base/donors', response.id, 'view']);
        }, 1500);
      },
      error: (error) => {
        console.error('Error creating donor:', error);
        this.errorMessage = error.error?.detail || 'Failed to create donor. Please try again.';
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/base/donors']);
  }

  goBack(): void {
    this.router.navigate(['/base/donors']);
  }
}
