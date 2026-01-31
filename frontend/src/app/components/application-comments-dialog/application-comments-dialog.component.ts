import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApplicationCommentService } from '../../services/application-comment.service';
import { ApplicationComment } from '../../models/application-comment.model';

@Component({
  selector: 'app-application-comments-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule,
    MatTooltipModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>comment</mat-icon>
      Comments for Application
    </h2>
    
    <mat-dialog-content>
      <!-- Add Comment Section -->
      <div class="add-comment-section">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Add a comment</mat-label>
          <textarea 
            matInput 
            [(ngModel)]="newComment" 
            rows="3"
            placeholder="Enter your comment here..."
            [disabled]="loading">
          </textarea>
        </mat-form-field>
        
        <div class="rating-section">
          <label>Rating (optional):</label>
          <div class="stars">
            <mat-icon 
              *ngFor="let star of [1, 2, 3, 4, 5]" 
              (click)="setRating(star)"
              [class.filled]="star <= newRating"
              [class.clickable]="!loading"
              matTooltip="{{ star }} star{{ star > 1 ? 's' : '' }}">
              {{ star <= newRating ? 'star' : 'star_border' }}
            </mat-icon>
            <button 
              mat-icon-button 
              *ngIf="newRating" 
              (click)="clearRating()"
              matTooltip="Clear rating"
              [disabled]="loading">
              <mat-icon>clear</mat-icon>
            </button>
          </div>
        </div>
        
        <button 
          mat-raised-button 
          color="primary" 
          (click)="addComment()"
          [disabled]="!newComment.trim() || loading">
          <mat-icon>add</mat-icon>
          Add Comment
        </button>
      </div>
      
      <mat-divider class="divider"></mat-divider>
      
      <!-- Comments List -->
      <div class="comments-section">
        <h3>All Comments for Candidate ({{ comments.length }})</h3>
        
        <div *ngIf="loading" class="loading-container">
          <mat-spinner diameter="40"></mat-spinner>
        </div>
        
        <div *ngIf="!loading && comments.length === 0" class="no-comments">
          <mat-icon>chat_bubble_outline</mat-icon>
          <p>No comments yet. Be the first to add one!</p>
        </div>
        
        <mat-list *ngIf="!loading && comments.length > 0">
          <mat-list-item *ngFor="let comment of comments" class="comment-item">
            <div class="comment-content">
              <div class="comment-header">
                <div class="comment-author">
                  <mat-icon>person</mat-icon>
                  <strong>{{ comment.displayName || comment.username }}</strong>
                  
                  <!-- Application context indicator -->
                  <mat-chip 
                    *ngIf="comment.currentApplication" 
                    class="current-app-chip"
                    matTooltip="Comment on this application">
                    Current Application
                  </mat-chip>
                  <mat-chip 
                    *ngIf="!comment.currentApplication && comment.jobReference" 
                    class="other-app-chip"
                    matTooltip="Comment on another application">
                    {{ comment.jobReference }} - {{ comment.roleCategory }}
                  </mat-chip>
                  <mat-chip 
                    *ngIf="!comment.currentApplication && !comment.jobReference" 
                    class="other-app-chip"
                    matTooltip="Comment on spontaneous application">
                    Spontaneous - {{ comment.roleCategory }}
                  </mat-chip>
                </div>
                <div class="comment-actions">
                  <span class="comment-date">{{ comment.createdAt | date:'short' }}</span>
                  <button 
                    *ngIf="comment.currentApplication"
                    mat-icon-button 
                    color="warn" 
                    (click)="deleteComment(comment.id!)"
                    matTooltip="Delete comment">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
              </div>
              
              <!-- Rating display -->
              <div class="comment-rating" *ngIf="comment.rating">
                <mat-icon 
                  *ngFor="let star of [1, 2, 3, 4, 5]"
                  [class.filled]="star <= comment.rating!"
                  class="small-star">
                  {{ star <= comment.rating! ? 'star' : 'star_border' }}
                </mat-icon>
                <span class="rating-text">({{ comment.rating }}/5)</span>
              </div>
              
              <p class="comment-text">{{ comment.comment }}</p>
            </div>
            <mat-divider></mat-divider>
          </mat-list-item>
        </mat-list>
      </div>
    </mat-dialog-content>
    
    <mat-dialog-actions align="end">
      <button mat-button (click)="onClose()">Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content {
      min-width: 700px;
      max-height: 75vh;
    }
    
    .add-comment-section {
      margin-bottom: 20px;
    }
    
    .full-width {
      width: 100%;
    }
    
    .rating-section {
      margin: 16px 0;
    }
    
    .rating-section label {
      display: block;
      margin-bottom: 8px;
      color: #666;
      font-size: 14px;
    }
    
    .stars {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    
    .stars mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #ccc;
      transition: color 0.2s;
    }
    
    .stars mat-icon.clickable {
      cursor: pointer;
    }
    
    .stars mat-icon.clickable:hover {
      color: #ffa726;
    }
    
    .stars mat-icon.filled {
      color: #ff9800;
    }
    
    .divider {
      margin: 20px 0;
    }
    
    .comments-section h3 {
      margin-bottom: 16px;
      color: #424242;
    }
    
    .loading-container {
      display: flex;
      justify-content: center;
      padding: 40px 0;
    }
    
    .no-comments {
      text-align: center;
      padding: 40px 20px;
      color: #9e9e9e;
    }
    
    .no-comments mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
    }
    
    .comment-item {
      height: auto !important;
      padding: 16px 0 !important;
      margin-bottom: 8px;
    }
    
    .comment-content {
      width: 100%;
    }
    
    .comment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    
    .comment-author {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    
    .comment-author mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
    
    .current-app-chip {
      background-color: #4caf50 !important;
      color: white !important;
      font-size: 11px;
      height: 24px;
    }
    
    .other-app-chip {
      background-color: #2196f3 !important;
      color: white !important;
      font-size: 11px;
      height: 24px;
    }
    
    .comment-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .comment-date {
      font-size: 12px;
      color: #757575;
    }
    
    .comment-rating {
      display: flex;
      align-items: center;
      gap: 4px;
      margin: 8px 0;
      padding-left: 28px;
    }
    
    .comment-rating .small-star {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: #ccc;
    }
    
    .comment-rating .small-star.filled {
      color: #ff9800;
    }
    
    .rating-text {
      font-size: 12px;
      color: #666;
      margin-left: 4px;
    }
    
    .comment-text {
      margin: 0;
      padding-left: 28px;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  `]
})
export class ApplicationCommentsDialogComponent implements OnInit {
  comments: ApplicationComment[] = [];
  newComment = '';
  newRating: number = 0;
  loading = false;

  constructor(
    public dialogRef: MatDialogRef<ApplicationCommentsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { applicationId: number },
    private commentService: ApplicationCommentService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.loading = true;
    this.commentService.getAllCandidateComments(this.data.applicationId).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to load comments', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  setRating(rating: number): void {
    if (!this.loading) {
      this.newRating = rating;
    }
  }

  clearRating(): void {
    this.newRating = 0;
  }

  addComment(): void {
    if (!this.newComment.trim()) {
      return;
    }

    this.loading = true;
    const rating = this.newRating > 0 ? this.newRating : undefined;
    
    this.commentService.addComment(this.data.applicationId, this.newComment.trim(), rating).subscribe({
      next: (comment) => {
        // Add to the beginning of the list
        this.comments.unshift(comment);
        this.newComment = '';
        this.newRating = 0;
        this.snackBar.open('Comment added successfully', 'Close', { duration: 2000 });
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to add comment', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  deleteComment(commentId: number): void {
    if (!confirm('Are you sure you want to delete this comment?')) {
      return;
    }

    this.loading = true;
    this.commentService.deleteComment(this.data.applicationId, commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter(c => c.id !== commentId);
        this.snackBar.open('Comment deleted', 'Close', { duration: 2000 });
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to delete comment', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
