import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { CandidateService } from '../../services/candidate.service';
import { Candidate } from '../../models/candidate.model';
import { CandidateDialogComponent } from '../candidate-dialog/candidate-dialog.component';

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
    MatCardModule
  ],
  templateUrl: './candidates.component.html',
  styleUrls: ['./candidates.component.css']
})
export class CandidatesComponent implements OnInit {
  candidates: Candidate[] = [];
  selectedCandidate: Candidate | null = null;
  displayedColumns: string[] = ['firstName', 'lastName', 'applicationCount', 'actions'];
  applicationColumns: string[] = ['jobReference', 'roleCategory', 'companyName', 'status', 'applicationDate'];
  searchTerm = '';

  constructor(
    private candidateService: CandidateService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private route: ActivatedRoute
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
    this.candidateService.getAllCandidates().subscribe({
      next: (data) => {
        this.candidates = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load candidates', 'Close', { duration: 3000 });
      }
    });
  }

  search(): void {
    if (this.searchTerm.trim()) {
      this.candidateService.searchCandidates(this.searchTerm).subscribe({
        next: (data) => {
          this.candidates = data;
        },
        error: (error) => {
          this.snackBar.open('Search failed', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.loadCandidates();
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
}
