import { Component, Inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { Job, JobStatus } from '../../models/job.model';

@Component({
    selector: 'app-job-dialog',
    imports: [
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule
],
    template: `
    <h2 mat-dialog-title>{{ data ? 'Edit Job' : 'New Job' }}</h2>
    <mat-dialog-content>
      <div class="form-container">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Reference</mat-label>
          <input matInput [(ngModel)]="job.reference" required 
                 placeholder="e.g. I01234">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Title</mat-label>
          <input matInput [(ngModel)]="job.title" required>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Category</mat-label>
          <input matInput [(ngModel)]="job.category" required 
                 placeholder="e.g. System Architect, Developer">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Publication Date</mat-label>
          <input matInput [matDatepicker]="picker" [(ngModel)]="job.publicationDate">
          <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
          <mat-datepicker #picker></mat-datepicker>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Status</mat-label>
          <mat-select [(ngModel)]="job.status">
            <mat-option value="OPEN">Open</mat-option>
            <mat-option value="CLOSED">Closed</mat-option>
            <mat-option value="ON_HOLD">On Hold</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Source</mat-label>
          <input matInput [(ngModel)]="job.source" placeholder="e.g. Pro-Unity">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <textarea matInput [(ngModel)]="job.description" rows="4"></textarea>
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onSave()" 
              [disabled]="!job.reference || !job.title || !job.category">
        Save
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    .form-container {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 500px;
      padding: 20px 0;
      max-height: 70vh;
      overflow-y: auto;
    }

    .full-width {
      width: 100%;
    }
  `]
})
export class JobDialogComponent {
  job: Partial<Job>;

  constructor(
    public dialogRef: MatDialogRef<JobDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Job | null
  ) {
    this.job = data ? { ...data } : {
      reference: '',
      title: '',
      category: '',
      status: 'OPEN',
      source: 'Pro-Unity',
      description: ''
    };
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.job.reference && this.job.title && this.job.category) {
      this.dialogRef.close(this.job);
    }
  }
}
