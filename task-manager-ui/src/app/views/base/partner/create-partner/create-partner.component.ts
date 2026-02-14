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
import { PartnerService, PartnerRequestDto } from '../../../../services/partner/partner.service';

@Component({
  selector: 'app-create-partner',
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
  templateUrl: './create-partner.component.html',
  styleUrl: './create-partner.component.scss'
})
export class CreatePartnerComponent {
  partnerForm: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private partnerService: PartnerService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.partnerForm = this.fb.group({
      partnerName: ['', [Validators.required]]
    });
  }

  get f() {
    return this.partnerForm.controls;
  }

  onSubmit(): void {
    if (this.partnerForm.invalid) {
      Object.keys(this.partnerForm.controls).forEach(key => {
        this.partnerForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const partnerRequest: PartnerRequestDto = this.partnerForm.value;

    this.partnerService.createPartner(partnerRequest).subscribe({
      next: (response) => {
        this.successMessage = 'Partner created successfully!';
        this.isSubmitting = false;
        this.cdr.detectChanges();
        
        // Navigate to view page after 1.5 seconds
        setTimeout(() => {
          this.router.navigate(['/base/partners', response.id, 'view']);
        }, 1500);
      },
      error: (error) => {
        console.error('Error creating partner:', error);
        this.errorMessage = error.error?.message || 'Failed to create partner. Please try again.';
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/base/partners']);
  }

  goBack(): void {
    this.router.navigate(['/base/partners']);
  }
}
