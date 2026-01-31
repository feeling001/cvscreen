export interface ApplicationComment {
  id?: number;
  applicationId: number;
  userId: number;
  username: string;
  displayName?: string;
  comment: string;
  rating?: number; // Rating from 1 to 5 stars
  createdAt?: string;
  jobReference?: string;
  roleCategory?: string;
  currentApplication?: boolean; // Changed from isCurrentApplication to currentApplication for consistency
}
