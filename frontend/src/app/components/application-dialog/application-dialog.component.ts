import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { Application, ApplicationStatus } from '../../models/application.model';
import { CandidateService } from '../../services/candidate.service';
import { JobService } from '../../services/job.service';
import { CompanyService } from '../../services/company.service';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

@Component({
    selector: 'app-application-dialog',
    imports: [
        CommonModule,
        FormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatSelectModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatAutocompleteModule
    ],
    template: `
    <h2 mat-dialog-title>{{ data ? 'Edit Application' : 'New Application' }}</h2>
    <mat-dialog-content>
      <div class="form-container">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Candidate</mat-label>
          <mat-select [(ngModel)]="application.candidateId" required>
            <mat-option *ngFor="let candidate of candidates" [value]="candidate.id">
              {{ candidate.firstName }} {{ candidate.lastName }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Job Reference (optional)</mat-label>
          <mat-select [(ngModel)]="application.jobId">
            <mat-option [value]="null">None (Spontaneous)</mat-option>
            <mat-option *ngFor="let job of jobs" [value]="job.id">
              {{ job.reference }} - {{ job.title }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Role Category</mat-label>
          <input matInput [(ngModel)]="application.roleCategory" required 
                 placeholder="e.g. System Architect, Developer">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Company (optional)</mat-label>
          <mat-select [(ngModel)]="application.companyId">
            <mat-option [value]="null">None</mat-option>
            <mat-option *ngFor="let company of companies" [value]="company.id">
              {{ company.name }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Daily Rate (€)</mat-label>
          <input matInput type="number" [(ngModel)]="application.dailyRate" 
                 placeholder="e.g. 650">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Application Date</mat-label>
          <input matInput [matDatepicker]="picker" [(ngModel)]="application.applicationDate" required>
          <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
          <mat-datepicker #picker></mat-datepicker>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Status</mat-label>
          <mat-select [(ngModel)]="application.status">
            <mat-option value="CV_RECEIVED">CV Received</mat-option>
            <mat-option value="CV_REVIEWED">CV Reviewed</mat-option>
            <mat-option value="REMOTE_INTERVIEW">Remote Interview</mat-option>
            <mat-option value="ONSITE_INTERVIEW">Onsite Interview</mat-option>
            <mat-option value="APPROVED_FOR_MISSION">Approved for Mission</mat-option>
            <mat-option value="REJECTED">Rejected</mat-option>
            <mat-option value="ON_HOLD">On Hold</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Conclusion</mat-label>
          <textarea matInput [(ngModel)]="application.conclusion" rows="3"
                    placeholder="e.g. Good profile, Maybe, Too junior"></textarea>
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onSave()" 
              [disabled]="!application.candidateId || !application.roleCategory || !application.applicationDate">
        Save
      </button>
    </mat-dialog-actions>
  `,
    styles: [`
    .form-container {
      display: flex;
      flex-direction: column;
      gap: 16px;
      min-width: 500px;
      padding: 20px 0;
      max-height: 70vh;
      overflow-y: auto;
    }

    .full-width {
      width: 100%;
    }
  `]
})
export class ApplicationDialogComponent implements OnInit {
  application: any;
  candidates: any[] = [];
  jobs: any[] = [];
  companies: any[] = [];

  constructor(
    public dialogRef: MatDialogRef<ApplicationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Application | null,
    private candidateService: CandidateService,
    private jobService: JobService,
    private companyService: CompanyService
  ) {
    this.application = data ? { ...data } : {
      candidateId: null,
      jobId: null,
      roleCategory: '',
      companyId: null,
      dailyRate: null,
      applicationDate: new Date().toISOString().split('T')[0],
      status: 'CV_RECEIVED',
      conclusion: ''
    };
  }

  ngOnInit(): void {
    this.loadCandidates();
    this.loadJobs();
    this.loadCompanies();
  }

  loadCandidates(): void {
    this.candidateService.getAllCandidatesSimple().subscribe({
      next: (data) => {
        this.candidates = data;
      },
      error: (error) => console.error('Failed to load candidates', error)
    });
  }

  loadJobs(): void {
    this.jobService.getAllJobs().subscribe({
      next: (data) => {
        this.jobs = data;
      },
      error: (error) => console.error('Failed to load jobs', error)
    });
  }

  loadCompanies(): void {
    // FIX: Gérer la réponse paginée
    this.companyService.getAllCompanies(0, 1000).subscribe({
      next: (response) => {
        this.companies = response.companies || [];
      },
      error: (error) => console.error('Failed to load companies', error)
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.application.candidateId && this.application.roleCategory && this.application.applicationDate) {
      this.dialogRef.close(this.application);
    }
  }
}
