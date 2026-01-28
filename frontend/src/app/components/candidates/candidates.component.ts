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
    MatDialogModule
  ],
  templateUrl: './candidates.component.html',
  styleUrls: ['./candidates.component.css']
})
export class CandidatesComponent implements OnInit {
  candidates: Candidate[] = [];
  displayedColumns: string[] = ['firstName', 'lastName', 'applicationCount', 'reviewCount', 'actions'];
  searchTerm = '';

  constructor(
    private candidateService: CandidateService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadCandidates();
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
          this.loadCandidates();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete candidate', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
