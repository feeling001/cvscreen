import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { User } from '../../models/user.model';

interface DialogData {
  user: User | null;
  isAdmin: boolean;
  currentUserId: number;
}

@Component({
    selector: 'app-user-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule
],
    template: `
    <h2 mat-dialog-title>{{ isEditMode ? 'Edit User' : 'Create New User' }}</h2>
    <mat-dialog-content>
      <div class="form-container">
        <!-- Username (only for creation) -->
        @if (!isEditMode) {
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Username</mat-label>
            <input matInput [(ngModel)]="formData.username" required
              placeholder="e.g. jdoe">
              <mat-hint>Username cannot be changed after creation</mat-hint>
            </mat-form-field>
          }
    
          <!-- Username (read-only for edit) -->
          @if (isEditMode) {
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username</mat-label>
              <input matInput [value]="data.user?.username" disabled>
              <mat-hint>Username cannot be changed</mat-hint>
            </mat-form-field>
          }
    
          <!-- Display Name -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Display Name</mat-label>
            <input matInput [(ngModel)]="formData.displayName" required
              placeholder="e.g. John Doe">
            </mat-form-field>
    
            <!-- Password -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ isEditMode ? 'New Password (leave empty to keep current)' : 'Password' }}</mat-label>
              <input matInput type="password" [(ngModel)]="formData.password"
                [required]="!isEditMode"
                placeholder="Minimum 6 characters">
                @if (isEditMode) {
                  <mat-hint>Leave empty to keep current password</mat-hint>
                }
                @if (!isEditMode) {
                  <mat-hint>Minimum 6 characters</mat-hint>
                }
              </mat-form-field>
    
              <!-- Confirm Password (only for new users or when changing password) -->
              @if (!isEditMode || formData.password) {
                <mat-form-field appearance="outline" class="full-width"
                  >
                  <mat-label>Confirm Password</mat-label>
                  <input matInput type="password" [(ngModel)]="confirmPassword"
                    [required]="!isEditMode || formData.password"
                    placeholder="Re-enter password">
                    @if (confirmPassword && formData.password !== confirmPassword) {
                      <mat-error>
                        Passwords do not match
                      </mat-error>
                    }
                  </mat-form-field>
                }
    
                <!-- Enabled (only for admin) -->
                @if (isEditMode) {
                  <mat-checkbox [(ngModel)]="formData.enabled"
                    [disabled]="!data.isAdmin"
                    >
                    User Enabled
                  </mat-checkbox>
                }
    
                @if (isEditMode && !data.isAdmin) {
                  <div class="info-message">
                    <mat-icon>info</mat-icon>
                    <span>As a regular user, you can only edit your display name and password.</span>
                  </div>
                }
              </div>
            </mat-dialog-content>
            <mat-dialog-actions align="end">
              <button mat-button (click)="onCancel()">Cancel</button>
              <button mat-raised-button color="primary" (click)="onSave()"
                [disabled]="!isValid()">
                {{ isEditMode ? 'Update' : 'Create' }}
              </button>
            </mat-dialog-actions>
    `,
    styles: [`
    .form-container {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 450px;
      padding: 20px 0;
    }

    .full-width {
      width: 100%;
    }

    .info-message {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: #e3f2fd;
      border-radius: 4px;
      color: #1976d2;
      font-size: 13px;
    }

    .info-message mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    mat-checkbox {
      margin-top: 8px;
    }
  `]
})
export class UserDialogComponent {
  isEditMode: boolean;
  formData: any;
  confirmPassword = '';

  constructor(
    public dialogRef: MatDialogRef<UserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {
    this.isEditMode = !!data.user;
    
    if (this.isEditMode) {
      this.formData = {
        displayName: data.user!.displayName,
        password: '',
        enabled: data.user!.enabled
      };
    } else {
      this.formData = {
        username: '',
        displayName: '',
        password: '',
        enabled: true
      };
    }
  }

  isValid(): boolean {
    if (!this.isEditMode) {
      // Creating new user
      return !!(
        this.formData.username &&
        this.formData.displayName &&
        this.formData.password &&
        this.formData.password.length >= 6 &&
        this.formData.password === this.confirmPassword
      );
    } else {
      // Editing user
      const hasDisplayName = !!this.formData.displayName;
      
      // If password is being changed, validate it
      if (this.formData.password) {
        return hasDisplayName &&
               this.formData.password.length >= 6 &&
               this.formData.password === this.confirmPassword;
      }
      
      return hasDisplayName;
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (!this.isValid()) {
      return;
    }

    const result: any = {
      displayName: this.formData.displayName
    };

    if (!this.isEditMode) {
      result.username = this.formData.username;
      result.password = this.formData.password;
      result.enabled = this.formData.enabled;
    } else {
      // Only include password if it was changed
      if (this.formData.password) {
        result.password = this.formData.password;
      }
      
      // Only include enabled if user is admin
      if (this.data.isAdmin) {
        result.enabled = this.formData.enabled;
      }
    }

    this.dialogRef.close(result);
  }
}
