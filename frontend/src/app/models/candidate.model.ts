export interface Candidate {
  id?: number;
  firstName: string;
  lastName: string;
  fullName?: string;
  globalNotes?: string;
  createdAt?: string;
  updatedAt?: string;
  applicationCount?: number;
  reviewCount?: number; // Total number of reviews across all applications
  averageRating?: number; // Average rating across all applications
  applications?: ApplicationSummary[];
}

export interface ApplicationSummary {
  id: number;
  jobReference?: string;
  jobTitle?: string;
  roleCategory: string;
  companyName?: string;
  dailyRate?: number;
  applicationDate: string;
  status: string;
  conclusion?: string;
}
