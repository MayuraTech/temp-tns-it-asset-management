import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Assignment Card Component
 * 
 * Displays user assignment information with avatar, contact details, and reassignment action.
 * Used in asset detail view to show current assignment status.
 * 
 * Features:
 * - User avatar with fallback initials
 * - Contact information display (name, email, phone, department)
 * - Reassign asset action button
 * - Responsive design with Editorial Geometry styling
 * - Accessible with proper ARIA labels and keyboard navigation
 */
@Component({
  selector: 'app-assignment-card',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatTooltipModule
  ],
  template: `
    <mat-card class="assignment-card" *ngIf="assignedUser; else unassignedTemplate">
      <mat-card-header class="card-header">
        <div class="avatar-container" mat-card-avatar>
          <div class="user-avatar" [style.background-color]="getAvatarColor(assignedUser)">
            <span class="avatar-initials">{{ getInitials(assignedUser) }}</span>
          </div>
        </div>
        
        <mat-card-title class="user-name">{{ assignedUser }}</mat-card-title>
        <mat-card-subtitle class="assignment-date" *ngIf="assignmentDate">
          Assigned {{ formatAssignmentDate(assignmentDate) }}
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content class="card-content">
        <!-- Contact Information -->
        <div class="contact-info">
          <div class="contact-item" *ngIf="assignedUserEmail">
            <mat-icon class="contact-icon">email</mat-icon>
            <span class="contact-text">{{ assignedUserEmail }}</span>
          </div>
          
          <div class="contact-item" *ngIf="phone">
            <mat-icon class="contact-icon">phone</mat-icon>
            <span class="contact-text">{{ phone }}</span>
          </div>
          
          <div class="contact-item" *ngIf="department">
            <mat-icon class="contact-icon">business</mat-icon>
            <span class="contact-text">{{ department }}</span>
          </div>
        </div>
      </mat-card-content>

      <mat-card-actions class="card-actions">
        <button 
          mat-stroked-button
          class="reassign-button"
          (click)="onReassignClick()"
          [disabled]="readOnly"
          matTooltip="Reassign this asset to another user"
          aria-label="Reassign asset">
          <mat-icon>swap_horiz</mat-icon>
          Reassign Asset
        </button>
      </mat-card-actions>
    </mat-card>

    <!-- Unassigned State Template -->
    <ng-template #unassignedTemplate>
      <mat-card class="assignment-card unassigned">
        <mat-card-content class="unassigned-content">
          <div class="unassigned-icon">
            <mat-icon>person_off</mat-icon>
          </div>
          <h3 class="unassigned-title">Unassigned Asset</h3>
          <p class="unassigned-description">This asset is not currently assigned to any user.</p>
        </mat-card-content>
        
        <mat-card-actions class="card-actions">
          <button 
            mat-flat-button
            color="primary"
            class="assign-button"
            (click)="onAssignClick()"
            [disabled]="readOnly"
            matTooltip="Assign this asset to a user"
            aria-label="Assign asset">
            <mat-icon>person_add</mat-icon>
            Assign Asset
          </button>
        </mat-card-actions>
      </mat-card>
    </ng-template>
  `,
  styles: [`
    .assignment-card {
      background: var(--surface-container-lowest, #ffffff);
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(20, 59, 125, 0.08);
      transition: all 0.3s ease;
      max-width: 400px;
    }

    .assignment-card:hover {
      box-shadow: 0 8px 24px rgba(20, 59, 125, 0.12);
      transform: translateY(-2px);
    }

    .card-header {
      padding: 20px 20px 0 20px;
    }

    .avatar-container {
      margin-right: 16px;
    }

    .user-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 600;
      font-size: 18px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }

    .avatar-initials {
      font-family: 'Manrope', sans-serif;
      font-weight: 700;
      text-transform: uppercase;
    }

    .user-name {
      font-family: 'Manrope', sans-serif;
      font-size: 18px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0;
    }

    .assignment-date {
      font-family: 'Inter', sans-serif;
      font-size: 12px;
      color: var(--on-surface-variant, #434750);
      margin: 4px 0 0 0;
    }

    .card-content {
      padding: 16px 20px;
    }

    .contact-info {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .contact-item {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .contact-icon {
      color: var(--primary, #143b7d);
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .contact-text {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      color: var(--on-surface, #1a1b20);
      word-break: break-word;
    }

    .card-actions {
      padding: 0 20px 20px 20px;
      margin: 0;
    }

    .reassign-button {
      width: 100%;
      height: 40px;
      color: var(--primary, #143b7d);
      border-color: var(--outline-variant, #c4c6d2);
      font-family: 'Inter', sans-serif;
      font-weight: 600;
    }

    .reassign-button:hover {
      background: rgba(20, 59, 125, 0.04);
    }

    /* Unassigned State Styles */
    .unassigned {
      text-align: center;
    }

    .unassigned-content {
      padding: 32px 20px 16px 20px;
    }

    .unassigned-icon {
      margin-bottom: 16px;
    }

    .unassigned-icon mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: var(--on-surface-variant, #434750);
      opacity: 0.6;
    }

    .unassigned-title {
      font-family: 'Manrope', sans-serif;
      font-size: 18px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0 0 8px 0;
    }

    .unassigned-description {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      color: var(--on-surface-variant, #434750);
      margin: 0;
      line-height: 1.4;
    }

    .assign-button {
      width: 100%;
      height: 40px;
      font-family: 'Inter', sans-serif;
      font-weight: 600;
    }

    /* Responsive Design */
    @media (max-width: 480px) {
      .assignment-card {
        max-width: 100%;
      }

      .card-header {
        padding: 16px 16px 0 16px;
      }

      .card-content {
        padding: 12px 16px;
      }

      .card-actions {
        padding: 0 16px 16px 16px;
      }

      .user-avatar {
        width: 40px;
        height: 40px;
        font-size: 16px;
      }

      .user-name {
        font-size: 16px;
      }
    }

    /* High Contrast Mode */
    @media (prefers-contrast: high) {
      .assignment-card {
        border: 2px solid var(--outline, #747782);
      }

      .user-avatar {
        border: 2px solid white;
      }
    }

    /* Reduced Motion */
    @media (prefers-reduced-motion: reduce) {
      .assignment-card {
        transition: none;
      }

      .assignment-card:hover {
        transform: none;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssignmentCardComponent {
  @Input() assignedUser?: string;
  @Input() assignedUserEmail?: string;
  @Input() assignmentDate?: string;
  @Input() phone?: string;
  @Input() department?: string;
  @Input() readOnly: boolean = false;

  @Output() reassignClick = new EventEmitter<void>();
  @Output() assignClick = new EventEmitter<void>();

  /**
   * Handle reassign button click
   */
  onReassignClick(): void {
    this.reassignClick.emit();
  }

  /**
   * Handle assign button click
   */
  onAssignClick(): void {
    this.assignClick.emit();
  }

  /**
   * Get user initials for avatar
   */
  getInitials(name: string): string {
    if (!name || !name.trim()) return '?';
    
    const parts = name.trim().split(' ').filter(part => part.length > 0);
    if (parts.length === 0) return '?';
    if (parts.length === 1) {
      return parts[0].charAt(0).toUpperCase();
    }
    
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }

  /**
   * Generate consistent avatar color based on name
   */
  getAvatarColor(name: string): string {
    if (!name) return '#6c757d';
    
    const colors = [
      '#143b7d', // Primary
      '#315396', // Primary container
      '#a9371d', // Secondary
      '#80002b', // Tertiary
      '#2e7d32', // Green
      '#1976d2', // Blue
      '#7b1fa2', // Purple
      '#c2185b', // Pink
      '#f57c00', // Orange
      '#5d4037'  // Brown
    ];
    
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    
    return colors[Math.abs(hash) % colors.length];
  }

  /**
   * Format assignment date for display
   */
  formatAssignmentDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      return 'today';
    } else if (diffDays === 1) {
      return 'yesterday';
    } else if (diffDays < 7) {
      return `${diffDays} days ago`;
    } else if (diffDays < 30) {
      const weeks = Math.floor(diffDays / 7);
      return `${weeks} week${weeks > 1 ? 's' : ''} ago`;
    } else if (diffDays < 365) {
      const months = Math.floor(diffDays / 30);
      return `${months} month${months > 1 ? 's' : ''} ago`;
    } else {
      const years = Math.floor(diffDays / 365);
      return `${years} year${years > 1 ? 's' : ''} ago`;
    }
  }
}