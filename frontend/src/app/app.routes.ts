import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { CandidatesComponent } from './components/candidates/candidates.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'candidates', pathMatch: 'full' },
      { path: 'candidates', component: CandidatesComponent },
      { 
        path: 'jobs', 
        loadComponent: () => import('./components/jobs/jobs.component').then(m => m.JobsComponent) 
      },
      { 
        path: 'applications', 
        loadComponent: () => import('./components/applications/applications.component').then(m => m.ApplicationsComponent) 
      },
      { 
        path: 'companies', 
        loadComponent: () => import('./components/companies/companies.component').then(m => m.CompaniesComponent) 
      },
      { 
        path: 'users', 
        loadComponent: () => import('./components/users/users.component').then(m => m.UsersComponent)
      },
      { 
        path: 'import-prounity', 
        loadComponent: () => import('./components/prounity-import/prounity-import.component').then(m => m.ProunityImportComponent)
      }
    ]
  },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];
