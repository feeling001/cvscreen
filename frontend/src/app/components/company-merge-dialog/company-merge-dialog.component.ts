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
import { Company } from '../../models/company.model';

@Component({
  selector: 'app-company-merge-dialog',
  standalone: true,
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
      Merge Companies
    </h2>
    <mat-dialog-content>
      <div class="merge-container">
        <div class="info-section">
          <mat-icon class="info-icon">info</mat-icon>
          <p>
            You are about to merge {{ companies.length }} companies. 
            Select which company to keep as the primary one. 
            All applications from the other companies will be transferred to the selected company.
          </p>
        </div>

        <mat-divider></mat-divider>

        <h3>Select Primary Company:</h3>
        <mat-radio-group [(ngModel)]="selectedCompanyId" class="company-list">
          <mat-radio-button 
            *ngFor="let company of companies" 
            [value]="company.id"
            class="company-option">
            <div class="company-info">
              <strong>{{ company.name }}</strong>
              <div class="company-details">
                <span class="detail-item">
                  <mat-icon class="small-icon">work</mat-icon>
                  {{ company.applicationCount || 0 }} applications
                </span>
                <span class="detail-item" *ngIf="company.notes">
                  <mat-icon class="small-icon">note</mat-icon>
                  Has notes
                </span>
              </div>
            </div>
          </mat-radio-button>
        </mat-radio-group>

        <mat-divider></mat-divider>

        <h3>Merged Notes:</h3>
        <p class="help-text">
          All notes from the selected companies will be combined. 
          You can edit the combined text below:
        </p>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Combined Notes</mat-label>
          <textarea 
            matInput 
            [(ngModel)]="mergedNotes" 
            rows="8"
            placeholder="Notes from all companies will appear here...">
          </textarea>
        </mat-form-field>

        <div class="warning-section">
          <mat-icon class="warning-icon">warning</mat-icon>
          <p>
            <strong>Warning:</strong> This action cannot be undone. 
            The non-selected companies will be permanently deleted after their applications are transferred.
          </p>
        </div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onMerge()" 
              [disabled]="!selectedCompanyId">
        <mat-icon>merge</mat-icon>
        Merge Companies
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

    .company-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 24px;
    }

    .company-option {
      padding: 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      transition: all 0.2s;
    }

    .company-option:hover {
      background: #f5f5f5;
    }

    .company-info {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-left: 8px;
    }

    .company-details {
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
export class CompanyMergeDialogComponent {
  selectedCompanyId: number | null = null;
  mergedNotes: string = '';

  constructor(
    public dialogRef: MatDialogRef<CompanyMergeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public companies: Company[]
  ) {
    // Pre-select the first company
    if (companies.length > 0 && companies[0].id) {
      this.selectedCompanyId = companies[0].id;
    }

    // Combine all notes
    this.mergedNotes = companies
      .filter(c => c.notes && c.notes.trim())
      .map(c => `=== ${c.name} ===\n${c.notes}`)
      .join('\n\n');
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onMerge(): void {
    if (!this.selectedCompanyId) {
      return;
    }

    const companyIds = this.companies
      .map(c => c.id!)
      .filter(id => id !== null && id !== undefined);

    this.dialogRef.close({
      targetCompanyId: this.selectedCompanyId,
      companyIds: companyIds,
      mergedNotes: this.mergedNotes
    });
  }
}
