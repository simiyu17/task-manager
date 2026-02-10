import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
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
import { DonorService, DonorRequestDto, DonorResponseDto } from '../../../../services/donor/donor.service';

@Component({
  selector: 'app-edit-donor',
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
  templateUrl: './edit-donor.component.html',
  styleUrl: './edit-donor.component.scss'
})
export class EditDonorComponent implements OnInit {
  donorForm: FormGroup;
  donorId: number = 0;
  isSubmitting = false;
  isLoading = true;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
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

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.donorId = parseInt(id, 10);
        this.loadDonor();
      }
    });
  }

  loadDonor(): void {
    this.isLoading = true;
    this.donorService.getDonorById(this.donorId).subscribe({
      next: (donor: DonorResponseDto) => {
        this.donorForm.patchValue({
          donorName: donor.donorName,
          emailAddress: donor.emailAddress,
          contactNumber: donor.contactNumber
        });
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading donor:', error);
        this.errorMessage = 'Failed to load donor details';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
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

    this.donorService.updateDonor(this.donorId, donorRequest).subscribe({
      next: (response) => {
        this.successMessage = 'Donor updated successfully!';
        this.isSubmitting = false;
        this.cdr.detectChanges();
        
        // Navigate to view page after 1.5 seconds
        setTimeout(() => {
          this.router.navigate(['/base/donors', this.donorId, 'view']);
        }, 1500);
      },
      error: (error) => {
        console.error('Error updating donor:', error);
        this.errorMessage = error.error?.message || 'Failed to update donor. Please try again.';
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/base/donors', this.donorId, 'view']);
  }

  goBack(): void {
    this.router.navigate(['/base/donors', this.donorId, 'view']);
  }
}
