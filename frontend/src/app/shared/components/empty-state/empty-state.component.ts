/**
 * EmptyStateComponent - Editorial Typography Empty State
 * 
 * Displays a centered empty state with geometric icon, editorial heading,
 * and descriptive text following Editorial Geometry principles.
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 21.1
 * Task 7.3: Empty state component with editorial typography
 */

import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './empty-state.component.html',
  styleUrls: ['./empty-state.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmptyStateComponent {
  
  /**
   * Heading text - defaults to "Workspace Canvas"
   * Requirement 9.2: Display heading using Manrope ExtraBold
   */
  @Input() heading: string = 'Workspace Canvas';
  
  /**
   * Description text explaining the purpose
   * Requirement 9.3: Display descriptive text
   */
  @Input() description: string = 
    'This is your dynamic content area where feature modules will be displayed. ' +
    'The Editorial Geometry design system creates depth through tonal layering ' +
    'and geometric accents rather than harsh structural lines.';
  
  /**
   * Additional information text
   */
  @Input() additionalInfo: string = 
    'Navigate using the sidebar to load different application sections, ' +
    'or use the search bar to find specific infrastructure items.';
  
  /**
   * Icon to display - can be emoji or icon class
   * Requirement 9.1: Display icon (27x27px) in rounded background
   */
  @Input() icon: string = '🎨';
  
  /**
   * Whether to show the additional info section
   */
  @Input() showAdditionalInfo: boolean = true;
  
  constructor() {}
}
