import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application } from '../models/application.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private readonly API_URL = `${environment.apiUrl}/applications`;

  constructor(private http: HttpClient) {}

  getAllApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(this.API_URL);
  }

  getApplicationById(id: number): Observable<Application> {
    return this.http.get<Application>(`${this.API_URL}/${id}`);
  }

  searchApplications(filters: {
    candidateName?: string;
    jobReference?: string;
    companyName?: string;
    roleCategory?: string;
    status?: string;
  }): Observable<Application[]> {
    let params = new HttpParams();
    if (filters.candidateName) params = params.set('candidateName', filters.candidateName);
    if (filters.jobReference) params = params.set('jobReference', filters.jobReference);
    if (filters.companyName) params = params.set('companyName', filters.companyName);
    if (filters.roleCategory) params = params.set('roleCategory', filters.roleCategory);
    if (filters.status) params = params.set('status', filters.status);

    return this.http.get<Application[]>(`${this.API_URL}/search`, { params });
  }

  createApplication(application: Partial<Application>): Observable<Application> {
    return this.http.post<Application>(this.API_URL, application);
  }

  updateApplication(id: number, application: Partial<Application>): Observable<Application> {
    return this.http.put<Application>(`${this.API_URL}/${id}`, application);
  }

  deleteApplication(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  uploadCV(applicationId: number, file: File): Observable<Application> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Application>(`${this.API_URL}/${applicationId}/cv`, formData);
  }
}
