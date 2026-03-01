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
import { PartnerService, PartnerRequestDto, PartnerResponseDto } from '../../../../services/partner/partner.service';

@Component({
  selector: 'app-edit-partner',
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
  templateUrl: './edit-partner.component.html',
  styleUrl: './edit-partner.component.scss'
})
export class EditPartnerComponent implements OnInit {
  partnerForm: FormGroup;
  partnerId: number = 0;
  isSubmitting = false;
  isLoading = true;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private partnerService: PartnerService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.partnerForm = this.fb.group({
      partnerName: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.partnerId = parseInt(id, 10);
        this.loadPartner();
      }
    });
  }

  loadPartner(): void {
    this.isLoading = true;
    this.partnerService.getPartnerById(this.partnerId).subscribe({
      next: (partner: PartnerResponseDto) => {
        this.partnerForm.patchValue({
          partnerName: partner.partnerName
        });
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading partner:', error);
        this.errorMessage = 'Failed to load partner details';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
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

    this.partnerService.updatePartner(this.partnerId, partnerRequest).subscribe({
      next: (response) => {
        this.successMessage = 'Partner updated successfully!';
        this.isSubmitting = false;
        this.cdr.detectChanges();
        
        // Navigate to view page after 1.5 seconds
        setTimeout(() => {
          this.router.navigate(['/base/partners', this.partnerId, 'view']);
        }, 1500);
      },
      error: (error) => {
        console.error('Error updating partner:', error);
        this.errorMessage = error.error?.detail || 'Failed to update partner. Please try again.';
        this.isSubmitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/base/partners', this.partnerId, 'view']);
  }

  goBack(): void {
    this.router.navigate(['/base/partners', this.partnerId, 'view']);
  }
}
