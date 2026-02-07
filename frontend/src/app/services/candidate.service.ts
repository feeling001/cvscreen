import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Candidate } from '../models/candidate.model';
import { environment } from '../../environments/environment';

export interface PaginatedResponse<T> {
  candidates?: T[];
  applications?: T[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
}

export interface CandidateDuplicate {
  candidate1: Candidate;
  candidate2: Candidate;
  similarityScore: number;
}

@Injectable({
  providedIn: 'root'
})
export class CandidateService {
  private readonly API_URL = `${environment.apiUrl}/candidates`;

  constructor(private http: HttpClient) {}

  getAllCandidates(
    page: number = 0,
    size: number = 100,
    sortBy: string = 'lastName',
    sortDirection: string = 'asc'
  ): Observable<PaginatedResponse<Candidate>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    
    return this.http.get<PaginatedResponse<Candidate>>(this.API_URL, { params });
  }
  
  // Méthode pour obtenir tous les candidats sans pagination (pour les dialogues)
  getAllCandidatesSimple(): Observable<Candidate[]> {
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '10000') // Grande taille pour obtenir tous les candidats
      .set('sortBy', 'lastName')
      .set('sortDirection', 'asc');
    
    return this.http.get<PaginatedResponse<Candidate>>(this.API_URL, { params })
      .pipe(map(response => response.candidates || []));
  }

  getCandidateById(id: number): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.API_URL}/${id}`);
  }

  searchCandidates(
    searchTerm: string,
    page: number = 0,
    size: number = 100,
    sortBy: string = 'lastName',
    sortDirection: string = 'asc'
  ): Observable<PaginatedResponse<Candidate>> {
    const params = new HttpParams()
      .set('q', searchTerm)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    
    return this.http.get<PaginatedResponse<Candidate>>(`${this.API_URL}/search`, { params });
  }

  /**
   * Find potential duplicate candidates
   */
  findPotentialDuplicates(): Observable<CandidateDuplicate[]> {
    return this.http.get<CandidateDuplicate[]>(`${this.API_URL}/duplicates`);
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
  
  mergeCandidates(targetCandidateId: number, candidateIdsToMerge: number[], mergedGlobalNotes: string): Observable<Candidate> {
    return this.http.post<Candidate>(`${this.API_URL}/merge`, {
      targetCandidateId,
      candidateIdsToMerge,
      mergedGlobalNotes
    });
  }
}
