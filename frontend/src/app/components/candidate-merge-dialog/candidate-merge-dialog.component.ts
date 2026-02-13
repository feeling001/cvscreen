import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { Candidate } from '../../models/candidate.model';

@Component({
    selector: 'app-candidate-merge-dialog',
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatRadioModule,
        MatIconModule,
        MatListModule,
        MatDividerModule
    ],
    template: `
    <h2 mat-dialog-title>
      <mat-icon>merge</mat-icon>
      Merge Candidates
    </h2>
    <mat-dialog-content>
      <div class="merge-container">
        <div class="info-section">
          <mat-icon class="info-icon">info</mat-icon>
          <p>
            You are about to merge {{ candidates.length }} candidates. 
            Select which candidate to keep as the primary one. 
            All applications from the other candidates will be transferred to the selected candidate.
          </p>
        </div>

        <mat-divider></mat-divider>

        <h3>Select Primary Candidate:</h3>
        <mat-radio-group [(ngModel)]="selectedCandidateId" class="candidate-list">
          <mat-radio-button 
            *ngFor="let candidate of candidates" 
            [value]="candidate.id"
            class="candidate-option">
            <div class="candidate-info">
              <strong>{{ candidate.firstName }} {{ candidate.lastName }}</strong>
              <div class="candidate-details">
                <span class="detail-item">
                  <mat-icon class="small-icon">work</mat-icon>
                  {{ candidate.applicationCount || 0 }} applications
                </span>
                <span class="detail-item">
                  <mat-icon class="small-icon">comment</mat-icon>
                  {{ candidate.reviewCount || 0 }} reviews
                </span>
                <span class="detail-item" *ngIf="candidate.contractType">
                  <mat-icon class="small-icon">badge</mat-icon>
                  {{ candidate.contractType }}
                </span>
              </div>
            </div>
          </mat-radio-button>
        </mat-radio-group>

        <mat-divider></mat-divider>

        <h3>Merged Global Notes:</h3>
        <p class="help-text">
          All global notes from the selected candidates will be combined. 
          You can edit the combined text below:
        </p>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Combined Global Notes</mat-label>
          <textarea 
            matInput 
            [(ngModel)]="mergedNotes" 
            rows="8"
            placeholder="Global notes from all candidates will appear here...">
          </textarea>
        </mat-form-field>

        <div class="warning-section">
          <mat-icon class="warning-icon">warning</mat-icon>
          <p>
            <strong>Warning:</strong> This action cannot be undone. 
            The non-selected candidates will be permanently deleted after their applications are transferred.
          </p>
        </div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onMerge()" 
              [disabled]="!selectedCandidateId">
        <mat-icon>merge</mat-icon>
        Merge Candidates
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    .merge-container {
      min-width: 550px;
      padding: 20px 0;
    }

    .info-section {
      display: flex;
      gap: 12px;
      padding: 16px;
      background: #e3f2fd;
      border-radius: 4px;
      margin-bottom: 24px;
    }

    .info-icon {
      color: #1976d2;
      flex-shrink: 0;
    }

    .info-section p {
      margin: 0;
      line-height: 1.6;
    }

    h3 {
      margin: 24px 0 16px 0;
      color: #424242;
      font-size: 16px;
    }

    .candidate-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 24px;
    }

    .candidate-option {
      padding: 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      transition: all 0.2s;
    }

    .candidate-option:hover {
      background: #f5f5f5;
    }

    .candidate-info {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-left: 8px;
    }

    .candidate-details {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      font-size: 13px;
      color: #666;
    }

    .detail-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .small-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .help-text {
      font-size: 13px;
      color: #666;
      margin-bottom: 12px;
    }

    .full-width {
      width: 100%;
    }

    .warning-section {
      display: flex;
      gap: 12px;
      padding: 16px;
      background: #fff3e0;
      border-radius: 4px;
      margin-top: 24px;
      border-left: 4px solid #ff9800;
    }

    .warning-icon {
      color: #f57c00;
      flex-shrink: 0;
    }

    .warning-section p {
      margin: 0;
      line-height: 1.6;
      font-size: 13px;
    }

    mat-divider {
      margin: 24px 0;
    }
  `]
})
export class CandidateMergeDialogComponent {
  selectedCandidateId: number | null = null;
  mergedNotes: string = '';

  constructor(
    public dialogRef: MatDialogRef<CandidateMergeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public candidates: Candidate[]
  ) {
    // Pre-select the first candidate
    if (candidates.length > 0 && candidates[0].id) {
      this.selectedCandidateId = candidates[0].id;
    }

    // Combine all global notes
    this.mergedNotes = candidates
      .filter(c => c.globalNotes && c.globalNotes.trim())
      .map(c => `=== ${c.firstName} ${c.lastName} ===\n${c.globalNotes}`)
      .join('\n\n');
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onMerge(): void {
    if (!this.selectedCandidateId) {
      return;
    }

    const candidateIds = this.candidates
      .map(c => c.id!)
      .filter(id => id !== null && id !== undefined);

    this.dialogRef.close({
      targetCandidateId: this.selectedCandidateId,
      candidateIds: candidateIds,
      mergedNotes: this.mergedNotes
    });
  }
}
