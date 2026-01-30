import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CompanyService } from '../../services/company.service';
import { Company } from '../../models/company.model';
import { CompanyDialogComponent } from '../company-dialog/company-dialog.component';

@Component({
  selector: 'app-companies',
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
    MatDialogModule,
    MatTooltipModule
  ],
  template: `
    <div class="container">
      <div class="header">
        <h2>Companies</h2>
        <button mat-raised-button color="primary" (click)="openCreateDialog()">
          <mat-icon>add</mat-icon>
          New Company
        </button>
      </div>
      
      <div class="search-bar">
        <mat-form-field appearance="outline">
          <mat-label>Search companies</mat-label>
          <input matInput [(ngModel)]="searchTerm" (keyup.enter)="search()">
        </mat-form-field>
        <button mat-raised-button color="primary" (click)="search()">
          <mat-icon>search</mat-icon>
          Search
        </button>
      </div>

      <table mat-table [dataSource]="companies" class="mat-elevation-z8">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Name</th>
          <td mat-cell *matCellDef="let company">
            <a class="link" 
               (click)="navigateToApplications(company.name)" 
               matTooltip="View applications from this company">
              {{ company.name }}
            </a>
          </td>
        </ng-container>

        <ng-container matColumnDef="applicationCount">
          <th mat-header-cell *matHeaderCellDef>Applications</th>
          <td mat-cell *matCellDef="let company">{{ company.applicationCount || 0 }}</td>
        </ng-container>

        <ng-container matColumnDef="notes">
          <th mat-header-cell *matHeaderCellDef>Notes</th>
          <td mat-cell *matCellDef="let company">
            <span class="notes-preview">{{ company.notes || '-' }}</span>
          </td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let company">
            <button mat-icon-button color="primary" (click)="openEditDialog(company)"
                    matTooltip="Edit">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button color="warn" (click)="deleteCompany(company.id!)"
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

    .notes-preview {
      display: block;
      max-width: 300px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  `]
})
export class CompaniesComponent implements OnInit {
  companies: Company[] = [];
  displayedColumns: string[] = ['name', 'applicationCount', 'notes', 'actions'];
  searchTerm = '';

  constructor(
    private companyService: CompanyService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCompanies();
  }

  loadCompanies(): void {
    this.companyService.getAllCompanies().subscribe({
      next: (data) => {
        this.companies = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load companies', 'Close', { duration: 3000 });
      }
    });
  }

  search(): void {
    if (this.searchTerm.trim()) {
      this.companyService.searchCompanies(this.searchTerm).subscribe({
        next: (data) => {
          this.companies = data;
        },
        error: (error) => {
          this.snackBar.open('Search failed', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.loadCompanies();
    }
  }

  navigateToApplications(companyName: string): void {
    if (companyName) {
      this.router.navigate(['/dashboard/applications'], { 
        queryParams: { companyName } 
      });
    }
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(CompanyDialogComponent, {
      width: '500px',
      data: null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.createCompany(result);
      }
    });
  }

  openEditDialog(company: Company): void {
    const dialogRef = this.dialog.open(CompanyDialogComponent, {
      width: '500px',
      data: company
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateCompany(company.id!, result);
      }
    });
  }

  createCompany(companyData: Partial<Company>): void {
    this.companyService.createCompany(companyData.name!, companyData.notes).subscribe({
      next: () => {
        this.snackBar.open('Company created successfully', 'Close', { duration: 3000 });
        this.loadCompanies();
      },
      error: (error) => {
        this.snackBar.open('Failed to create company', 'Close', { duration: 3000 });
      }
    });
  }

  updateCompany(id: number, companyData: Partial<Company>): void {
    this.companyService.updateCompany(id, companyData.name!, companyData.notes).subscribe({
      next: () => {
        this.snackBar.open('Company updated successfully', 'Close', { duration: 3000 });
        this.loadCompanies();
      },
      error: (error) => {
        this.snackBar.open('Failed to update company', 'Close', { duration: 3000 });
      }
    });
  }

  deleteCompany(id: number): void {
    if (confirm('Are you sure you want to delete this company?')) {
      this.companyService.deleteCompany(id).subscribe({
        next: () => {
          this.snackBar.open('Company deleted', 'Close', { duration: 3000 });
          this.loadCompanies();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete company', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
