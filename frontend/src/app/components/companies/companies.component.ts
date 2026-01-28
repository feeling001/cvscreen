import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CompanyService } from '../../services/company.service';
import { Company } from '../../models/company.model';

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
    MatSnackBarModule
  ],
  template: `
    <div class="container">
      <h2>Companies</h2>
      
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
          <td mat-cell *matCellDef="let company">{{ company.name }}</td>
        </ng-container>

        <ng-container matColumnDef="applicationCount">
          <th mat-header-cell *matHeaderCellDef>Applications</th>
          <td mat-cell *matCellDef="let company">{{ company.applicationCount || 0 }}</td>
        </ng-container>

        <ng-container matColumnDef="notes">
          <th mat-header-cell *matHeaderCellDef>Notes</th>
          <td mat-cell *matCellDef="let company">{{ company.notes || '-' }}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Actions</th>
          <td mat-cell *matCellDef="let company">
            <button mat-icon-button color="warn" (click)="deleteCompany(company.id!)">
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
export class CompaniesComponent implements OnInit {
  companies: Company[] = [];
  displayedColumns: string[] = ['name', 'applicationCount', 'notes', 'actions'];
  searchTerm = '';

  constructor(
    private companyService: CompanyService,
    private snackBar: MatSnackBar
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
