import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatExpansionModule } from '@angular/material/expansion';
import { environment } from '../../../environments/environment';

interface ImportError {
  lineNumber: number;
  message: string;
}

interface ImportResult {
  success: boolean;
  successCount: number;
  failedCount: number;
  totalProcessed: number;
  message: string;
  errors?: ImportError[];
}

@Component({
  selector: 'app-simple-csv-import',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    MatSnackBarModule,
    MatExpansionModule
  ],
  template: `
    <div class="import-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>upload_file</mat-icon>
            Import Applications from CSV (Simple Format)
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Template Information -->
          <mat-expansion-panel class="template-info">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <mat-icon>info</mat-icon>
                CSV Template Information
              </mat-panel-title>
            </mat-expansion-panel-header>

            <div class="template-content">
              <h4>Expected Format:</h4>
              <p><strong>Separator:</strong> Semicolon (;)</p>
              <p><strong>Encoding:</strong> UTF-8 or ISO-8859-1</p>

              <h4>Columns (in order):</h4>
              <ol>
                <li><strong>application_date</strong> - Application date (dd/MM/yyyy) - <em>Required</em></li>
                <li><strong>job_reference</strong> - Job reference (e.g., I09526) - <em>Optional</em></li>
                <li><strong>role_category</strong> - Role/Category - <em>Required</em></li>
                <li><strong>candidate_name</strong> - Candidate last name - <em>Required</em></li>
                <li><strong>helper</strong> - Helper field (ignored) - <em>Optional</em></li>
                <li><strong>candidate_first_name</strong> - Candidate first name - <em>Required</em></li>
                <li><strong>company_name</strong> - Consulting company name - <em>Optional</em></li>
                <li><strong>daily_rate</strong> - Daily rate in euros - <em>Optional</em></li>
                <li><strong>evaluation_notes</strong> - Evaluation notes - <em>Optional</em></li>
                <li><strong>conclusion</strong> - Conclusion (0/NOK=rejected, 1/OK=approved) - <em>Optional</em></li>
              </ol>

              <h4>Example:</h4>
              <div class="code-block">
                application_date;job_reference;role_category;candidate_name;helper;candidate_first_name;company_name;daily_rate;evaluation_notes;conclusion<br>
                19/01/2025;I09526;Full Stack Senior Dev;De Roose;p;Antoine;Extia;595;Philippe Massaert Ne semble pas senior;0<br>
                19/01/2025;I09526;Full Stack Senior Dev;D'Hondt;p;Joachim;;680;Philippe Massaert Job Hopper - NOK;NOK
              </div>

              <button mat-raised-button color="primary" (click)="downloadTemplate()">
                <mat-icon>download</mat-icon>
                Download Sample CSV
              </button>
            </div>
          </mat-expansion-panel>

          <!-- File Upload -->
          <div class="upload-section">
            <input
              type="file"
              #fileInput
              accept=".csv"
              (change)="onFileSelected($event)"
              style="display: none"
            />

            <button
              mat-raised-button
              color="primary"
              (click)="fileInput.click()"
              [disabled]="uploading"
            >
              <mat-icon>folder_open</mat-icon>
              Select CSV File
            </button>

            <div *ngIf="selectedFile" class="file-info">
              <mat-icon>description</mat-icon>
              <span>{{ selectedFile.name }}</span>
              <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
            </div>

            <button
              mat-raised-button
              color="accent"
              (click)="uploadFile()"
              [disabled]="!selectedFile || uploading"
            >
              <mat-icon>cloud_upload</mat-icon>
              Upload and Import
            </button>
          </div>

          <!-- Progress Bar -->
          <mat-progress-bar
            *ngIf="uploading"
            mode="indeterminate"
            class="progress-bar"
          ></mat-progress-bar>

          <!-- Import Results -->
          <div *ngIf="importResult" class="results-section">
            <mat-card [class.success-card]="importResult.success && !importResult.errors" 
                      [class.warning-card]="importResult.success && importResult.errors"
                      [class.error-card]="!importResult.success">
              <mat-card-header>
                <mat-icon *ngIf="importResult.success && !importResult.errors">check_circle</mat-icon>
                <mat-icon *ngIf="importResult.success && importResult.errors">warning</mat-icon>
                <mat-icon *ngIf="!importResult.success">error</mat-icon>
                <mat-card-title>{{ importResult.message }}</mat-card-title>
              </mat-card-header>

              <mat-card-content>
                <div class="stats">
                  <div class="stat">
                    <span class="stat-label">Total Processed:</span>
                    <span class="stat-value">{{ importResult.totalProcessed }}</span>
                  </div>
                  <div class="stat success">
                    <span class="stat-label">Successful:</span>
                    <span class="stat-value">{{ importResult.successCount }}</span>
                  </div>
                  <div class="stat error" *ngIf="importResult.failedCount > 0">
                    <span class="stat-label">Failed:</span>
                    <span class="stat-value">{{ importResult.failedCount }}</span>
                  </div>
                </div>

                <!-- Error Details -->
                <div *ngIf="importResult.errors && importResult.errors.length > 0" class="errors-section">
                  <h4>Import Errors:</h4>
                  <div class="errors-table-container">
                    <table mat-table [dataSource]="importResult.errors" class="errors-table">
                      <ng-container matColumnDef="lineNumber">
                        <th mat-header-cell *matHeaderCellDef>Line Number</th>
                        <td mat-cell *matCellDef="let error">{{ error.lineNumber }}</td>
                      </ng-container>

                      <ng-container matColumnDef="message">
                        <th mat-header-cell *matHeaderCellDef>Error Message</th>
                        <td mat-cell *matCellDef="let error">{{ error.message }}</td>
                      </ng-container>

                      <tr mat-header-row *matHeaderRowDef="errorColumns"></tr>
                      <tr mat-row *matRowDef="let row; columns: errorColumns;"></tr>
                    </table>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .import-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    mat-card-header {
      margin-bottom: 20px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .template-info {
      margin-bottom: 24px;
    }

    .template-content {
      padding: 16px;
    }

    .template-content h4 {
      margin-top: 16px;
      margin-bottom: 8px;
      color: #424242;
    }

    .template-content ol {
      margin-left: 20px;
    }

    .template-content li {
      margin-bottom: 4px;
    }

    .code-block {
      background: #f5f5f5;
      padding: 12px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 11px;
      overflow-x: auto;
      margin: 12px 0;
      border: 1px solid #ddd;
      line-height: 1.6;
    }

    .upload-section {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin: 24px 0;
    }

    .file-info {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 4px;
    }

    .file-size {
      color: #666;
      font-size: 14px;
    }

    .progress-bar {
      margin: 16px 0;
    }

    .results-section {
      margin-top: 24px;
    }

    .success-card {
      border-left: 4px solid #4caf50;
    }

    .warning-card {
      border-left: 4px solid #ff9800;
    }

    .error-card {
      border-left: 4px solid #f44336;
    }

    .stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin: 16px 0;
    }

    .stat {
      display: flex;
      flex-direction: column;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 4px;
    }

    .stat.success {
      background: #e8f5e9;
    }

    .stat.error {
      background: #ffebee;
    }

    .stat-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 4px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: 500;
    }

    .errors-section {
      margin-top: 24px;
    }

    .errors-section h4 {
      margin-bottom: 12px;
      color: #f44336;
    }

    .errors-table-container {
      max-height: 400px;
      overflow-y: auto;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .errors-table {
      width: 100%;
    }

    .errors-table td {
      padding: 12px;
    }

    mat-expansion-panel {
      box-shadow: none;
      border: 1px solid #ddd;
    }
  `]
})
export class SimpleCsvImportComponent {
  selectedFile: File | null = null;
  uploading = false;
  importResult: ImportResult | null = null;
  errorColumns = ['lineNumber', 'message'];

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.importResult = null;
    }
  }

  uploadFile(): void {
    if (!this.selectedFile) {
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.uploading = true;
    this.importResult = null;

    this.http.post<ImportResult>(
      `${environment.apiUrl}/import/simple-csv`,
      formData
    ).subscribe({
      next: (result) => {
        this.uploading = false;
        this.importResult = result;
        
        if (result.success && !result.errors) {
          this.snackBar.open(result.message, 'Close', { 
            duration: 5000,
            panelClass: ['success-snackbar']
          });
        } else if (result.success && result.errors) {
          this.snackBar.open(result.message, 'Close', { 
            duration: 7000,
            panelClass: ['warning-snackbar']
          });
        } else {
          this.snackBar.open(result.message, 'Close', { 
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      },
      error: (error) => {
        this.uploading = false;
        this.snackBar.open('Upload failed: ' + error.message, 'Close', { 
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }

  downloadTemplate(): void {
    const template = `application_date;job_reference;role_category;candidate_name;helper;candidate_first_name;company_name;daily_rate;evaluation_notes;conclusion
19/01/2025;I09526;Full Stack Senior Dev;De Roose;p;Antoine;Extia;595;Philippe Massaert Ne semble pas senior;0
19/01/2025;I09526;Full Stack Senior Dev;D'Hondt;p;Joachim;;680;Philippe Massaert Job Hopper - NOK;NOK
20/01/2025;I09527;Java Developer;Martin;x;Sophie;Accenture;620;Good technical skills;1
21/01/2025;;Project Manager;Bernard;;Pierre;Sopra Steria;550;Experienced PM;OK`;

    const blob = new Blob([template], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'simple-import-template.csv';
    link.click();
  }
}
