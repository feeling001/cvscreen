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
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { environment } from '../../../environments/environment';

interface ImportError {
  lineNumber: number;
  message: string;
}

interface ImportResult {
  success: boolean;
  successCount: number;
  skippedCount: number;
  failedCount: number;
  totalProcessed: number;
  message: string;
  errors?: ImportError[];
}

@Component({
  selector: 'app-prounity-import',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatTableModule,
    MatSnackBarModule,
    MatExpansionModule,
    MatListModule,
    MatDividerModule
  ],
  template: `
    <div class="import-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>cloud_download</mat-icon>
            Import Candidates from Pro-Unity
          </mat-card-title>
        </mat-card-header>

        <mat-card-content>
          <!-- Instructions -->
          <mat-expansion-panel class="instructions-panel">
            <mat-expansion-panel-header>
              <mat-panel-title>
                <mat-icon>help_outline</mat-icon>
                How to export from Pro-Unity
              </mat-panel-title>
            </mat-expansion-panel-header>

            <div class="instructions-content">
              <h4>Step-by-step guide:</h4>
              <ol>
                <li>Open Pro-Unity and navigate to the job post you want to import</li>
                <li>Open your browser's Developer Console:
                  <ul>
                    <li><strong>Chrome/Edge:</strong> Press F12 or Ctrl+Shift+I</li>
                    <li><strong>Firefox:</strong> Press F12 or Ctrl+Shift+K</li>
                  </ul>
                </li>
                <li>In the Console tab, type the following command and press Enter:<br>
                  <code class="code-block">copy(JSON.stringify(jobPost, null, 2))</code>
                </li>
                <li>The job data is now copied to your clipboard</li>
                <li>Create a new file (e.g., <code>job-export.json</code>) and paste the content</li>
                <li>Upload the file using the button below</li>
              </ol>

              <h4>Important Notes:</h4>
              <mat-list>
                <mat-list-item>
                  <mat-icon matListItemIcon>check_circle</mat-icon>
                  <div matListItemTitle>Duplicate Detection</div>
                  <div matListItemLine>Re-importing the same file will automatically skip duplicates</div>
                </mat-list-item>
                <mat-list-item>
                  <mat-icon matListItemIcon>auto_awesome</mat-icon>
                  <div matListItemTitle>Auto-creation</div>
                  <div matListItemLine>Candidates, jobs, and companies are created automatically</div>
                </mat-list-item>
                <mat-list-item>
                  <mat-icon matListItemIcon>sync</mat-icon>
                  <div matListItemTitle>Status Mapping</div>
                  <div matListItemLine>Pro-Unity statuses are automatically mapped to CVScreen statuses</div>
                </mat-list-item>
              </mat-list>
            </div>
          </mat-expansion-panel>

          <!-- File Upload -->
          <div class="upload-section">
            <input
              type="file"
              #fileInput
              accept=".json"
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
              Select JSON File
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
              Import from Pro-Unity
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
                    <span class="stat-label">New Imports:</span>
                    <span class="stat-value">{{ importResult.successCount }}</span>
                  </div>
                  <div class="stat info" *ngIf="importResult.skippedCount > 0">
                    <span class="stat-label">Duplicates Skipped:</span>
                    <span class="stat-value">{{ importResult.skippedCount }}</span>
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
                        <th mat-header-cell *matHeaderCellDef>Line</th>
                        <td mat-cell *matCellDef="let error">{{ error.lineNumber || 'N/A' }}</td>
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

    .instructions-panel {
      margin-bottom: 24px;
      box-shadow: none;
      border: 1px solid #ddd;
    }

    .instructions-content {
      padding: 16px;
    }

    .instructions-content h4 {
      margin-top: 16px;
      margin-bottom: 12px;
      color: #424242;
    }

    .instructions-content ol {
      margin-left: 20px;
      line-height: 1.8;
    }

    .instructions-content ol li {
      margin-bottom: 12px;
    }

    .instructions-content ul {
      margin-left: 20px;
      margin-top: 8px;
    }

    .code-block {
      background: #f5f5f5;
      padding: 8px 12px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
      display: inline-block;
      margin: 4px 0;
      border: 1px solid #ddd;
    }

    code {
      background: #f5f5f5;
      padding: 2px 6px;
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }

    mat-list-item {
      height: auto !important;
      padding: 12px 0 !important;
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

    .stat.info {
      background: #e3f2fd;
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
  `]
})
export class ProunityImportComponent {
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
      `${environment.apiUrl}/import/prounity`,
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
}
