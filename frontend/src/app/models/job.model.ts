export interface Job {
  id?: number;
  reference: string;
  title: string;
  category: string;
  publicationDate?: string;
  status: string;
  source?: string;
  description?: string;
  applicationCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export enum JobStatus {
  OPEN = 'OPEN',
  CLOSED = 'CLOSED',
  ON_HOLD = 'ON_HOLD'
}
