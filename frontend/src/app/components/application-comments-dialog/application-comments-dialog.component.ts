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
import { AuthService } from '../../services/auth.service';
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
  templateUrl: './application-comments-dialog.component.html',
  styleUrls: ['./application-comments-dialog.component.css']
})
export class ApplicationCommentsDialogComponent implements OnInit {
  comments: ApplicationComment[] = [];
  newComment = '';
  newRating: number = 0;
  loading = false;
  currentUsername: string = '';
  
  // Edit mode tracking
  editingCommentId: number | null = null;
  editingCommentText: string = '';
  editingCommentRating: number = 0;

  constructor(
    public dialogRef: MatDialogRef<ApplicationCommentsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { applicationId: number },
    private commentService: ApplicationCommentService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    const currentUser = this.authService.getCurrentUser();
    this.currentUsername = currentUser?.username || '';
  }

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
  
  // Start editing a comment
  startEditing(comment: ApplicationComment): void {
    this.editingCommentId = comment.id!;
    this.editingCommentText = comment.comment;
    this.editingCommentRating = comment.rating || 0;
  }
  
  // Cancel editing
  cancelEditing(): void {
    this.editingCommentId = null;
    this.editingCommentText = '';
    this.editingCommentRating = 0;
  }
  
  // Set rating for editing
  setEditingRating(rating: number): void {
    this.editingCommentRating = rating;
  }
  
  clearEditingRating(): void {
    this.editingCommentRating = 0;
  }
  
  // Update comment
  updateComment(commentId: number): void {
    if (!this.editingCommentText.trim()) {
      return;
    }

    this.loading = true;
    const rating = this.editingCommentRating > 0 ? this.editingCommentRating : undefined;
    
    this.commentService.updateComment(this.data.applicationId, commentId, this.editingCommentText.trim(), rating).subscribe({
      next: (updatedComment) => {
        // Update the comment in the list
        const index = this.comments.findIndex(c => c.id === commentId);
        if (index !== -1) {
          this.comments[index] = updatedComment;
        }
        this.cancelEditing();
        this.snackBar.open('Comment updated successfully', 'Close', { duration: 2000 });
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to update comment', 'Close', { duration: 3000 });
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
  
  isOwnComment(comment: ApplicationComment): boolean {
    return comment.username === this.currentUsername;
  }
  
  isEditing(comment: ApplicationComment): boolean {
    return this.editingCommentId === comment.id;
  }

  onClose(): void {
    this.dialogRef.close();
  }
}
