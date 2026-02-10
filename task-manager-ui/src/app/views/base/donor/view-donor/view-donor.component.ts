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
import { DonorService, DonorResponseDto } from '../../../../services/donor/donor.service';

@Component({
  selector: 'app-view-donor',
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
  templateUrl: './view-donor.component.html',
  styleUrl: './view-donor.component.scss'
})
export class ViewDonorComponent implements OnInit {
  donorId: number = 0;
  donor: DonorResponseDto | null = null;
  isLoading = true;
  errorMessage = '';
  showDeleteModal = false;
  isDeleting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private donorService: DonorService,
    private datePipe: DatePipe,
    private cdr: ChangeDetectorRef
  ) {}

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
      next: (response) => {
        this.donor = response;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = 'Failed to load donor details';
        this.isLoading = false;
        console.error('Error loading donor:', error);
        this.cdr.detectChanges();
      }
    });
  }

  formatDate(date: string | null): string {
    if (!date) return 'N/A';
    return this.datePipe.transform(date, 'medium') || 'N/A';
  }

  onEdit(): void {
    this.router.navigate(['/base/donors', this.donorId, 'edit']);
  }

  onDeleteClick(): void {
    this.showDeleteModal = true;
  }

  onConfirmDelete(): void {
    this.isDeleting = true;
    this.donorService.deleteDonor(this.donorId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.showDeleteModal = false;
        this.cdr.detectChanges();
        
        // Navigate back to donors list after successful deletion
        this.router.navigate(['/base/donors']);
      },
      error: (error) => {
        console.error('Error deleting donor:', error);
        this.errorMessage = error.error?.message || 'Failed to delete donor. Please try again.';
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
    this.router.navigate(['/base/donors']);
  }
}
