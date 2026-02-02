import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { UserDialogComponent } from '../user-dialog/user-dialog.component';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    MatChipsModule
  ],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  currentUser: any;
  isAdmin = false;
  displayedColumns: string[] = ['username', 'displayName', 'enabled', 'commentCount', 'createdAt', 'actions'];

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.isAdmin = this.currentUser?.username === 'admin';
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load users', 'Close', { duration: 3000 });
      }
    });
  }

  openCreateDialog(): void {
    if (!this.isAdmin) {
      this.snackBar.open('Only admin can create users', 'Close', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: '500px',
      data: { user: null, isAdmin: this.isAdmin, currentUserId: this.currentUser?.id }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.createUser(result);
      }
    });
  }

  openEditDialog(user: User): void {
    const canEdit = this.isAdmin || user.id === this.currentUser?.id;
    
    if (!canEdit) {
      this.snackBar.open('You can only edit your own profile', 'Close', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(UserDialogComponent, {
      width: '500px',
      data: { user, isAdmin: this.isAdmin, currentUserId: this.currentUser?.id }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateUser(user.id!, result);
      }
    });
  }

  createUser(userData: any): void {
    this.userService.createUser(userData).subscribe({
      next: () => {
        this.snackBar.open('User created successfully', 'Close', { duration: 3000 });
        this.loadUsers();
      },
      error: (error) => {
        const message = error.error?.message || 'Failed to create user';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  updateUser(id: number, userData: any): void {
    this.userService.updateUser(id, userData).subscribe({
      next: () => {
        this.snackBar.open('User updated successfully', 'Close', { duration: 3000 });
        this.loadUsers();
      },
      error: (error) => {
        const message = error.error?.message || 'Failed to update user';
        this.snackBar.open(message, 'Close', { duration: 3000 });
      }
    });
  }

  deleteUser(id: number, username: string): void {
    if (!this.isAdmin) {
      this.snackBar.open('Only admin can delete users', 'Close', { duration: 3000 });
      return;
    }

    if (username === 'admin') {
      this.snackBar.open('Cannot delete admin user', 'Close', { duration: 3000 });
      return;
    }

    if (confirm(`Are you sure you want to delete user ${username}?`)) {
      this.userService.deleteUser(id).subscribe({
        next: () => {
          this.snackBar.open('User deleted', 'Close', { duration: 3000 });
          this.loadUsers();
        },
        error: (error) => {
          const message = error.error?.message || 'Failed to delete user';
          this.snackBar.open(message, 'Close', { duration: 3000 });
        }
      });
    }
  }

  canEdit(user: User): boolean {
    return this.isAdmin || user.id === this.currentUser?.id;
  }

  canDelete(user: User): boolean {
    return this.isAdmin && user.username !== 'admin';
  }
}
