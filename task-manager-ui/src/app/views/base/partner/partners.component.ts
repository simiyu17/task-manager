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
import { PartnerService, PartnerResponseDto, PageResponse } from '../../../services/partner/partner.service';

@Component({
  selector: 'app-partners',
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
  templateUrl: './partners.component.html',
  styleUrl: './partners.component.scss',
})
export class PartnersComponent implements OnInit {
  partners: PartnerResponseDto[] = [];
  currentPage = 0;
  itemsPerPage = 10;
  totalPages = 0;
  totalElements = 0;
  Math = Math;
  isLoading = true;

  constructor(
    private partnerService: PartnerService,
    private cdr: ChangeDetectorRef,
    private datePipe: DatePipe,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPartners();
  }

  loadPartners(): void {
    this.isLoading = true;
    this.partnerService.getAllPartnersPaginated(this.currentPage, this.itemsPerPage).subscribe({
      next: (response: PageResponse<PartnerResponseDto>) => {
        this.partners = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading partners:', error);
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
    this.loadPartners();
  }

  onCreatePartner(): void {
    this.router.navigate(['/base/partners/create']);
  }

  onViewPartner(id: number): void {
    this.router.navigate(['/base/partners', id, 'view']);
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
