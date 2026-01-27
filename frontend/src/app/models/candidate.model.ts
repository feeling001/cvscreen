export interface Candidate {
  id?: number;
  firstName: string;
  lastName: string;
  fullName?: string;
  globalNotes?: string;
  createdAt?: string;
  updatedAt?: string;
  applicationCount?: number;
  reviewCount?: number;
  applications?: ApplicationSummary[];
  reviews?: CandidateReview[];
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

export interface CandidateReview {
  id?: number;
  candidateId: number;
  userId: number;
  username: string;
  displayName?: string;
  comment: string;
  createdAt?: string;
}
