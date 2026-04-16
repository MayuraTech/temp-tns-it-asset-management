import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LifecycleStatus } from '../../../models';

/**
 * Asset Status Badge Component
 * 
 * Displays lifecycle status with appropriate color coding and styling.
 * Follows Editorial Geometry design system with no 1px borders and surface hierarchy.
 * 
 * Features:
 * - Color-coded status badges for all 7 lifecycle statuses
 * - Responsive design with proper contrast ratios
 * - Accessible with proper ARIA labels
 * - Editorial Geometry styling with rounded corners and subtle shadows
 */
@Component({
  selector: 'app-asset-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span 
      class="status-badge"
      [class]="getStatusClass(status)"
      [attr.aria-label]="getStatusLabel(status)"
      role="status">
      {{ getStatusLabel(status) }}
    </span>
  `,
  styles: [`
    .status-badge {
      display: inline-flex;
      align-items: center;
      padding: 4px 12px;
      border-radius: 8px;
      font-family: 'Inter', sans-serif;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.3px;
      text-transform: uppercase;
      white-space: nowrap;
      box-shadow: 0 2px 4px rgba(20, 59, 125, 0.1);
      transition: all 0.2s ease;
    }

    /* Status-specific color coding */
    .status-ordered {
      background: rgba(255, 193, 7, 0.15);
      color: #b8860b;
    }

    .status-received {
      background: rgba(13, 110, 253, 0.15);
      color: #0d6efd;
    }

    .status-deployed {
      background: rgba(25, 135, 84, 0.15);
      color: #198754;
    }

    .status-in-use {
      background: rgba(40, 167, 69, 0.15);
      color: #28a745;
    }

    .status-maintenance {
      background: rgba(255, 133, 27, 0.15);
      color: #fd7e14;
    }

    .status-storage {
      background: rgba(108, 117, 125, 0.15);
      color: #6c757d;
    }

    .status-retired {
      background: rgba(220, 53, 69, 0.15);
      color: #dc3545;
    }

    /* Hover effects for interactive contexts */
    .status-badge:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 8px rgba(20, 59, 125, 0.15);
    }

    /* High contrast mode support */
    @media (prefers-contrast: high) {
      .status-badge {
        border: 2px solid currentColor;
        font-weight: 700;
      }
    }

    /* Reduced motion support */
    @media (prefers-reduced-motion: reduce) {
      .status-badge {
        transition: none;
      }
      
      .status-badge:hover {
        transform: none;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssetStatusBadgeComponent {
  @Input() status!: LifecycleStatus;

  /**
   * Get CSS class for the status badge
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
   * Get human-readable label for the status
   */
  getStatusLabel(status: LifecycleStatus): string {
    const labelMap: Record<LifecycleStatus, string> = {
      [LifecycleStatus.ORDERED]: 'Ordered',
      [LifecycleStatus.RECEIVED]: 'Received',
      [LifecycleStatus.DEPLOYED]: 'Deployed',
      [LifecycleStatus.IN_USE]: 'In Use',
      [LifecycleStatus.MAINTENANCE]: 'Maintenance',
      [LifecycleStatus.STORAGE]: 'Storage',
      [LifecycleStatus.RETIRED]: 'Retired'
    };
    return labelMap[status] || 'Unknown';
  }
}