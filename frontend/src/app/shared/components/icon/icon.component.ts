/**
 * Icon Component - Editorial Geometry Icon System
 * 
 * Reusable SVG icon component with size and color customization
 * following Editorial Geometry design principles.
 */

import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-icon',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span 
      class="icon-container"
      [style.width.px]="size"
      [style.height.px]="size"
      [style.color]="color"
      [innerHTML]="iconSvg"
      [attr.aria-label]="ariaLabel"
      [attr.role]="ariaLabel ? 'img' : 'presentation'">
    </span>
  `,
  styles: [`
    .icon-container {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    
    .icon-container :deep(svg) {
      width: 100%;
      height: 100%;
      display: block;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class IconComponent {
  @Input() name: string = '';
  @Input() size: number = 20;
  @Input() color: string = 'currentColor';
  @Input() ariaLabel?: string;

  private iconMap: { [key: string]: string } = {
    'assets-icon': '/assets/icons/assets-icon.svg',
    'software-icon': '/assets/icons/software-icon.svg',
    'licenses-icon': '/assets/icons/licenses-icon.svg',
    'network-icon': '/assets/icons/network-icon.svg',
    'users-icon': '/assets/icons/users-icon.svg',
    'audit-icon': '/assets/icons/audit-icon.svg',
    'archive-icon': '/assets/icons/archive-icon.svg',
    'search-icon': '/assets/icons/search-icon.svg',
    'notification-icon': '/assets/icons/notification-icon.svg',
    'settings-icon': '/assets/icons/settings-icon.svg',
    'logo-icon': '/assets/icons/logo-icon.svg',
    'geometric-triangle-accent': '/assets/icons/geometric-triangle-accent.svg',
    'geometric-triangle-large': '/assets/icons/geometric-triangle-large.svg',
    'geometric-triangle-small': '/assets/icons/geometric-triangle-small.svg'
  };

  constructor(private sanitizer: DomSanitizer) {}

  get iconSvg(): SafeHtml {
    const iconPath = this.iconMap[this.name];
    if (!iconPath) {
      console.warn(`Icon "${this.name}" not found in icon map`);
      return '';
    }

    // For now, return a placeholder. In a real implementation,
    // you would fetch the SVG content and sanitize it
    const svgContent = this.getSvgContent(this.name);
    return this.sanitizer.bypassSecurityTrustHtml(svgContent);
  }

  private getSvgContent(iconName: string): string {
    // This is a simplified implementation. In a real app, you might:
    // 1. Pre-load SVG content at build time
    // 2. Use an HTTP service to fetch SVG content
    // 3. Inline SVG content directly
    
    switch (iconName) {
      case 'assets-icon':
        return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M3 4H17C17.5523 4 18 4.44772 18 5V15C18 15.5523 17.5523 16 17 16H3C2.44772 16 2 15.5523 2 15V5C2 4.44772 2.44772 4 3 4Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M6 8H14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M6 12H10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>`;
      
      case 'software-icon':
        return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4 3H16C16.5523 3 17 3.44772 17 4V16C17 16.5523 16.5523 17 16 17H4C3.44772 17 3 16.5523 3 16V4C3 3.44772 3.44772 3 4 3Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M7 7L9 9L13 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M7 13H13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>`;
      
      case 'licenses-icon':
        return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M5 4H15C15.5523 4 16 4.44772 16 5V15C16 15.5523 15.5523 16 15 16H5C4.44772 16 4 15.5523 4 15V5C4 4.44772 4.44772 4 5 4Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M8 2V6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M12 2V6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M7 9H13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M7 12H11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>`;
      
      case 'network-icon':
        return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <circle cx="6" cy="6" r="1.5" fill="currentColor"/>
          <circle cx="14" cy="6" r="1.5" fill="currentColor"/>
          <circle cx="6" cy="14" r="1.5" fill="currentColor"/>
          <circle cx="14" cy="14" r="1.5" fill="currentColor"/>
          <path d="M6 6L14 14" stroke="currentColor" stroke-width="1" opacity="0.5"/>
          <path d="M14 6L6 14" stroke="currentColor" stroke-width="1" opacity="0.5"/>
        </svg>`;
      
      case 'users-icon':
        return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="7" cy="6" r="3" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M2 18C2 14.6863 4.68629 12 8 12H6C4.89543 12 4 12.8954 4 14V18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <circle cx="14" cy="7" r="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M18 18C18 16.3431 16.6569 15 15 15H13C12.4477 15 12 15.4477 12 16V18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>`;
      
      case 'audit-icon':
        return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4 6H16C16.5523 6 17 6.44772 17 7V17C17 17.5523 16.5523 18 16 18H4C3.44772 18 3 17.5523 3 17V7C3 6.44772 3.44772 6 4 6Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M6 3V9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M10 3V9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <path d="M14 3V9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          <circle cx="10" cy="13" r="1" fill="currentColor"/>
        </svg>`;
      
      case 'archive-icon':
        return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M3 6H17V16C17 16.5523 16.5523 17 16 17H4C3.44772 17 3 16.5523 3 16V6Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M2 3H18V6H2V3Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M8 10H12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>`;
      
      case 'search-icon':
        return `<svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="m13 13-3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>`;
      
      case 'notification-icon':
        return `<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M13.73 13.73C14.4127 13.0473 14.8 12.1424 14.8 11.2V8.2C14.8 5.88609 12.9139 4 10.6 4H7.4C5.08609 4 3.2 5.88609 3.2 8.2V11.2C3.2 12.1424 3.58734 13.0473 4.27 13.73L5 14.46V15.4C5 15.7314 5.26863 16 5.6 16H12.4C12.7314 16 13 15.7314 13 15.4V14.46L13.73 13.73Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M7 2C7 1.44772 7.44772 1 8 1H10C10.5523 1 11 1.44772 11 2V4H7V2Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
        </svg>`;
      
      case 'settings-icon':
        return `<svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="9" cy="9" r="3" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M9 1L10.09 3.26L12.5 2.5L13.5 4.5L15.74 5.59L15 8L15.74 10.41L13.5 11.5L12.5 13.5L10.09 12.74L9 15L7.91 12.74L5.5 13.5L4.5 11.5L2.26 10.41L3 8L2.26 5.59L4.5 4.5L5.5 2.5L7.91 3.26L9 1Z" stroke="currentColor" stroke-width="1.5" fill="none"/>
        </svg>`;
      
      case 'logo-icon':
        return `<svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="40" height="40" rx="8" fill="#315396"/>
          <path d="M12 28V12H16L20 20L24 12H28V28H24V18L20 26L16 18V28H12Z" fill="white"/>
          <path d="M8 8L12 12L8 16L4 12L8 8Z" fill="#143b7d" opacity="0.3"/>
          <path d="M32 24L36 28L32 32L28 28L32 24Z" fill="#143b7d" opacity="0.3"/>
        </svg>`;
      
      default:
        return `<svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="2" y="2" width="16" height="16" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <path d="M6 10L8 12L14 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>`;
    }
  }
}