import { Component } from '@angular/core';

import { HttpClient, HttpEventType } from '@angular/common/http';
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
    imports: [
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
    templateUrl: './prounity-import.component.html',
    styleUrls: ['./prounity-import.component.css']
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
      
      if (this.selectedFile.size > 20000000) {
        this.snackBar.open('File exceeds 20MB limit!', 'Close', { duration: 5000 });
      }
    }
  }

  clearFile(): void {
    this.selectedFile = null;
    this.importResult = null;
  }

  uploadFile(): void {
    if (!this.selectedFile || this.selectedFile.size > 20000000) {
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
        
        if (result.success) {
          this.snackBar.open(result.message, 'Close', { duration: 5000 });
        } else {
          this.snackBar.open(result.message, 'Close', { duration: 5000 });
        }
      },
      error: (error) => {
        this.uploading = false;
        this.snackBar.open('Upload failed: ' + (error.error?.message || error.message), 'Close', { duration: 5000 });
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
