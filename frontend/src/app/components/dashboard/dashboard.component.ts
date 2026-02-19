import { Component, OnInit } from '@angular/core';

import { Router, RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';
import { InfoService, AppInfo } from '../../services/info.service';

@Component({
    selector: 'app-dashboard',
    imports: [
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule
],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  currentUser: any;
  appInfo: AppInfo = { version: '...', environment: '' };

  constructor(
    private authService: AuthService,
    private router: Router,
    private infoService: InfoService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.infoService.getInfo().subscribe({
      next: (info) => this.appInfo = info,
      error: () => {} // silencieux si indispo
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
