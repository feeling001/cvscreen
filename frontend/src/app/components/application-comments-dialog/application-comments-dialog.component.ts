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
    MatSnackBarModule
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
        <h3>Comments ({{ comments.length }})</h3>
        
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
                </div>
                <div class="comment-actions">
                  <span class="comment-date">{{ comment.createdAt | date:'short' }}</span>
                  <button 
                    mat-icon-button 
                    color="warn" 
                    (click)="deleteComment(comment.id!)"
                    matTooltip="Delete comment">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
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
      min-width: 600px;
      max-height: 70vh;
    }
    
    .add-comment-section {
      margin-bottom: 20px;
    }
    
    .full-width {
      width: 100%;
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
    }
    
    .comment-author mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
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
    this.commentService.getComments(this.data.applicationId).subscribe({
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

  addComment(): void {
    if (!this.newComment.trim()) {
      return;
    }

    this.loading = true;
    this.commentService.addComment(this.data.applicationId, this.newComment.trim()).subscribe({
      next: (comment) => {
        this.comments.unshift(comment);
        this.newComment = '';
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
