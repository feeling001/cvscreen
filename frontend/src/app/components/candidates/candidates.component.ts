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
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { CandidateService } from '../../services/candidate.service';
import { Candidate } from '../../models/candidate.model';
import { CandidateDialogComponent } from '../candidate-dialog/candidate-dialog.component';
import { CandidateMergeDialogComponent } from '../candidate-merge-dialog/candidate-merge-dialog.component';

@Component({
  selector: 'app-candidates',
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
    MatExpansionModule,
    MatCardModule,
    MatPaginatorModule,
    MatSelectModule,
    MatSortModule,
    MatTooltipModule,
    MatCheckboxModule,
    MatDividerModule,
    MatChipsModule
  ],
  templateUrl: './candidates.component.html',
  styleUrls: ['./candidates.component.css']
})
export class CandidatesComponent implements OnInit {
  candidates: Candidate[] = [];
  selectedCandidate: Candidate | null = null;
  displayedColumns: string[] = ['select', 'firstName', 'lastName', 'contractType', 'applicationCount', 'reviewCount', 'averageRating', 'actions'];
  applicationColumns: string[] = ['jobReference', 'roleCategory', 'companyName', 'status', 'applicationDate'];
  commentColumns: string[] = ['user', 'rating', 'comment', 'context', 'date'];
  searchTerm = '';
  
  // Selection for merge
  selectedCandidateIds: Set<number> = new Set();
  
  // Pagination
  currentPage = 0;
  pageSize = 100;
  totalItems = 0;
  totalPages = 0;
  pageSizeOptions = [25, 50, 100, 200];
  
  // Sorting
  sortBy = 'lastName';
  sortDirection: 'asc' | 'desc' = 'asc';

  constructor(
    private candidateService: CandidateService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCandidates();
    
    // Check for candidateId query parameter
    this.route.queryParams.subscribe(params => {
      const candidateId = params['candidateId'];
      if (candidateId) {
        this.viewCandidateDetails(+candidateId);
      }
    });
  }

  loadCandidates(): void {
    this.candidateService.getAllCandidates(this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
      next: (response) => {
        this.candidates = response.candidates || [];
        this.currentPage = response.currentPage;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
      },
      error: (error) => {
        this.snackBar.open('Failed to load candidates', 'Close', { duration: 3000 });
      }
    });
  }

  search(): void {
    this.currentPage = 0; // Reset to first page on new search
    if (this.searchTerm.trim()) {
      this.candidateService.searchCandidates(this.searchTerm, this.currentPage, this.pageSize, this.sortBy, this.sortDirection).subscribe({
        next: (response) => {
          this.candidates = response.candidates || [];
          this.currentPage = response.currentPage;
          this.totalItems = response.totalItems;
          this.totalPages = response.totalPages;
        },
        error: (error) => {
          this.snackBar.open('Search failed', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.loadCandidates();
    }
  }
  
  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    if (this.searchTerm.trim()) {
      this.search();
    } else {
      this.loadCandidates();
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
        this.loadCandidates();
      }
    }
  }

  viewCandidateDetails(id: number): void {
    this.candidateService.getCandidateById(id).subscribe({
      next: (data) => {
        this.selectedCandidate = data;
        // Scroll to details section
        setTimeout(() => {
          const element = document.getElementById('candidate-details');
          if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }, 100);
      },
      error: (error) => {
        this.snackBar.open('Failed to load candidate details', 'Close', { duration: 3000 });
      }
    });
  }

  closeDetails(): void {
    this.selectedCandidate = null;
  }
  
  // Navigate to applications page with candidate filter
  navigateToApplications(candidateName: string): void {
    if (candidateName) {
      this.router.navigate(['/dashboard/applications'], {
        queryParams: { candidateName }
      });
    }
  }
  
  // Selection management
  toggleSelection(candidateId: number): void {
    if (this.selectedCandidateIds.has(candidateId)) {
      this.selectedCandidateIds.delete(candidateId);
    } else {
      this.selectedCandidateIds.add(candidateId);
    }
  }
  
  isSelected(candidateId: number): boolean {
    return this.selectedCandidateIds.has(candidateId);
  }
  
  clearSelection(): void {
    this.selectedCandidateIds.clear();
  }
  
  // Merge candidates
  openMergeDialog(): void {
    if (this.selectedCandidateIds.size < 2) {
      this.snackBar.open('Please select at least 2 candidates to merge', 'Close', { duration: 3000 });
      return;
    }
    
    const selectedCandidates = this.candidates.filter(c => c.id && this.selectedCandidateIds.has(c.id));
    
    const dialogRef = this.dialog.open(CandidateMergeDialogComponent, {
      width: '600px',
      data: selectedCandidates
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.performMerge(result.targetCandidateId, result.candidateIds, result.mergedNotes);
      }
    });
  }
  
  performMerge(targetId: number, candidateIds: number[], mergedNotes: string): void {
    this.candidateService.mergeCandidates(targetId, candidateIds, mergedNotes).subscribe({
      next: () => {
        this.snackBar.open('Candidates merged successfully', 'Close', { duration: 3000 });
        this.clearSelection();
        this.loadCandidates();
        this.selectedCandidate = null;
      },
      error: (error) => {
        this.snackBar.open('Failed to merge candidates: ' + error.error?.message, 'Close', { duration: 5000 });
      }
    });
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(CandidateDialogComponent, {
      width: '500px',
      data: null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.createCandidate(result);
      }
    });
  }

  openEditDialog(candidate: Candidate): void {
    const dialogRef = this.dialog.open(CandidateDialogComponent, {
      width: '500px',
      data: candidate
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateCandidate(candidate.id!, result);
      }
    });
  }

  createCandidate(candidateData: Partial<Candidate>): void {
    this.candidateService.createCandidate(candidateData).subscribe({
      next: () => {
        this.snackBar.open('Candidate created successfully', 'Close', { duration: 3000 });
        this.loadCandidates();
      },
      error: (error) => {
        this.snackBar.open('Failed to create candidate', 'Close', { duration: 3000 });
      }
    });
  }

  updateCandidate(id: number, candidateData: Partial<Candidate>): void {
    this.candidateService.updateCandidate(id, candidateData).subscribe({
      next: () => {
        this.snackBar.open('Candidate updated successfully', 'Close', { duration: 3000 });
        this.loadCandidates();
        if (this.selectedCandidate && this.selectedCandidate.id === id) {
          this.viewCandidateDetails(id);
        }
      },
      error: (error) => {
        this.snackBar.open('Failed to update candidate', 'Close', { duration: 3000 });
      }
    });
  }

  deleteCandidate(id: number): void {
    if (confirm('Are you sure you want to delete this candidate?')) {
      this.candidateService.deleteCandidate(id).subscribe({
        next: () => {
          this.snackBar.open('Candidate deleted', 'Close', { duration: 3000 });
          if (this.selectedCandidate && this.selectedCandidate.id === id) {
            this.selectedCandidate = null;
          }
          this.loadCandidates();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete candidate', 'Close', { duration: 3000 });
        }
      });
    }
  }

  getStatusLabel(status: string): string {
    const statusLabels: { [key: string]: string } = {
      'CV_RECEIVED': 'CV Received',
      'CV_REVIEWED': 'CV Reviewed',
      'REMOTE_INTERVIEW': 'Remote Interview',
      'ONSITE_INTERVIEW': 'Onsite Interview',
      'APPROVED_FOR_MISSION': 'Approved',
      'REJECTED': 'Rejected',
      'ON_HOLD': 'On Hold'
    };
    return statusLabels[status] || status;
  }
  
  getStarArray(rating: number): number[] {
    return [1, 2, 3, 4, 5];
  }
}
