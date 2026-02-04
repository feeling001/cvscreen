export interface Candidate {
  id?: number;
  firstName: string;
  lastName: string;
  fullName?: string;
  contractType?: string; // "Subcontractor" or "Freelancer"
  globalNotes?: string;
  createdAt?: string;
  updatedAt?: string;
  applicationCount?: number;
  reviewCount?: number; // Total number of reviews across all applications
  averageRating?: number; // Average rating across all applications
  applications?: ApplicationSummary[];
  allComments?: ApplicationComment[]; // All comments across all applications
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

export interface ApplicationComment {
  id?: number;
  applicationId: number;
  userId: number;
  username: string;
  displayName?: string;
  comment: string;
  rating?: number;
  createdAt?: string;
  jobReference?: string;
  roleCategory?: string;
  currentApplication?: boolean;
}
