export interface Candidate {
  id?: number;
  firstName: string;
  lastName: string;
  fullName?: string;
  globalNotes?: string;
  createdAt?: string;
  updatedAt?: string;
  applicationCount?: number;
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
