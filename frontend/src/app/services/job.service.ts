import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Job } from '../models/job.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class JobService {
  private readonly API_URL = `${environment.apiUrl}/jobs`;

  constructor(private http: HttpClient) {}

  getAllJobs(): Observable<Job[]> {
    return this.http.get<Job[]>(this.API_URL);
  }

  getJobById(id: number): Observable<Job> {
    return this.http.get<Job>(`${this.API_URL}/${id}`);
  }

  getJobByReference(reference: string): Observable<Job> {
    return this.http.get<Job>(`${this.API_URL}/reference/${reference}`);
  }

  searchJobs(searchTerm: string): Observable<Job[]> {
    const params = new HttpParams().set('q', searchTerm);
    return this.http.get<Job[]>(`${this.API_URL}/search`, { params });
  }

  createJob(job: Partial<Job>): Observable<Job> {
    return this.http.post<Job>(this.API_URL, job);
  }

  updateJob(id: number, job: Partial<Job>): Observable<Job> {
    return this.http.put<Job>(`${this.API_URL}/${id}`, job);
  }

  deleteJob(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
