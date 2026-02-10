import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import {
  CardBodyComponent,
  CardComponent,
  CardHeaderComponent,
  ColComponent,
  RowComponent,
  ButtonDirective,
  TableDirective,
  PageItemComponent,
  PageLinkDirective,
  PaginationComponent
} from '@coreui/angular';
import { DonorService, DonorResponseDto, PageResponse } from '../../../services/donor/donor.service';

@Component({
  selector: 'app-donor',
  standalone: true,
  imports: [
    CommonModule,
    RowComponent,
    ColComponent,
    CardComponent,
    CardHeaderComponent,
    CardBodyComponent,
    ButtonDirective,
    TableDirective,
    PaginationComponent,
    PageItemComponent,
    PageLinkDirective
  ],
  providers: [DatePipe],
  templateUrl: './donor.component.html',
  styleUrl: './donor.component.scss',
})
export class DonorComponent implements OnInit {
  donors: DonorResponseDto[] = [];
  currentPage = 0;
  itemsPerPage = 10;
  totalPages = 0;
  totalElements = 0;
  Math = Math;
  isLoading = true;

  constructor(
    private donorService: DonorService,
    private cdr: ChangeDetectorRef,
    private datePipe: DatePipe,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDonors();
  }

  loadDonors(): void {
    this.isLoading = true;
    this.donorService.getAllDonorsPaginated(this.currentPage, this.itemsPerPage).subscribe({
      next: (response: PageResponse<DonorResponseDto>) => {
        this.donors = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading donors:', error);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatDate(date: string | null): string {
    if (!date) return 'N/A';
    return this.datePipe.transform(date, 'medium') || 'N/A';
  }

  onPageChange(page: number): void {
    this.currentPage = page - 1; // Spring Boot pages are 0-indexed
    this.loadDonors();
  }

  onCreateDonor(): void {
    this.router.navigate(['/base/donors/create']);
  }

  onViewDonor(id: number): void {
    this.router.navigate(['/base/donors', id, 'view']);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  get startIndex(): number {
    return this.currentPage * this.itemsPerPage + 1;
  }

  get endIndex(): number {
    return Math.min((this.currentPage + 1) * this.itemsPerPage, this.totalElements);
  }
}
