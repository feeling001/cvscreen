import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { Candidate } from '../../models/candidate.model';

@Component({
    selector: 'app-candidate-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule
],
    template: `
    <h2 mat-dialog-title>{{ data ? 'Edit Candidate' : 'New Candidate' }}</h2>
    <mat-dialog-content>
      <div class="form-container">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>First Name</mat-label>
          <input matInput [(ngModel)]="candidate.firstName" required>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Last Name</mat-label>
          <input matInput [(ngModel)]="candidate.lastName" required>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Contract Type</mat-label>
          <mat-select [(ngModel)]="candidate.contractType">
            <mat-option [value]="null">Not specified</mat-option>
            <mat-option value="Subcontractor">Subcontractor</mat-option>
            <mat-option value="Freelancer">Freelancer</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Global Notes</mat-label>
          <textarea matInput [(ngModel)]="candidate.globalNotes" rows="4"></textarea>
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onSave()" 
              [disabled]="!candidate.firstName || !candidate.lastName">
        Save
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    .form-container {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 400px;
      padding: 20px 0;
    }

    .full-width {
      width: 100%;
    }
  `]
})
export class CandidateDialogComponent {
  candidate: Partial<Candidate>;

  constructor(
    public dialogRef: MatDialogRef<CandidateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Candidate | null
  ) {
    this.candidate = data ? { ...data } : {
      firstName: '',
      lastName: '',
      contractType: '',
      globalNotes: ''
    };
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.candidate.firstName && this.candidate.lastName) {
      this.dialogRef.close(this.candidate);
    }
  }
}
