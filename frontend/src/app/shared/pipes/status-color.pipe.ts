import { Pipe, PipeTransform } from '@angular/core';
import { LifecycleStatus, TicketStatus } from '../models';

/**
 * StatusColorPipe maps status values to CSS color classes or hex values
 * Supports both LifecycleStatus and TicketStatus enums
 */
@Pipe({
  name: 'statusColor',
  standalone: true
})
export class StatusColorPipe implements PipeTransform {
  
  /**
   * Transform a status value into a color class or hex value
   * @param status - LifecycleStatus or TicketStatus value
   * @param outputType - 'class' for CSS class names, 'hex' for hex color values (default: 'class')
   * @returns Color class name or hex value
   */
  transform(status: LifecycleStatus | TicketStatus | string | null | undefined, outputType: 'class' | 'hex' = 'class'): string {
    if (!status) {
      return outputType === 'class' ? 'status-default' : '#6c757d';
    }

    const statusUpper = status.toString().toUpperCase();

    // Map LifecycleStatus to colors
    const lifecycleColorMap: Record<string, { class: string; hex: string }> = {
      ORDERED: { class: 'status-info', hex: '#0dcaf0' },
      RECEIVED: { class: 'status-primary', hex: '#0d6efd' },
      DEPLOYED: { class: 'status-success', hex: '#198754' },
      IN_USE: { class: 'status-success', hex: '#198754' },
      IN_MAINTENANCE: { class: 'status-warning', hex: '#ffc107' },
      IN_STORAGE: { class: 'status-secondary', hex: '#6c757d' },
      RETIRED: { class: 'status-dark', hex: '#495057' },
      DISPOSED: { class: 'status-dark', hex: '#343a40' }
    };

    // Map TicketStatus to colors
    const ticketColorMap: Record<string, { class: string; hex: string }> = {
      PENDING: { class: 'status-warning', hex: '#ffc107' },
      APPROVED: { class: 'status-success', hex: '#198754' },
      REJECTED: { class: 'status-danger', hex: '#dc3545' },
      IN_PROGRESS: { class: 'status-info', hex: '#0dcaf0' },
      COMPLETED: { class: 'status-success', hex: '#198754' },
      CANCELLED: { class: 'status-secondary', hex: '#6c757d' }
    };

    // Check lifecycle status first
    if (lifecycleColorMap[statusUpper]) {
      return outputType === 'class' 
        ? lifecycleColorMap[statusUpper].class 
        : lifecycleColorMap[statusUpper].hex;
    }

    // Check ticket status
    if (ticketColorMap[statusUpper]) {
      return outputType === 'class' 
        ? ticketColorMap[statusUpper].class 
        : ticketColorMap[statusUpper].hex;
    }

    // Default color for unknown status
    return outputType === 'class' ? 'status-default' : '#6c757d';
  }
}
