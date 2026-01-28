import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ApplicationService } from '../../services/application.service';
import { Application } from '../../models/application.model';
import { ApplicationDialogComponent } from '../application-dialog/application-dialog.component';

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
    MatSnackBarModule,
    MatDialogModule
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
      
      <div class="search-bar">
        <mat-form-field appearance="outline">
          <mat-label>Candidate name</mat-label>
          <input matInput [(ngModel)]="candidateName">
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Job reference</mat-label>
          <input matInput [(ngModel)]="jobReference">
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Company</mat-label>
          <input matInput [(ngModel)]="companyName">
        </mat-form-field>
        <button mat-raised-button color="primary" (click)="search()">
          <mat-icon>search</mat-icon>
          Search
        </button>
        <button mat-raised-button (click)="clearSearch()">
          <mat-icon>clear</mat-icon>
          Clear
        </button>
      </div>

      <table mat-table [dataSource]="applications" class="mat-elevation-z8">
        <ng-container matColumnDef="candidateName">
          <th mat-header-cell *matHeaderCellDef>Candidate</th>
          <td mat-cell *matCellDef="let app">{{ app.candidateName }}</td>
        </ng-container>

        <ng-container matColumnDef="jobReference">
          <th mat-header-cell *matHeaderCellDef>Job Reference</th>
          <td mat-cell *matCellDef="let app">{{ app.jobReference || 'N/A' }}</td>
        </ng-container>

        <ng-container matColumnDef="roleCategory">
          <th mat-header-cell *matHeaderCellDef>Role</th>
          <td mat-cell *matCellDef="let app">{{ app.roleCategory }}</td>
        </ng-container>

        <ng-container matColumnDef="companyName">
          <th mat-header-cell *matHeaderCellDef>Company</th>
          <td mat-cell *matCellDef="let app">{{ app.companyName || 'N/A' }}</td>
        </ng-container>

        <ng-container matColumnDef="dailyRate">
          <th mat-header-cell *matHeaderCellDef>Daily Rate</th>
          <td mat-cell *matCellDef="let app">{{ app.dailyRate || 'N/A' }}</td>
        </ng-container>

        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let app">{{ app.status }}</td>
        </ng-container>

        <ng-container matColumnDef="applicationDate">
          <th mat-header-cell *matHeaderCellDef>Date</th>
          <td mat-cell *matCellDef="let app">{{ app.applicationDate | date:'shortDate' }}</td>
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
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
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

    .search-bar {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      align-items: center;
      flex-wrap: wrap;
    }

    .search-bar mat-form-field {
      flex: 1;
      min-width: 150px;
    }

    table {
      width: 100%;
    }
  `]
})
export class ApplicationsComponent implements OnInit {
  applications: Application[] = [];
  displayedColumns: string[] = ['candidateName', 'jobReference', 'roleCategory', 'companyName', 'dailyRate', 'status', 'applicationDate', 'actions'];
  
  candidateName = '';
  jobReference = '';
  companyName = '';

  constructor(
    private applicationService: ApplicationService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadApplications();
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

  search(): void {
    const filters = {
      candidateName: this.candidateName.trim(),
      jobReference: this.jobReference.trim(),
      companyName: this.companyName.trim()
    };

    this.applicationService.searchApplications(filters).subscribe({
      next: (data) => {
        this.applications = data;
      },
      error: (error) => {
        this.snackBar.open('Search failed', 'Close', { duration: 3000 });
      }
    });
  }

  clearSearch(): void {
    this.candidateName = '';
    this.jobReference = '';
    this.companyName = '';
    this.loadApplications();
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
        this.loadApplications();
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
          this.loadApplications();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete application', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
