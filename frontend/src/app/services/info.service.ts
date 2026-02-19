// frontend/src/app/services/info.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AppInfo {
  version: string;
  environment: string;
}

@Injectable({ providedIn: 'root' })
export class InfoService {
  constructor(private http: HttpClient) {}

  getInfo(): Observable<AppInfo> {
    return this.http.get<AppInfo>(`${environment.apiUrl}/info`);
  }
}