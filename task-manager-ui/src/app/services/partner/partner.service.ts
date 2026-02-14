import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../api.service';
import { HttpParams } from '@angular/common/http';

export interface PartnerRequestDto {
  partnerName: string;
}

export interface PartnerResponseDto {
  id: number;
  partnerName: string;
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
export class PartnerService {
  private readonly API_PATH = 'partners';

  constructor(private apiService: ApiService) {}

  /**
   * Create a new partner
   */
  createPartner(partnerRequest: PartnerRequestDto): Observable<PartnerResponseDto> {
    return this.apiService.post<PartnerResponseDto>(this.API_PATH, partnerRequest);
  }

  /**
   * Update an existing partner
   */
  updatePartner(id: number, partnerRequest: PartnerRequestDto): Observable<PartnerResponseDto> {
    return this.apiService.put<PartnerResponseDto>(`${this.API_PATH}/${id}`, partnerRequest);
  }

  /**
   * Get a partner by ID
   */
  getPartnerById(id: number): Observable<PartnerResponseDto> {
    return this.apiService.get<PartnerResponseDto>(`${this.API_PATH}/${id}`);
  }

  /**
   * Get all partners (non-paginated)
   */
  getAllPartners(): Observable<PartnerResponseDto[]> {
    return this.apiService.get<PartnerResponseDto[]>(this.API_PATH);
  }

  /**
   * Get paginated list of partners
   */
  getAllPartnersPaginated(page: number = 0, size: number = 10, sort: string = 'dateCreated'): Observable<PageResponse<PartnerResponseDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.apiService.get<PageResponse<PartnerResponseDto>>(`${this.API_PATH}/paginated`, { params });
  }

  /**
   * Delete a partner by ID
   */
  deletePartner(id: number): Observable<void> {
    return this.apiService.delete<void>(`${this.API_PATH}/${id}`);
  }
}
