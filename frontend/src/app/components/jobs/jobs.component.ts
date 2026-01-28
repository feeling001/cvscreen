import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JobService } from '../../services/job.service';
import { Job } from '../../models/job.model';

@Component({
  selector: 'app-jobs',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="container">
      <h2>Jobs</h2>
      
      <div class="search-bar">
        <mat-form-field appearance="outline">
          <mat-label>Search jobs</mat-label>
          <input matInput [(ngModel)]="searchTerm" (keyup.enter)="search()">
        </mat-form-field>
        <button mat-raised-button color="primary" (click)="search()">
          <mat-icon>search</mat-icon>
          Search
        </button>
      </div>

      <table mat-table [dataSource]="jobs" class="mat-elevation-z8">
        <ng-container matColumnDef="reference">
          <th mat-header-cell *matHeaderCellDef>Reference</th>
          <td mat-cell *matCellDef="let job">{{ job.reference }}</td>
        </ng-container>

        <ng-container matColumnDef="title">
          <th mat-header-cell *matHeaderCellDef>Title</th>
          <td mat-cell *matCellDef="let job">{{ job.title }}</td>
        </ng-container>

        <ng-container matColumnDef="category">
          <th mat-header-cell *matHeaderCellDef>Category</th>
          <td mat-cell *matCellDef="let job">{{ job.category }}</td>
        </ng-container>

        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let job">{{ job.status }}</td>
        </ng-container>

        <ng-container matColumnDef="applicationCount">
          <th mat-header-cell *matHeaderCellDef>Applications</th>
          <td mat-cell *matCellDef="let job">{{ job.applicationCount || 0 }}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let job">
            <button mat-icon-button color="warn" (click)="deleteJob(job.id!)">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    </div>
  `,
  styles: [`
    .container {
      padding: 20px;
    }

    .search-bar {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      align-items: center;
    }

    .search-bar mat-form-field {
      flex: 1;
    }

    table {
      width: 100%;
    }
  `]
})
export class JobsComponent implements OnInit {
  jobs: Job[] = [];
  displayedColumns: string[] = ['reference', 'title', 'category', 'status', 'applicationCount', 'actions'];
  searchTerm = '';

  constructor(
    private jobService: JobService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.jobService.getAllJobs().subscribe({
      next: (data) => {
        this.jobs = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load jobs', 'Close', { duration: 3000 });
      }
    });
  }

  search(): void {
    if (this.searchTerm.trim()) {
      this.jobService.searchJobs(this.searchTerm).subscribe({
        next: (data) => {
          this.jobs = data;
        },
        error: (error) => {
          this.snackBar.open('Search failed', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.loadJobs();
    }
  }

  deleteJob(id: number): void {
    if (confirm('Are you sure you want to delete this job?')) {
      this.jobService.deleteJob(id).subscribe({
        next: () => {
          this.snackBar.open('Job deleted', 'Close', { duration: 3000 });
          this.loadJobs();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete job', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
