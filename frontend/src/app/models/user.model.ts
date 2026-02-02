export interface User {
  id?: number;
  username: string;
  displayName: string;
  enabled: boolean;
  createdAt?: string;
  commentCount?: number;
}

export interface CreateUserRequest {
  username: string;
  displayName: string;
  password: string;
  enabled?: boolean;
}

export interface UpdateUserRequest {
  displayName?: string;
  password?: string;
  enabled?: boolean;
}
