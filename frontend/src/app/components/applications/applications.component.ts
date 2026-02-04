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
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
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
    MatTooltipModule,
    MatPaginatorModule,
    MatSortModule
  ],
  templateUrl: './applications.component.html',
  styleUrls: ['./applications.component.css']
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
  
  // Pagination
  currentPage = 0;
  pageSize = 100;
  totalItems = 0;
  totalPages = 0;
  pageSizeOptions = [25, 50, 100, 200];
  
  // Sorting
  sortBy = 'applicationDate';
  sortDirection: 'asc' | 'desc' = 'desc';

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
    
    // Check for query parameters - CORRECTED to handle candidateName
    this.route.queryParams.subscribe(params => {
      const jobReference = params['jobReference'];
      const companyName = params['companyName'];
      const candidateName = params['candidateName']; // ADDED
      
      if (jobReference) {
        this.filterJobReference = jobReference;
        this.applyFilters();
      } else if (companyName) {
        this.filterCompany = companyName;
        this.applyFilters();
      } else if (candidateName) { // ADDED
        this.searchTerm = candidateName;
        this.applyFilters();
      } else {
        this.loadApplications();
      }
    });
  }

  loadApplications(): void {
    this.applicationService.getAllApplications(this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
      next: (response) => {
        this.applications = response.applications || [];
        this.currentPage = response.currentPage;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
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
    this.currentPage = 0; // Reset to first page on filter change
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
      this.applicationService.searchApplications(filters, this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
        next: (response) => {
          this.applications = response.applications || [];
          this.currentPage = response.currentPage;
          this.totalItems = response.totalItems;
          this.totalPages = response.totalPages;
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
    this.currentPage = 0;
    
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
    
    this.loadApplications();
  }

  hasActiveFilters(): boolean {
    return !!(this.searchTerm || this.filterStatus || this.filterCompany || this.filterJobReference || this.filterRole);
  }
  
  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    if (this.hasActiveFilters()) {
      this.applyFilters();
    } else {
      this.loadApplications();
    }
  }
  
  onSortChange(sort: Sort): void {
    if (sort.direction) {
      this.sortBy = sort.active;
      this.sortDirection = sort.direction;
      this.currentPage = 0; // Reset to first page on sort change
      if (this.hasActiveFilters()) {
        this.applyFilters();
      } else {
        this.loadApplications();
      }
    }
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
