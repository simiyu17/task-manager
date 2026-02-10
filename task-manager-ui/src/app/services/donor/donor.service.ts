import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api.service';
import { HttpParams } from '@angular/common/http';

export interface DonorRequestDto {
  donorName: string;
  emailAddress: string;
  contactNumber: string;
}

export interface DonorResponseDto {
  id: number;
  donorName: string;
  emailAddress: string;
  contactNumber: string;
  dateCreated: string;
  lastModified: string;
  createdBy: string;
  lastModifiedBy: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class DonorService {
  private readonly API_PATH = 'donors';

  constructor(private apiService: ApiService) {}

  /**
   * Create a new donor
   */
  createDonor(donorRequest: DonorRequestDto): Observable<DonorResponseDto> {
    return this.apiService.post<DonorResponseDto>(this.API_PATH, donorRequest);
  }

  /**
   * Update an existing donor
   */
  updateDonor(id: number, donorRequest: DonorRequestDto): Observable<DonorResponseDto> {
    return this.apiService.put<DonorResponseDto>(`${this.API_PATH}/${id}`, donorRequest);
  }

  /**
   * Get a donor by ID
   */
  getDonorById(id: number): Observable<DonorResponseDto> {
    return this.apiService.get<DonorResponseDto>(`${this.API_PATH}/${id}`);
  }

  /**
   * Get all donors (non-paginated)
   */
  getAllDonors(): Observable<DonorResponseDto[]> {
    return this.apiService.get<DonorResponseDto[]>(this.API_PATH);
  }

  /**
   * Get paginated list of donors
   */
  getAllDonorsPaginated(page: number = 0, size: number = 10, sort: string = 'dateCreated'): Observable<PageResponse<DonorResponseDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.apiService.get<PageResponse<DonorResponseDto>>(`${this.API_PATH}/paginated`, { params });
  }

  /**
   * Delete a donor by ID
   */
  deleteDonor(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.API_PATH}/${id}`);
  }
}
