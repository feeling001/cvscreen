import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';

interface DuplicatePair {
  candidate1: any;
  candidate2: any;
  similarityScore: number;
}

@Component({
  selector: 'app-candidate-duplicates-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatChipsModule,
    MatCardModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>compare_arrows</mat-icon>
      Compare Duplicate Candidates
    </h2>
    <mat-dialog-content>
      <div class="comparison-container">
        <div class="similarity-badge" [class.high-similarity]="data.similarityScore >= 0.95">
          <mat-icon>{{ data.similarityScore >= 0.95 ? 'warning' : 'info' }}</mat-icon>
          <span>{{ getSimilarityPercentage() }}% Similar</span>
        </div>

        <div class="comparison-grid">
          <!-- Candidate 1 -->
          <mat-card class="candidate-card">
            <mat-card-header>
              <mat-card-title>
                <mat-icon>person</mat-icon>
                Candidate 1
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="info-section">
                <div class="info-item">
                  <strong>Full Name:</strong>
                  <span>{{ data.candidate1.firstName }} {{ data.candidate1.lastName }}</span>
                </div>
                <div class="info-item">
                  <strong>Contract Type:</strong>
                  <span>{{ data.candidate1.contractType || 'Not specified' }}</span>
                </div>
                <div class="info-item">
                  <strong>Applications:</strong>
                  <span>{{ data.candidate1.applicationCount || 0 }}</span>
                </div>
                <div class="info-item">
                  <strong>Reviews:</strong>
                  <span>{{ data.candidate1.reviewCount || 0 }}</span>
                </div>
                <div class="info-item" *ngIf="data.candidate1.averageRating">
                  <strong>Average Rating:</strong>
                  <span>{{ data.candidate1.averageRating | number:'1.1-1' }}/5</span>
                </div>
                <div class="info-item" *ngIf="data.candidate1.globalNotes">
                  <strong>Notes:</strong>
                  <span class="notes">{{ data.candidate1.globalNotes }}</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- VS Divider -->
          <div class="vs-divider">
            <mat-icon>compare_arrows</mat-icon>
            <span>VS</span>
          </div>

          <!-- Candidate 2 -->
          <mat-card class="candidate-card">
            <mat-card-header>
              <mat-card-title>
                <mat-icon>person</mat-icon>
                Candidate 2
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="info-section">
                <div class="info-item">
                  <strong>Full Name:</strong>
                  <span>{{ data.candidate2.firstName }} {{ data.candidate2.lastName }}</span>
                </div>
                <div class="info-item">
                  <strong>Contract Type:</strong>
                  <span>{{ data.candidate2.contractType || 'Not specified' }}</span>
                </div>
                <div class="info-item">
                  <strong>Applications:</strong>
                  <span>{{ data.candidate2.applicationCount || 0 }}</span>
                </div>
                <div class="info-item">
                  <strong>Reviews:</strong>
                  <span>{{ data.candidate2.reviewCount || 0 }}</span>
                </div>
                <div class="info-item" *ngIf="data.candidate2.averageRating">
                  <strong>Average Rating:</strong>
                  <span>{{ data.candidate2.averageRating | number:'1.1-1' }}/5</span>
                </div>
                <div class="info-item" *ngIf="data.candidate2.globalNotes">
                  <strong>Notes:</strong>
                  <span class="notes">{{ data.candidate2.globalNotes }}</span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <div class="action-section">
          <p class="merge-info">
            <mat-icon>info</mat-icon>
            You can merge these candidates to combine their applications and data.
          </p>
          <button mat-raised-button color="accent" (click)="onMerge()">
            <mat-icon>merge</mat-icon>
            Merge These Candidates
          </button>
        </div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onClose()">Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .comparison-container {
      min-width: 800px;
      padding: 20px 0;
    }

    .similarity-badge {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 16px;
      margin-bottom: 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 8px;
      font-size: 18px;
      font-weight: 600;
      box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    }

    .similarity-badge.high-similarity {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      animation: pulse 2s infinite;
    }

    @keyframes pulse {
      0%, 100% { transform: scale(1); }
      50% { transform: scale(1.02); }
    }

    .similarity-badge mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .comparison-grid {
      display: grid;
      grid-template-columns: 1fr auto 1fr;
      gap: 24px;
      align-items: start;
    }

    .candidate-card {
      height: 100%;
    }

    .candidate-card mat-card-header {
      background: #f5f5f5;
      padding: 16px;
      margin: -16px -16px 16px -16px;
    }

    .candidate-card mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      font-size: 16px;
    }

    .vs-divider {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 20px 0;
    }

    .vs-divider mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #667eea;
    }

    .vs-divider span {
      font-size: 24px;
      font-weight: 600;
      color: #667eea;
    }

    .info-section {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .info-item strong {
      font-size: 12px;
      color: #666;
      text-transform: uppercase;
    }

    .info-item span {
      font-size: 14px;
    }

    .notes {
      white-space: pre-wrap;
      background: #f9f9f9;
      padding: 8px;
      border-radius: 4px;
      font-size: 13px;
      max-height: 100px;
      overflow-y: auto;
    }

    .action-section {
      margin-top: 32px;
      padding: 20px;
      background: #fff3e0;
      border-radius: 8px;
      border-left: 4px solid #ff9800;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .merge-info {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      color: #e65100;
      font-weight: 500;
    }

    .merge-info mat-icon {
      color: #ff9800;
    }

    .action-section button {
      align-self: flex-start;
    }
  `]
})
export class CandidateDuplicatesDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<CandidateDuplicatesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DuplicatePair
  ) {}

  getSimilarityPercentage(): number {
    return Math.round(this.data.similarityScore * 100);
  }

  onClose(): void {
    this.dialogRef.close();
  }

  onMerge(): void {
    this.dialogRef.close({ action: 'merge', candidates: [this.data.candidate1, this.data.candidate2] });
  }
}
