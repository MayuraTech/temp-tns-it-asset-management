/**
 * MainContentComponent - Tonal Layered Content Area
 * 
 * Implements the flexible content area with Editorial Geometry background system,
 * content projection, geometric accents, and empty state display.
 * 
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 16.4, 23.1, 23.2, 23.3, 24.3
 * Sub-task 5.1: Geometric triangle accents in background
 */

import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable, BehaviorSubject } from 'rxjs';
import { GeometricTriangleComponent } from '../../../shared/components/geometric-triangle/geometric-triangle.component';

@Component({
  selector: 'app-main-content',
  standalone: true,
  imports: [CommonModule, GeometricTriangleComponent],
  templateUrl: './main-content.component.html',
  styleUrls: ['./main-content.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainContentComponent implements OnInit {
  
  /**
   * Empty state observable - determines if empty state should be shown
   */
  isEmpty$ = new BehaviorSubject<boolean>(true);
  
  constructor() {}
  
  ngOnInit(): void {
    // Initialize component
    // TODO: Connect to router to detect when content is loaded
  }
  
  /**
   * Show empty state when no content is available
   */
  showEmptyState(): void {
    this.isEmpty$.next(true);
  }
}