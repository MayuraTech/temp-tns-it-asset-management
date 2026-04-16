import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LifecycleStatus } from '../../../models';

/**
 * Interface for lifecycle history events
 */
export interface LifecycleEvent {
  status: LifecycleStatus;
  date: string;
  description: string;
  icon: string;
  user?: string;
}

/**
 * Lifecycle Timeline Component
 * 
 * Displays chronological history of asset lifecycle events in a vertical timeline.
 * Used in asset detail view to show status changes and important events.
 * 
 * Features:
 * - Vertical timeline with status icons
 * - Chronological event display with dates and descriptions
 * - User attribution for changes
 * - Responsive design with Editorial Geometry styling
 * - Accessible with proper ARIA labels and semantic markup
 */
@Component({
  selector: 'app-lifecycle-timeline',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatTooltipModule
  ],
  template: `
    <div class="timeline-container" *ngIf="events && events.length > 0; else emptyTemplate">
      <h3 class="timeline-title">Lifecycle History</h3>
      
      <div class="timeline" role="list" aria-label="Asset lifecycle history">
        <div 
          class="timeline-item"
          *ngFor="let event of events; let first = first; let last = last"
          [class.first-item]="first"
          [class.last-item]="last"
          role="listitem">
          
          <!-- Timeline Line -->
          <div class="timeline-line" *ngIf="!last"></div>
          
          <!-- Event Icon -->
          <div class="timeline-icon" [class]="getStatusClass(event.status)">
            <mat-icon 
              [matTooltip]="getStatusTooltip(event.status)"
              [attr.aria-label]="getStatusTooltip(event.status)">
              {{ event.icon }}
            </mat-icon>
          </div>
          
          <!-- Event Content -->
          <div class="timeline-content">
            <div class="event-header">
              <h4 class="event-title">{{ event.description }}</h4>
              <time class="event-date" [attr.datetime]="event.date">
                {{ formatEventDate(event.date) }}
              </time>
            </div>
            
            <div class="event-details" *ngIf="event.user">
              <span class="event-user">
                <mat-icon class="user-icon">person</mat-icon>
                {{ event.user }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State Template -->
    <ng-template #emptyTemplate>
      <div class="empty-timeline">
        <div class="empty-icon">
          <mat-icon>timeline</mat-icon>
        </div>
        <h3 class="empty-title">No History Available</h3>
        <p class="empty-description">
          Lifecycle events will appear here as the asset progresses through its lifecycle.
        </p>
      </div>
    </ng-template>
  `,
  styles: [`
    .timeline-container {
      background: var(--surface-container-lowest, #ffffff);
      border-radius: 8px;
      padding: 24px;
      box-shadow: 0 4px 12px rgba(20, 59, 125, 0.08);
    }

    .timeline-title {
      font-family: 'Manrope', sans-serif;
      font-size: 18px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0 0 24px 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .timeline-title::before {
      content: '';
      width: 4px;
      height: 20px;
      background: var(--primary, #143b7d);
      border-radius: 2px;
    }

    .timeline {
      position: relative;
    }

    .timeline-item {
      position: relative;
      display: flex;
      align-items: flex-start;
      gap: 16px;
      padding-bottom: 24px;
    }

    .timeline-item.last-item {
      padding-bottom: 0;
    }

    .timeline-line {
      position: absolute;
      left: 20px;
      top: 40px;
      bottom: -24px;
      width: 2px;
      background: var(--outline-variant, #c4c6d2);
      opacity: 0.5;
    }

    .timeline-icon {
      position: relative;
      z-index: 1;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba(20, 59, 125, 0.15);
      flex-shrink: 0;
    }

    .timeline-icon mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      color: white;
    }

    /* Status-specific icon colors */
    .status-ordered {
      background: #ffc107;
    }

    .status-received {
      background: #0d6efd;
    }

    .status-deployed {
      background: #198754;
    }

    .status-in-use {
      background: #28a745;
    }

    .status-maintenance {
      background: #fd7e14;
    }

    .status-storage {
      background: #6c757d;
    }

    .status-retired {
      background: #dc3545;
    }

    .timeline-content {
      flex: 1;
      min-width: 0;
    }

    .event-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
      margin-bottom: 8px;
    }

    .event-title {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0;
      line-height: 1.4;
    }

    .event-date {
      font-family: 'Inter', sans-serif;
      font-size: 12px;
      color: var(--on-surface-variant, #434750);
      white-space: nowrap;
      flex-shrink: 0;
    }

    .event-details {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .event-user {
      display: flex;
      align-items: center;
      gap: 4px;
      font-family: 'Inter', sans-serif;
      font-size: 12px;
      color: var(--on-surface-variant, #434750);
    }

    .user-icon {
      font-size: 14px;
      width: 14px;
      height: 14px;
      color: var(--primary, #143b7d);
    }

    /* Empty State Styles */
    .empty-timeline {
      text-align: center;
      padding: 40px 20px;
      background: var(--surface-container-low, #f4f3f9);
      border-radius: 8px;
    }

    .empty-icon {
      margin-bottom: 16px;
    }

    .empty-icon mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: var(--on-surface-variant, #434750);
      opacity: 0.6;
    }

    .empty-title {
      font-family: 'Manrope', sans-serif;
      font-size: 16px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0 0 8px 0;
    }

    .empty-description {
      font-family: 'Inter', sans-serif;
      font-size: 14px;
      color: var(--on-surface-variant, #434750);
      margin: 0;
      line-height: 1.4;
      max-width: 300px;
      margin-left: auto;
      margin-right: auto;
    }

    /* Responsive Design */
    @media (max-width: 480px) {
      .timeline-container {
        padding: 16px;
      }

      .timeline-title {
        font-size: 16px;
        margin-bottom: 16px;
      }

      .timeline-item {
        gap: 12px;
        padding-bottom: 20px;
      }

      .timeline-icon {
        width: 32px;
        height: 32px;
      }

      .timeline-icon mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
      }

      .timeline-line {
        left: 16px;
        top: 32px;
      }

      .event-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
      }

      .event-title {
        font-size: 13px;
      }

      .event-date {
        font-size: 11px;
      }
    }

    /* High Contrast Mode */
    @media (prefers-contrast: high) {
      .timeline-container {
        border: 2px solid var(--outline, #747782);
      }

      .timeline-icon {
        border: 2px solid white;
      }

      .timeline-line {
        background: var(--outline, #747782);
        opacity: 1;
      }
    }

    /* Reduced Motion */
    @media (prefers-reduced-motion: reduce) {
      .timeline-icon {
        transition: none;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LifecycleTimelineComponent {
  @Input() events: LifecycleEvent[] = [];

  /**
   * Get CSS class for status-specific styling
   */
  getStatusClass(status: LifecycleStatus): string {
    const statusMap: Record<LifecycleStatus, string> = {
      [LifecycleStatus.ORDERED]: 'status-ordered',
      [LifecycleStatus.RECEIVED]: 'status-received',
      [LifecycleStatus.DEPLOYED]: 'status-deployed',
      [LifecycleStatus.IN_USE]: 'status-in-use',
      [LifecycleStatus.MAINTENANCE]: 'status-maintenance',
      [LifecycleStatus.STORAGE]: 'status-storage',
      [LifecycleStatus.RETIRED]: 'status-retired'
    };
    return statusMap[status] || 'status-ordered';
  }

  /**
   * Get tooltip text for status
   */
  getStatusTooltip(status: LifecycleStatus): string {
    const tooltipMap: Record<LifecycleStatus, string> = {
      [LifecycleStatus.ORDERED]: 'Asset has been ordered',
      [LifecycleStatus.RECEIVED]: 'Asset has been received',
      [LifecycleStatus.DEPLOYED]: 'Asset has been deployed',
      [LifecycleStatus.IN_USE]: 'Asset is currently in use',
      [LifecycleStatus.MAINTENANCE]: 'Asset is under maintenance',
      [LifecycleStatus.STORAGE]: 'Asset is in storage',
      [LifecycleStatus.RETIRED]: 'Asset has been retired'
    };
    return tooltipMap[status] || 'Unknown status';
  }

  /**
   * Format event date for display
   */
  formatEventDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) {
      return 'Today, ' + date.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit' 
      });
    } else if (diffDays === 1) {
      return 'Yesterday, ' + date.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit' 
      });
    } else if (diffDays < 7) {
      return date.toLocaleDateString('en-US', { 
        weekday: 'long',
        hour: '2-digit', 
        minute: '2-digit' 
      });
    } else {
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
}