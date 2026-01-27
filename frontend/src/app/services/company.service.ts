import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Company } from '../models/company.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private readonly API_URL = `${environment.apiUrl}/companies`;

  constructor(private http: HttpClient) {}

  getAllCompanies(): Observable<Company[]> {
    return this.http.get<Company[]>(this.API_URL);
  }

  getCompanyById(id: number): Observable<Company> {
    return this.http.get<Company>(`${this.API_URL}/${id}`);
  }

  searchCompanies(searchTerm: string): Observable<Company[]> {
    const params = new HttpParams().set('q', searchTerm);
    return this.http.get<Company[]>(`${this.API_URL}/search`, { params });
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
}
