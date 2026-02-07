import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Company } from '../models/company.model';
import { environment } from '../../environments/environment';

export interface PaginatedResponse<T> {
  companies?: T[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
}

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private readonly API_URL = `${environment.apiUrl}/companies`;

  constructor(private http: HttpClient) {}

  getAllCompanies(
    page: number = 0,
    size: number = 50,
    sortBy: string = 'applicationCount',
    sortDirection: string = 'desc'
  ): Observable<PaginatedResponse<Company>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    
    return this.http.get<PaginatedResponse<Company>>(this.API_URL, { params });
  }

  getCompanyById(id: number): Observable<Company> {
    return this.http.get<Company>(`${this.API_URL}/${id}`);
  }

  searchCompanies(
    searchTerm: string,
    page: number = 0,
    size: number = 50,
    sortBy: string = 'name',
    sortDirection: string = 'asc'
  ): Observable<PaginatedResponse<Company>> {
    const params = new HttpParams()
      .set('q', searchTerm)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    
    return this.http.get<PaginatedResponse<Company>>(`${this.API_URL}/search`, { params });
  }

  createCompany(name: string, notes?: string): Observable<Company> {
    const params = new HttpParams()
      .set('name', name)
      .set('notes', notes || '');
    return this.http.post<Company>(this.API_URL, null, { params });
  }

  updateCompany(id: number, name: string, notes?: string): Observable<Company> {
    const params = new HttpParams()
      .set('name', name)
      .set('notes', notes || '');
    return this.http.put<Company>(`${this.API_URL}/${id}`, null, { params });
  }

  deleteCompany(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
  
  mergeCompanies(targetCompanyId: number, companyIdsToMerge: number[], mergedNotes: string): Observable<Company> {
    return this.http.post<Company>(`${this.API_URL}/merge`, {
      targetCompanyId,
      companyIdsToMerge,
      mergedNotes
    });
  }
}
