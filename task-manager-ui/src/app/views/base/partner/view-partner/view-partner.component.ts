import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  ButtonDirective,
  ModalModule
} from '@coreui/angular';
import { PartnerService, PartnerResponseDto } from '../../../../services/partner/partner.service';

@Component({
  selector: 'app-view-partner',
  standalone: true,
  imports: [
    CommonModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    ModalModule
  ],
  providers: [DatePipe],
  templateUrl: './view-partner.component.html',
  styleUrl: './view-partner.component.scss'
})
export class ViewPartnerComponent implements OnInit {
  partnerId: number = 0;
  partner: PartnerResponseDto | null = null;
  isLoading = true;
  errorMessage = '';
  showDeleteModal = false;
  isDeleting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private partnerService: PartnerService,
    private datePipe: DatePipe,
    private cdr: ChangeDetectorRef
  ) {}

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
      next: (response) => {
        this.partner = response;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = 'Failed to load partner details';
        this.isLoading = false;
        console.error('Error loading partner:', error);
        this.cdr.detectChanges();
      }
    });
  }

  formatDate(date: string | null): string {
    if (!date) return 'N/A';
    return this.datePipe.transform(date, 'medium') || 'N/A';
  }

  onEdit(): void {
    this.router.navigate(['/base/partners', this.partnerId, 'edit']);
  }

  onDeleteClick(): void {
    this.showDeleteModal = true;
  }

  onConfirmDelete(): void {
    this.isDeleting = true;
    this.partnerService.deletePartner(this.partnerId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.showDeleteModal = false;
        this.cdr.detectChanges();
        
        // Navigate back to partners list after successful deletion
        this.router.navigate(['/base/partners']);
      },
      error: (error) => {
        console.error('Error deleting partner:', error);
        this.errorMessage = error.error?.detail || 'Failed to delete partner. Please try again.';
        this.isDeleting = false;
        this.showDeleteModal = false;
        this.cdr.detectChanges();
      }
    });
  }

  onCancelDelete(): void {
    this.showDeleteModal = false;
  }

  goBack(): void {
    this.router.navigate(['/base/partners']);
  }
}
