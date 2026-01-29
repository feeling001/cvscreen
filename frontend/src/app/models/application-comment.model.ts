export interface ApplicationComment {
  id?: number;
  applicationId: number;
  userId: number;
  username: string;
  displayName?: string;
  comment: string;
  createdAt?: string;
}
