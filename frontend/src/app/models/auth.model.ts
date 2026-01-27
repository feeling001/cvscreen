export interface LoginRequest {
  username: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  username: string;
  displayName: string;
}
