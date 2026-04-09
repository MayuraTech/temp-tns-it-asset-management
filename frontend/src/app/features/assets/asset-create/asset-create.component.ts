/**
 * Asset Create Component - Create New Asset
 */

import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-asset-create',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <h1 class="page-title">Add New Asset</h1>
      <p class="page-description">Create a new IT asset</p>
      <div class="placeholder-card">
        <p>Asset creation form will be displayed here.</p>
        <button class="btn-secondary" (click)="goBack()">Back to Assets</button>
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
    
    .btn-secondary {
      margin-top: var(--space-lg);
      padding: 12px 24px;
      background: var(--primary);
      color: white;
      border: none;
      border-radius: 8px;
      font-family: var(--font-body);
      font-weight: 700;
      cursor: pointer;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssetCreateComponent {
  constructor(private router: Router) {}
  
  goBack(): void {
    this.router.navigate(['/assets']);
  }
}
