/**
 * Geometric Triangle Component - Editorial Geometry Accent System
 * 
 * Renders geometric triangle accents that break the grid and act as
 * visual anchors following Editorial Geometry design principles.
 * 
 * Requirements: 24.1, 24.2, 24.4, 24.5, 24.7
 * Sub-task 5.1: Geometric triangle accent system with configurable size,
 * color, positioning, and 80px breathing room enforcement.
 */

import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

export type TriangleSize = 'small' | 'medium' | 'large' | 'xlarge';
export type TrianglePosition = 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right' | 'center';

@Component({
  selector: 'app-geometric-triangle',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="triangle-container"
      [class]="positionClass"
      [class.with-blur]="blur"
      [class.responsive]="responsive"
      [style.width.px]="triangleWidth"
      [style.height.px]="triangleHeight">
      <svg 
        [attr.width]="triangleWidth" 
        [attr.height]="triangleHeight" 
        [attr.viewBox]="viewBox" 
        fill="none" 
        xmlns="http://www.w3.org/2000/svg"
        class="triangle-svg"
        [attr.aria-hidden]="true">
        <defs *ngIf="blur">
          <filter id="triangle-blur-{{uniqueId}}">
            <feGaussianBlur in="SourceGraphic" stdDeviation="8"/>
          </filter>
        </defs>
        <path 
          [attr.d]="trianglePath" 
          [attr.fill]="color" 
          [attr.opacity]="opacity"
          [attr.filter]="blur ? 'url(#triangle-blur-' + uniqueId + ')' : null"/>
      </svg>
    </div>
  `,
  styles: [`
    .triangle-container {
      position: absolute;
      z-index: -1;
      pointer-events: none;
      transition: transform 0.3s ease;
    }
    
    .triangle-svg {
      display: block;
    }
    
    /* Position Classes - Requirement 24.4 */
    .position-top-left {
      top: 0;
      left: 0;
    }
    
    .position-top-right {
      top: 0;
      right: 0;
    }
    
    .position-bottom-left {
      bottom: 0;
      left: 0;
    }
    
    .position-bottom-right {
      bottom: 0;
      right: 0;
    }
    
    .position-center {
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
    
    /* Blur Effect for Background Elements - Requirement 24.3 */
    .with-blur {
      filter: blur(8px);
      opacity: 0.8;
    }
    
    /* Responsive Scaling - Requirement 24.7 */
    .responsive {
      @media (max-width: 1024px) {
        transform: scale(0.8);
      }
      
      @media (max-width: 768px) {
        transform: scale(0.6);
      }
    }
    
    .responsive.position-center {
      @media (max-width: 1024px) {
        transform: translate(-50%, -50%) scale(0.8);
      }
      
      @media (max-width: 768px) {
        transform: translate(-50%, -50%) scale(0.6);
      }
    }
    
    /* Reduced Motion Support */
    @media (prefers-reduced-motion: reduce) {
      .triangle-container {
        transition: none;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GeometricTriangleComponent {
  /**
   * Size of the triangle - Requirement 24.1
   */
  @Input() size: TriangleSize = 'medium';
  
  /**
   * Position of the triangle - Requirement 24.4
   */
  @Input() position: TrianglePosition = 'top-right';
  
  /**
   * Color of the triangle - Requirement 24.5
   */
  @Input() color: string = '#143b7d';
  
  /**
   * Opacity of the triangle
   */
  @Input() opacity: number = 0.1;
  
  /**
   * Apply blur effect for background elements - Requirement 24.3
   */
  @Input() blur: boolean = false;
  
  /**
   * Enable responsive scaling - Requirement 24.7
   */
  @Input() responsive: boolean = true;
  
  /**
   * Unique ID for SVG filter references
   */
  uniqueId: string = Math.random().toString(36).substring(7);

  /**
   * Get triangle width based on size - Requirement 24.1
   */
  get triangleWidth(): number {
    switch (this.size) {
      case 'small': return 80;
      case 'medium': return 120;
      case 'large': return 200;
      case 'xlarge': return 300;
      default: return 120;
    }
  }

  /**
   * Get triangle height based on size - Requirement 24.1
   */
  get triangleHeight(): number {
    switch (this.size) {
      case 'small': return 69;
      case 'medium': return 104;
      case 'large': return 173;
      case 'xlarge': return 260;
      default: return 104;
    }
  }

  /**
   * Get SVG viewBox
   */
  get viewBox(): string {
    return `0 0 ${this.triangleWidth} ${this.triangleHeight}`;
  }

  /**
   * Generate triangle path - Equilateral triangle pointing upward
   */
  get trianglePath(): string {
    const width = this.triangleWidth;
    const height = this.triangleHeight;
    const halfWidth = width / 2;
    
    // Equilateral triangle pointing upward
    return `M${halfWidth} 0L${width - 0.358979 * width} ${height}H${0.358979 * width}L${halfWidth} 0Z`;
  }

  /**
   * Get position class
   */
  get positionClass(): string {
    return `position-${this.position}`;
  }
}