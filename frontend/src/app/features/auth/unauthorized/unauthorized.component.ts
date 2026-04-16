import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/**
 * Unauthorized Component
 * 
 * Displayed when a user attempts to access a route they don't have permission for.
 * Provides options to navigate back or return to dashboard.
 */
@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="unauthorized-container">
      <mat-card class="unauthorized-card">
        <mat-card-header>
          <div class="icon-container">
            <mat-icon class="error-icon">block</mat-icon>
          </div>
        </mat-card-header>
        
        <mat-card-content>
          <h1 class="title">Access Denied</h1>
          <p class="message">
            You do not have permission to access this page.
          </p>
          <p class="sub-message">
            If you believe this is an error, please contact your system administrator.
          </p>
        </mat-card-content>

        <mat-card-actions>
          <button 
            mat-raised-button 
            color="primary" 
            (click)="goToDashboard()"
            class="action-button"
          >
            <mat-icon>home</mat-icon>
            Go to Dashboard
          </button>
          <button 
            mat-button 
            (click)="goBack()"
            class="action-button"
          >
            <mat-icon>arrow_back</mat-icon>
            Go Back
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .unauthorized-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: var(--surface);
      padding: 24px;
    }

    .unauthorized-card {
      width: 100%;
      max-width: 500px;
      background: var(--surface-container-lowest);
      box-shadow: 0 20px 40px rgba(20, 59, 125, 0.06);
      text-align: center;
    }

    .icon-container {
      display: flex;
      justify-content: center;
      margin: 24px 0;
    }

    .error-icon {
      font-size: 80px;
      width: 80px;
      height: 80px;
      color: var(--secondary);
    }

    .title {
      font-family: var(--font-heading);
      font-size: 30px;
      color: var(--on-surface);
      margin: 16px 0;
      letter-spacing: -0.75px;
    }

    .message {
      font-family: var(--font-body);
      font-size: 16px;
      color: var(--on-surface);
      margin: 16px 0;
    }

    .sub-message {
      font-family: var(--font-body);
      font-size: 14px;
      color: var(--on-surface-variant);
      margin: 8px 0 24px;
    }

    mat-card-actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
      padding: 24px;
    }

    .action-button {
      width: 100%;
      height: 48px;
      font-family: var(--font-body);
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }

    .action-button mat-icon {
      margin-right: 8px;
    }
  `]
})
export class UnauthorizedComponent {
  constructor(private router: Router) {}

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goBack(): void {
    window.history.back();
  }
}
