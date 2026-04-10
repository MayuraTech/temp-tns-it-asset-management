import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';

type AssetStatus = 'ordered' | 'received' | 'deployed' | 'in_use' | 'maintenance' | 'storage' | 'retired';
type TicketStatus = 'pending' | 'approved' | 'rejected' | 'in_progress' | 'completed' | 'cancelled';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule, MatChipsModule],
  templateUrl: './status-badge.component.html',
  styleUrls: ['./status-badge.component.scss']
})
export class StatusBadgeComponent {
  @Input() status!: AssetStatus | TicketStatus | string;
  @Input() type: 'asset' | 'ticket' = 'asset';

  getStatusClass(): string {
    const statusLower = this.status?.toLowerCase();
    
    if (this.type === 'asset') {
      switch (statusLower) {
        case 'ordered':
          return 'status-ordered';
        case 'received':
          return 'status-received';
        case 'deployed':
        case 'in_use':
          return 'status-active';
        case 'maintenance':
          return 'status-maintenance';
        case 'storage':
          return 'status-storage';
        case 'retired':
          return 'status-retired';
        default:
          return 'status-default';
      }
    } else {
      switch (statusLower) {
        case 'pending':
          return 'status-pending';
        case 'approved':
          return 'status-approved';
        case 'rejected':
          return 'status-rejected';
        case 'in_progress':
          return 'status-in-progress';
        case 'completed':
          return 'status-completed';
        case 'cancelled':
          return 'status-cancelled';
        default:
          return 'status-default';
      }
    }
  }

  getDisplayText(): string {
    return this.status?.replace(/_/g, ' ').toUpperCase() || '';
  }
}
