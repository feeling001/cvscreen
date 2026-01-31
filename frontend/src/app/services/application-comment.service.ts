import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationComment } from '../models/application-comment.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApplicationCommentService {
  private readonly API_URL = `${environment.apiUrl}/applications`;

  constructor(private http: HttpClient) {}

  getComments(applicationId: number): Observable<ApplicationComment[]> {
    return this.http.get<ApplicationComment[]>(`${this.API_URL}/${applicationId}/comments`);
  }

  getAllCandidateComments(applicationId: number): Observable<ApplicationComment[]> {
    return this.http.get<ApplicationComment[]>(`${this.API_URL}/${applicationId}/comments/all-candidate-comments`);
  }

  addComment(applicationId: number, comment: string, rating?: number): Observable<ApplicationComment> {
    return this.http.post<ApplicationComment>(
      `${this.API_URL}/${applicationId}/comments`,
      { comment, rating }
    );
  }

  deleteComment(applicationId: number, commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${applicationId}/comments/${commentId}`);
  }
}
