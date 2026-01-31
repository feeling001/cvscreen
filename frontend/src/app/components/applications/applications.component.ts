import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApplicationService } from '../../services/application.service';
import { CompanyService } from '../../services/company.service';
import { Application } from '../../models/application.model';
import { ApplicationDialogComponent } from '../application-dialog/application-dialog.component';
import { ApplicationCommentsDialogComponent } from '../application-comments-dialog/application-comments-dialog.component';

@Component({
  selector: 'app-applications',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatSnackBarModule,
    MatDialogModule,
    MatChipsModule,
    MatBadgeModule,
    MatTooltipModule
  ],
  template: `
    <div class="container">
      <div class="header">
        <h2>Applications</h2>
        <button mat-raised-button color="primary" (click)="openCreateDialog()">
          <mat-icon>add</mat-icon>
          New Application
        </button>
      </div>
      
      <div class="filters-section">
        <div class="search-row">
          <mat-form-field appearance="outline">
            <mat-label>Search (name, role, date)</mat-label>
            <input matInput [(ngModel)]="searchTerm" 
                   placeholder="e.g. John, Architect, 2024-01-15"
                   (keyup.enter)="search()">
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
        </div>

        <div class="filters-row">
          <mat-form-field appearance="outline">
            <mat-label>Filter by Status</mat-label>
            <mat-select [(ngModel)]="filterStatus" (selectionChange)="applyFilters()">
              <mat-option [value]="null">All Statuses</mat-option>
              <mat-option value="CV_RECEIVED">CV Received</mat-option>
              <mat-option value="CV_REVIEWED">CV Reviewed</mat-option>
              <mat-option value="REMOTE_INTERVIEW">Remote Interview</mat-option>
              <mat-option value="ONSITE_INTERVIEW">Onsite Interview</mat-option>
              <mat-option value="APPROVED_FOR_MISSION">Approved for Mission</mat-option>
              <mat-option value="REJECTED">Rejected</mat-option>
              <mat-option value="ON_HOLD">On Hold</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Filter by Company</mat-label>
            <mat-select [(ngModel)]="filterCompany" (selectionChange)="applyFilters()">
              <mat-option [value]="null">All Companies</mat-option>
              <mat-option *ngFor="let company of companies" [value]="company.name">
                {{ company.name }}
              </mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Filter by Job Reference</mat-label>
            <input matInput [(ngModel)]="filterJobReference" 
                   placeholder="e.g. I01234"
                   (keyup.enter)="applyFilters()">
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Filter by Role</mat-label>
            <input matInput [(ngModel)]="filterRole" 
                   placeholder="e.g. Architect"
                   (keyup.enter)="applyFilters()">
          </mat-form-field>

          <button mat-raised-button color="primary" (click)="applyFilters()">
            <mat-icon>filter_list</mat-icon>
            Apply Filters
          </button>
          
          <button mat-raised-button (click)="clearFilters()">
            <mat-icon>clear</mat-icon>
            Clear All
          </button>
        </div>

        <div class="active-filters" *ngIf="hasActiveFilters()">
          <mat-chip-set>
            <mat-chip *ngIf="searchTerm" (removed)="searchTerm = ''; applyFilters()">
              Search: {{ searchTerm }}
              <button matChipRemove><mat-icon>cancel</mat-icon></button>
            </mat-chip>
            <mat-chip *ngIf="filterStatus" (removed)="filterStatus = null; applyFilters()">
              Status: {{ getStatusLabel(filterStatus) }}
              <button matChipRemove><mat-icon>cancel</mat-icon></button>
            </mat-chip>
            <mat-chip *ngIf="filterCompany" (removed)="filterCompany = null; applyFilters()">
              Company: {{ filterCompany }}
              <button matChipRemove><mat-icon>cancel</mat-icon></button>
            </mat-chip>
            <mat-chip *ngIf="filterJobReference" (removed)="filterJobReference = ''; applyFilters()">
              Job: {{ filterJobReference }}
              <button matChipRemove><mat-icon>cancel</mat-icon></button>
            </mat-chip>
            <mat-chip *ngIf="filterRole" (removed)="filterRole = ''; applyFilters()">
              Role: {{ filterRole }}
              <button matChipRemove><mat-icon>cancel</mat-icon></button>
            </mat-chip>
          </mat-chip-set>
        </div>
      </div>

      <div class="results-info">
        <p>Showing {{ applications.length }} application(s)</p>
      </div>

      <table mat-table [dataSource]="applications" class="mat-elevation-z8">
        <ng-container matColumnDef="candidateName">
          <th mat-header-cell *matHeaderCellDef>Candidate</th>
          <td mat-cell *matCellDef="let app">
            <a class="link" (click)="navigateToCandidate(app.candidateId)" 
               matTooltip="View candidate details">
              {{ app.candidateName }}
            </a>
          </td>
        </ng-container>

        <ng-container matColumnDef="jobReference">
          <th mat-header-cell *matHeaderCellDef>Job Reference</th>
          <td mat-cell *matCellDef="let app">
            <a *ngIf="app.jobReference" 
               class="link" 
               (click)="navigateToJob(app.jobId)" 
               matTooltip="View job details">
              {{ app.jobReference }}
            </a>
            <span *ngIf="!app.jobReference" class="spontaneous">Spontaneous</span>
          </td>
        </ng-container>

        <ng-container matColumnDef="roleCategory">
          <th mat-header-cell *matHeaderCellDef>Role</th>
          <td mat-cell *matCellDef="let app">{{ app.roleCategory }}</td>
        </ng-container>

        <ng-container matColumnDef="companyName">
          <th mat-header-cell *matHeaderCellDef>Company</th>
          <td mat-cell *matCellDef="let app">{{ app.companyName || '-' }}</td>
        </ng-container>

        <ng-container matColumnDef="dailyRate">
          <th mat-header-cell *matHeaderCellDef>Daily Rate</th>
          <td mat-cell *matCellDef="let app">{{ app.dailyRate ? app.dailyRate + ' €' : '-' }}</td>
        </ng-container>

        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let app" 
              [class.approved-status]="app.status === 'APPROVED_FOR_MISSION'">
            <span class="status-badge" [class.approved]="app.status === 'APPROVED_FOR_MISSION'">
              {{ getStatusLabel(app.status) }}
            </span>
          </td>
        </ng-container>

        <ng-container matColumnDef="rating">
          <th mat-header-cell *matHeaderCellDef>Rating</th>
          <td mat-cell *matCellDef="let app">
            <div class="rating-display" *ngIf="app.averageRating && app.averageRating > 0">
              <mat-icon 
                *ngFor="let star of getStarArray(app.averageRating!)" 
                [class.filled]="star <= app.averageRating!"
                [class.half-filled]="star > app.averageRating! && star - 0.5 <= app.averageRating!"
                class="rating-star"
                matTooltip="Average rating: {{ app.averageRating | number:'1.1-1' }}/5">
                {{ star <= app.averageRating! ? 'star' : (star - 0.5 <= app.averageRating! ? 'star_half' : 'star_border') }}
              </mat-icon>
              <span class="rating-value">{{ app.averageRating | number:'1.1-1' }}</span>
            </div>
            <span *ngIf="!app.averageRating || app.averageRating === 0" class="no-rating">-</span>
          </td>
        </ng-container>

        <ng-container matColumnDef="applicationDate">
          <th mat-header-cell *matHeaderCellDef>Date</th>
          <td mat-cell *matCellDef="let app">{{ app.applicationDate | date:'dd/MM/yyyy' }}</td>
        </ng-container>

        <ng-container matColumnDef="comments">
          <th mat-header-cell *matHeaderCellDef>Comments</th>
          <td mat-cell *matCellDef="let app">
            <button 
              mat-icon-button 
              color="accent" 
              (click)="openCommentsDialog(app)"
              [matBadge]="app.commentCount || 0"
              [matBadgeHidden]="!app.commentCount"
              matBadgeColor="warn"
              matTooltip="View/Add Comments">
              <mat-icon>comment</mat-icon>
            </button>
          </td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let app">
            <button mat-icon-button color="primary" (click)="openEditDialog(app)"
                    matTooltip="Edit">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="deleteApplication(app.id!)"
                    matTooltip="Delete">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;" 
            [class.approved-row]="row.status === 'APPROVED_FOR_MISSION'"></tr>
      </table>
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

    .filters-section {
      background: #f5f5f5;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 24px;
    }

    .search-row {
      margin-bottom: 16px;
    }

    .search-row mat-form-field {
      width: 100%;
    }

    .filters-row {
      display: flex;
      gap: 16px;
      align-items: center;
      flex-wrap: wrap;
    }

    .filters-row mat-form-field {
      flex: 1;
      min-width: 180px;
    }

    .active-filters {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #ddd;
    }

    .active-filters mat-chip-set {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .results-info {
      margin-bottom: 16px;
      color: #666;
    }

    .results-info p {
      margin: 0;
      font-size: 14px;
    }

    table {
      width: 100%;
    }

    .approved-row {
      background-color: #e8f5e9 !important;
    }

    .approved-row:hover {
      background-color: #c8e6c9 !important;
    }

    .status-badge {
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      background-color: #e0e0e0;
      color: #333;
      display: inline-block;
    }

    .status-badge.approved {
      background-color: #4caf50;
      color: white;
      font-weight: 600;
    }

    .approved-status {
      font-weight: 600;
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

    .spontaneous {
      color: #999;
      font-style: italic;
    }

    .rating-display {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .rating-star {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: #ccc;
    }

    .rating-star.filled {
      color: #ff9800;
    }

    .rating-star.half-filled {
      color: #ffb74d;
    }

    .rating-value {
      font-size: 12px;
      color: #666;
      margin-left: 4px;
      font-weight: 500;
    }

    .no-rating {
      color: #999;
    }
  `]
})
export class ApplicationsComponent implements OnInit {
  applications: Application[] = [];
  companies: any[] = [];
  displayedColumns: string[] = ['candidateName', 'jobReference', 'roleCategory', 'companyName', 'dailyRate', 'status', 'rating', 'applicationDate', 'comments', 'actions'];
  
  searchTerm = '';
  filterStatus: string | null = null;
  filterCompany: string | null = null;
  filterJobReference = '';
  filterRole = '';

  constructor(
    private applicationService: ApplicationService,
    private companyService: CompanyService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadCompanies();
    
    this.route.queryParams.subscribe(params => {
      const jobReference = params['jobReference'];
      const companyName = params['companyName'];
      
      if (jobReference) {
        this.filterJobReference = jobReference;
        this.applyFilters();
      } else if (companyName) {
        this.filterCompany = companyName;
        this.applyFilters();
      } else {
        this.loadApplications();
      }
    });
  }

  loadApplications(): void {
    this.applicationService.getAllApplications().subscribe({
      next: (data) => {
        this.applications = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load applications', 'Close', { duration: 3000 });
      }
    });
  }

  loadCompanies(): void {
    this.companyService.getAllCompanies().subscribe({
      next: (data) => {
        this.companies = data;
      },
      error: (error) => {
        console.error('Failed to load companies', error);
      }
    });
  }

  search(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    const filters: any = {};

    if (this.searchTerm.trim()) {
      filters.candidateName = this.searchTerm.trim();
    }

    if (this.filterStatus) {
      filters.status = this.filterStatus;
    }

    if (this.filterCompany) {
      filters.companyName = this.filterCompany;
    }

    if (this.filterJobReference.trim()) {
      filters.jobReference = this.filterJobReference.trim();
    }

    if (this.filterRole.trim()) {
      filters.roleCategory = this.filterRole.trim();
    }

    if (Object.keys(filters).length > 0) {
      this.applicationService.searchApplications(filters).subscribe({
        next: (data) => {
          this.applications = data;
        },
        error: (error) => {
          this.snackBar.open('Search failed', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.loadApplications();
    }
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.filterStatus = null;
    this.filterCompany = null;
    this.filterJobReference = '';
    this.filterRole = '';
    
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
    
    this.loadApplications();
  }

  hasActiveFilters(): boolean {
    return !!(this.searchTerm || this.filterStatus || this.filterCompany || this.filterJobReference || this.filterRole);
  }

  getStatusLabel(status: string): string {
    const statusLabels: { [key: string]: string } = {
      'CV_RECEIVED': 'CV Received',
      'CV_REVIEWED': 'CV Reviewed',
      'REMOTE_INTERVIEW': 'Remote Interview',
      'ONSITE_INTERVIEW': 'Onsite Interview',
      'APPROVED_FOR_MISSION': 'Approved for Mission',
      'REJECTED': 'Rejected',
      'ON_HOLD': 'On Hold'
    };
    return statusLabels[status] || status;
  }

  getStarArray(rating: number): number[] {
    return [1, 2, 3, 4, 5];
  }

  navigateToCandidate(candidateId: number): void {
    if (candidateId) {
      this.router.navigate(['/dashboard/candidates'], { 
        queryParams: { candidateId } 
      });
    }
  }

  navigateToJob(jobId: number): void {
    if (jobId) {
      this.router.navigate(['/dashboard/jobs'], { 
        queryParams: { jobId } 
      });
    }
  }

  openCommentsDialog(application: Application): void {
    const dialogRef = this.dialog.open(ApplicationCommentsDialogComponent, {
      width: '800px',
      data: { applicationId: application.id }
    });

    dialogRef.afterClosed().subscribe(() => {
      this.applyFilters();
    });
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ApplicationDialogComponent, {
      width: '600px',
      data: null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.createApplication(result);
      }
    });
  }

  openEditDialog(application: Application): void {
    const dialogRef = this.dialog.open(ApplicationDialogComponent, {
      width: '600px',
      data: application
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateApplication(application.id!, result);
      }
    });
  }

  createApplication(applicationData: any): void {
    this.applicationService.createApplication(applicationData).subscribe({
      next: () => {
        this.snackBar.open('Application created successfully', 'Close', { duration: 3000 });
        this.loadApplications();
      },
      error: (error) => {
        this.snackBar.open('Failed to create application', 'Close', { duration: 3000 });
      }
    });
  }

  updateApplication(id: number, applicationData: any): void {
    this.applicationService.updateApplication(id, applicationData).subscribe({
      next: () => {
        this.snackBar.open('Application updated successfully', 'Close', { duration: 3000 });
        this.applyFilters();
      },
      error: (error) => {
        this.snackBar.open('Failed to update application', 'Close', { duration: 3000 });
      }
    });
  }

  deleteApplication(id: number): void {
    if (confirm('Are you sure you want to delete this application?')) {
      this.applicationService.deleteApplication(id).subscribe({
        next: () => {
          this.snackBar.open('Application deleted', 'Close', { duration: 3000 });
          this.applyFilters();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete application', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
