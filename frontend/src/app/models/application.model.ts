export interface Application {
  id?: number;
  candidateId: number;
  candidateName?: string;
  jobId?: number;
  jobReference?: string;
  jobTitle?: string;
  roleCategory: string;
  companyId?: number;
  companyName?: string;
  dailyRate?: number;
  applicationDate: string;
  status: string;
  conclusion?: string;
  evaluationNotes?: string;
  cvFilePath?: string;
  hasReviews?: boolean;
  commentCount?: number;
  averageRating?: number; // Average rating from comments (1-5 stars)
  createdAt?: string;
  updatedAt?: string;
}

export enum ApplicationStatus {
  CV_RECEIVED = 'CV_RECEIVED',
  CV_REVIEWED = 'CV_REVIEWED',
  REMOTE_INTERVIEW = 'REMOTE_INTERVIEW',
  ONSITE_INTERVIEW = 'ONSITE_INTERVIEW',
  APPROVED_FOR_MISSION = 'APPROVED_FOR_MISSION',
  REJECTED = 'REJECTED',
  ON_HOLD = 'ON_HOLD'
}
