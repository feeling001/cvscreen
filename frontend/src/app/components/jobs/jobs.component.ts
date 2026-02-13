import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { JobService } from '../../services/job.service';
import { Job } from '../../models/job.model';
import { JobDialogComponent } from '../job-dialog/job-dialog.component';

@Component({
    selector: 'app-jobs',
    imports: [
        CommonModule,
        FormsModule,
        MatTableModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatSnackBarModule,
        MatDialogModule,
        MatCardModule,
        MatChipsModule
    ],
    template: `
    <div class="container">
      <div class="header">
        <h2>Jobs</h2>
        <button mat-raised-button color="primary" (click)="openCreateDialog()">
          <mat-icon>add</mat-icon>
          New Job
        </button>
      </div>
    
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
          <td mat-cell *matCellDef="let job">
            <mat-chip [class.status-open]="job.status === 'OPEN'"
              [class.status-closed]="job.status === 'CLOSED'"
              [class.status-hold]="job.status === 'ON_HOLD'">
              {{ job.status }}
            </mat-chip>
          </td>
        </ng-container>
    
        <ng-container matColumnDef="applicationCount">
          <th mat-header-cell *matHeaderCellDef>Applications</th>
          <td mat-cell *matCellDef="let job">
            <a class="link"
              (click)="navigateToApplications(job.reference)"
              matTooltip="View applications for this job">
              {{ job.applicationCount || 0 }}
            </a>
          </td>
        </ng-container>
    
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let job">
            <button mat-icon-button color="accent" (click)="viewJobDetails(job.id!)"
              matTooltip="View Details">
              <mat-icon>visibility</mat-icon>
            </button>
            <button mat-icon-button color="primary" (click)="openEditDialog(job)"
              matTooltip="Edit">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="deleteJob(job.id!)"
              matTooltip="Delete">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>
    
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"
        [class.highlighted-row]="selectedJob?.id === row.id"></tr>
      </table>
    
      <!-- Job Details Section -->
      @if (selectedJob) {
        <div id="job-details" class="details-section">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <div class="details-header">
                  <div>
                    <mat-icon>work</mat-icon>
                    {{ selectedJob.reference }} - {{ selectedJob.title }}
                  </div>
                  <button mat-icon-button (click)="closeDetails()" matTooltip="Close">
                    <mat-icon>close</mat-icon>
                  </button>
                </div>
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="detail-section">
                <h3>Job Information</h3>
                <div class="info-grid">
                  <div class="info-item">
                    <strong>Reference:</strong>
                    <span>{{ selectedJob.reference }}</span>
                  </div>
                  <div class="info-item">
                    <strong>Title:</strong>
                    <span>{{ selectedJob.title }}</span>
                  </div>
                  <div class="info-item">
                    <strong>Category:</strong>
                    <span>{{ selectedJob.category }}</span>
                  </div>
                  <div class="info-item">
                    <strong>Status:</strong>
                    <mat-chip [class.status-open]="selectedJob.status === 'OPEN'"
                      [class.status-closed]="selectedJob.status === 'CLOSED'"
                      [class.status-hold]="selectedJob.status === 'ON_HOLD'">
                      {{ selectedJob.status }}
                    </mat-chip>
                  </div>
                  <div class="info-item">
                    <strong>Source:</strong>
                    <span>{{ selectedJob.source || 'N/A' }}</span>
                  </div>
                  <div class="info-item">
                    <strong>Publication Date:</strong>
                    <span>{{ selectedJob.publicationDate ? (selectedJob.publicationDate | date:'dd/MM/yyyy') : 'N/A' }}</span>
                  </div>
                  <div class="info-item">
                    <strong>Applications:</strong>
                    <a class="link" (click)="navigateToApplications(selectedJob.reference)">
                      {{ selectedJob.applicationCount || 0 }} application(s)
                    </a>
                  </div>
                </div>
              </div>
              @if (selectedJob.description) {
                <div class="detail-section">
                  <h3>Description</h3>
                  <p class="description">{{ selectedJob.description }}</p>
                </div>
              }
            </mat-card-content>
          </mat-card>
        </div>
      }
    </div>
    `,
    styles: [`
    .container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .header h2 {
      margin: 0;
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

    .link {
      color: #3f51b5;
      cursor: pointer;
      text-decoration: none;
      font-weight: 500;
    }

    .link:hover {
      text-decoration: underline;
    }

    .status-open {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .status-closed {
      background-color: #f44336 !important;
      color: white !important;
    }

    .status-hold {
      background-color: #ff9800 !important;
      color: white !important;
    }

    .highlighted-row {
      background-color: #e3f2fd !important;
    }

    /* Job Details Section */
    .details-section {
      margin-top: 32px;
      animation: slideIn 0.3s ease-out;
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateY(20px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .details-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .details-header > div {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .detail-section {
      margin-bottom: 24px;
    }

    .detail-section h3 {
      color: #424242;
      margin-bottom: 16px;
      font-size: 18px;
      border-bottom: 2px solid #3f51b5;
      padding-bottom: 8px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .info-item strong {
      color: #666;
      font-size: 14px;
    }

    .info-item span {
      font-size: 16px;
    }

    .description {
      white-space: pre-wrap;
      line-height: 1.6;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 4px;
    }
  `]
})
export class JobsComponent implements OnInit {
  jobs: Job[] = [];
  selectedJob: Job | null = null;
  displayedColumns: string[] = ['reference', 'title', 'category', 'status', 'applicationCount', 'actions'];
  searchTerm = '';

  constructor(
    private jobService: JobService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadJobs();
    
    // Check for jobId query parameter
    this.route.queryParams.subscribe(params => {
      const jobId = params['jobId'];
      if (jobId) {
        this.viewJobDetails(+jobId);
      }
    });
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

  viewJobDetails(id: number): void {
    this.jobService.getJobById(id).subscribe({
      next: (data) => {
        this.selectedJob = data;
        // Scroll to details section
        setTimeout(() => {
          const element = document.getElementById('job-details');
          if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }, 100);
      },
      error: (error) => {
        this.snackBar.open('Failed to load job details', 'Close', { duration: 3000 });
      }
    });
  }

  closeDetails(): void {
    this.selectedJob = null;
  }

  navigateToApplications(jobReference: string): void {
    if (jobReference) {
      this.router.navigate(['/dashboard/applications'], { 
        queryParams: { jobReference } 
      });
    }
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(JobDialogComponent, {
      width: '600px',
      data: null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.createJob(result);
      }
    });
  }

  openEditDialog(job: Job): void {
    const dialogRef = this.dialog.open(JobDialogComponent, {
      width: '600px',
      data: job
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateJob(job.id!, result);
      }
    });
  }

  createJob(jobData: Partial<Job>): void {
    this.jobService.createJob(jobData).subscribe({
      next: () => {
        this.snackBar.open('Job created successfully', 'Close', { duration: 3000 });
        this.loadJobs();
      },
      error: (error) => {
        this.snackBar.open('Failed to create job', 'Close', { duration: 3000 });
      }
    });
  }

  updateJob(id: number, jobData: Partial<Job>): void {
    this.jobService.updateJob(id, jobData).subscribe({
      next: () => {
        this.snackBar.open('Job updated successfully', 'Close', { duration: 3000 });
        this.loadJobs();
        if (this.selectedJob && this.selectedJob.id === id) {
          this.viewJobDetails(id);
        }
      },
      error: (error) => {
        this.snackBar.open('Failed to update job', 'Close', { duration: 3000 });
      }
    });
  }

  deleteJob(id: number): void {
    if (confirm('Are you sure you want to delete this job?')) {
      this.jobService.deleteJob(id).subscribe({
        next: () => {
          this.snackBar.open('Job deleted', 'Close', { duration: 3000 });
          if (this.selectedJob && this.selectedJob.id === id) {
            this.selectedJob = null;
          }
          this.loadJobs();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete job', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
