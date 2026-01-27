import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Candidate, CandidateReview } from '../models/candidate.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CandidateService {
  private readonly API_URL = `${environment.apiUrl}/candidates`;

  constructor(private http: HttpClient) {}

  getAllCandidates(): Observable<Candidate[]> {
    return this.http.get<Candidate[]>(this.API_URL);
  }

  getCandidateById(id: number): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.API_URL}/${id}`);
  }

  searchCandidates(searchTerm: string): Observable<Candidate[]> {
    const params = new HttpParams().set('q', searchTerm);
    return this.http.get<Candidate[]>(`${this.API_URL}/search`, { params });
  }

  createCandidate(candidate: Partial<Candidate>): Observable<Candidate> {
    return this.http.post<Candidate>(this.API_URL, candidate);
  }

  updateCandidate(id: number, candidate: Partial<Candidate>): Observable<Candidate> {
    return this.http.put<Candidate>(`${this.API_URL}/${id}`, candidate);
  }

  deleteCandidate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getReviews(candidateId: number): Observable<CandidateReview[]> {
    return this.http.get<CandidateReview[]>(`${this.API_URL}/${candidateId}/reviews`);
  }

  addReview(candidateId: number, comment: string): Observable<CandidateReview> {
    return this.http.post<CandidateReview>(`${this.API_URL}/${candidateId}/reviews`, { comment });
  }

  deleteReview(candidateId: number, reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${candidateId}/reviews/${reviewId}`);
  }
}
