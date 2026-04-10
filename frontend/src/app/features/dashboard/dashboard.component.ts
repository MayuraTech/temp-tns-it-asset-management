/**
 * Dashboard Component - Main Dashboard View
 * 
 * Displays the main dashboard with overview metrics and quick actions
 * following Editorial Geometry design principles.
 */

import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container">
      <h1 class="dashboard-title">Dashboard</h1>
      <p class="dashboard-description">Welcome to AssetIntel - Corporate IT Global Infrastructure</p>
      
      <div class="dashboard-content">
        <div class="placeholder-card">
          <h2>Overview</h2>
          <p>Dashboard content will be displayed here.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: var(--space-xxl);
    }
    
    .dashboard-title {
      font-family: var(--font-heading);
      font-size: var(--headline-lg);
      font-weight: 800;
      color: var(--secondary);
      margin-bottom: var(--space-md);
    }
    
    .dashboard-description {
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
    
    .placeholder-card h2 {
      font-family: var(--font-heading);
      font-size: var(--headline-md);
      color: var(--on-surface);
      margin-bottom: var(--space-md);
    }
    
    .placeholder-card p {
      font-family: var(--font-body);
      color: var(--on-surface-variant);
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
}
