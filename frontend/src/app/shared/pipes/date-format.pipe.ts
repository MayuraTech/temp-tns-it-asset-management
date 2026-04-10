import { Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';

/**
 * DateFormatPipe provides consistent date formatting across the application.
 * Supports multiple format types: short, medium, long, full
 * Handles both Date objects and ISO string dates
 */
@Pipe({
  name: 'dateFormat',
  standalone: true
})
export class DateFormatPipe implements PipeTransform {
  private datePipe: DatePipe;

  constructor() {
    this.datePipe = new DatePipe('en-US');
  }

  /**
   * Transform a date value into a formatted string
   * @param value - Date object or ISO string
   * @param format - Format type: 'short' | 'medium' | 'long' | 'full' (default: 'medium')
   * @returns Formatted date string or empty string if invalid
   */
  transform(value: Date | string | null | undefined, format: 'short' | 'medium' | 'long' | 'full' = 'medium'): string {
    if (!value) {
      return '';
    }

    // Convert string to Date if needed
    const date = typeof value === 'string' ? new Date(value) : value;

    // Check if date is valid
    if (isNaN(date.getTime())) {
      return '';
    }

    // Map format types to Angular DatePipe formats
    const formatMap: Record<string, string> = {
      short: 'M/d/yy',
      medium: 'MMM d, y',
      long: 'MMMM d, y',
      full: 'EEEE, MMMM d, y'
    };

    const dateFormat = formatMap[format] || formatMap.medium;
    return this.datePipe.transform(date, dateFormat) || '';
  }
}
