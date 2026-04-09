/**
 * Assets Component - Assets Management View
 */

import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-assets',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <h1 class="page-title">Assets</h1>
      <p class="page-description">Manage your IT assets</p>
      <div class="placeholder-card">
        <p>Assets list will be displayed here.</p>
      </div>
    </div>
  `,
  styles: [`
    .page-container {
      padding: var(--space-xxl);
    }
    
    .page-title {
      font-family: var(--font-heading);
      font-size: var(--headline-lg);
      font-weight: 800;
      color: var(--secondary);
      margin-bottom: var(--space-md);
    }
    
    .page-description {
      font-family: var(--font-body);
      font-size: var(--body-lg);
      color: var(--on-surface-variant);
      margin-bottom: var(--space-xl);
    }
    
    .placeholder-card {
      background: var(--surface-container-lowest);
      border-radius: 8px;
      padding: var(--space-xl);
      box-shadow: 0 20px 40px rgba(20, 59, 125, 0.06);
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssetsComponent {
}
