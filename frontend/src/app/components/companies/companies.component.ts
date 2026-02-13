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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { CompanyService } from '../../services/company.service';
import { Company } from '../../models/company.model';
import { CompanyDialogComponent } from '../company-dialog/company-dialog.component';
import { CompanyMergeDialogComponent } from '../company-merge-dialog/company-merge-dialog.component';

@Component({
    selector: 'app-companies',
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
        MatTooltipModule,
        MatCheckboxModule,
        MatPaginatorModule,
        MatSortModule
    ],
    templateUrl: './companies.component.html',
    styleUrls: ['./companies.component.css']
})
export class CompaniesComponent implements OnInit {
  companies: Company[] = [];
  displayedColumns: string[] = ['select', 'name', 'applicationCount', 'notes', 'actions'];
  searchTerm = '';
  
  // Selection for merge
  selectedCompanyIds: Set<number> = new Set();
  
  // Pagination
  currentPage = 0;
  pageSize = 50;
  totalItems = 0;
  totalPages = 0;
  pageSizeOptions = [25, 50, 100, 200];
  
  // Sorting - Default: applicationCount DESC
  sortBy = 'applicationCount';
  sortDirection: 'asc' | 'desc' = 'desc';

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
    this.companyService.getAllCompanies(this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
      next: (response) => {
        this.companies = response.companies || [];
        this.currentPage = response.currentPage;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
      },
      error: (error) => {
        this.snackBar.open('Failed to load companies', 'Close', { duration: 3000 });
      }
    });
  }

  search(): void {
    this.currentPage = 0; // Reset to first page on new search
    if (this.searchTerm.trim()) {
      this.companyService.searchCompanies(this.searchTerm, this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
        next: (response) => {
          this.companies = response.companies || [];
          this.currentPage = response.currentPage;
          this.totalItems = response.totalItems;
          this.totalPages = response.totalPages;
        },
        error: (error) => {
          this.snackBar.open('Search failed', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.loadCompanies();
    }
  }
  
  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    if (this.searchTerm.trim()) {
      this.search();
    } else {
      this.loadCompanies();
    }
  }
  
  onSortChange(sort: Sort): void {
    if (sort.direction) {
      this.sortBy = sort.active;
      this.sortDirection = sort.direction;
      this.currentPage = 0; // Reset to first page on sort change
      if (this.searchTerm.trim()) {
        this.search();
      } else {
        this.loadCompanies();
      }
    }
  }

  navigateToApplications(companyName: string): void {
    if (companyName) {
      this.router.navigate(['/dashboard/applications'], { 
        queryParams: { companyName } 
      });
    }
  }
  
  // Selection management
  toggleSelection(companyId: number): void {
    if (this.selectedCompanyIds.has(companyId)) {
      this.selectedCompanyIds.delete(companyId);
    } else {
      this.selectedCompanyIds.add(companyId);
    }
  }
  
  isSelected(companyId: number): boolean {
    return this.selectedCompanyIds.has(companyId);
  }
  
  clearSelection(): void {
    this.selectedCompanyIds.clear();
  }
  
  // Merge companies
  openMergeDialog(): void {
    if (this.selectedCompanyIds.size < 2) {
      this.snackBar.open('Please select at least 2 companies to merge', 'Close', { duration: 3000 });
      return;
    }
    
    const selectedCompanies = this.companies.filter(c => c.id && this.selectedCompanyIds.has(c.id));
    
    const dialogRef = this.dialog.open(CompanyMergeDialogComponent, {
      width: '600px',
      data: selectedCompanies
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.performMerge(result.targetCompanyId, result.companyIds, result.mergedNotes);
      }
    });
  }
  
  performMerge(targetId: number, companyIds: number[], mergedNotes: string): void {
    this.companyService.mergeCompanies(targetId, companyIds, mergedNotes).subscribe({
      next: () => {
        this.snackBar.open('Companies merged successfully', 'Close', { duration: 3000 });
        this.clearSelection();
        this.loadCompanies();
      },
      error: (error) => {
        this.snackBar.open('Failed to merge companies: ' + error.error?.message, 'Close', { duration: 5000 });
      }
    });
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
