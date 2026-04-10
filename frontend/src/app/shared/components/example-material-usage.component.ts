/**
 * Example Material Usage Component
 * 
 * This component demonstrates the usage of Angular Material components
 * with the custom theme and responsive breakpoints.
 * 
 * This is a reference component and can be removed once actual feature
 * components are implemented.
 */

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from '../material.module';

@Component({
  selector: 'app-example-material-usage',
  standalone: true,
  imports: [CommonModule, MaterialModule],
  template: `
    <div class="container responsive-padding">
      <h1>Angular Material Configuration Example</h1>
      
      <!-- Card Example -->
      <mat-card class="mt-3">
        <mat-card-header>
          <mat-card-title>Material Components</mat-card-title>
          <mat-card-subtitle>Demonstrating configured components</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          
          <!-- Buttons -->
          <div class="mb-3">
            <h3>Buttons</h3>
            <button mat-raised-button color="primary" class="mr-2">Primary</button>
            <button mat-raised-button color="accent" class="mr-2">Accent</button>
            <button mat-raised-button color="warn" class="mr-2">Warn</button>
            <button mat-stroked-button class="mr-2">Stroked</button>
            <button mat-flat-button>Flat</button>
          </div>
          
          <!-- Status Badges -->
          <div class="mb-3">
            <h3>Status Badges</h3>
            <span class="status-badge ordered mr-2">Ordered</span>
            <span class="status-badge received mr-2">Received</span>
            <span class="status-badge deployed mr-2">Deployed</span>
            <span class="status-badge in-use mr-2">In Use</span>
            <span class="status-badge maintenance mr-2">Maintenance</span>
            <span class="status-badge storage mr-2">Storage</span>
            <span class="status-badge retired">Retired</span>
          </div>
          
          <!-- Priority Badges -->
          <div class="mb-3">
            <h3>Priority Badges</h3>
            <span class="priority-badge low mr-2">Low</span>
            <span class="priority-badge medium mr-2">Medium</span>
            <span class="priority-badge high mr-2">High</span>
            <span class="priority-badge urgent">Urgent</span>
          </div>
          
          <!-- Form Fields -->
          <div class="mb-3">
            <h3>Form Fields</h3>
            <div class="form-responsive">
              <mat-form-field>
                <mat-label>Name</mat-label>
                <input matInput placeholder="Enter name">
              </mat-form-field>
              
              <mat-form-field>
                <mat-label>Email</mat-label>
                <input matInput type="email" placeholder="Enter email">
              </mat-form-field>
              
              <mat-form-field>
                <mat-label>Status</mat-label>
                <mat-select>
                  <mat-option value="ordered">Ordered</mat-option>
                  <mat-option value="received">Received</mat-option>
                  <mat-option value="deployed">Deployed</mat-option>
                </mat-select>
              </mat-form-field>
            </div>
          </div>
          
          <!-- Responsive Grid -->
          <div class="mb-3">
            <h3>Responsive Grid</h3>
            <div class="row">
              <div class="col-mobile-12 col-tablet-6 col-desktop-4">
                <mat-card class="bg-light">
                  <mat-card-content>
                    Column 1
                  </mat-card-content>
                </mat-card>
              </div>
              <div class="col-mobile-12 col-tablet-6 col-desktop-4">
                <mat-card class="bg-light">
                  <mat-card-content>
                    Column 2
                  </mat-card-content>
                </mat-card>
              </div>
              <div class="col-mobile-12 col-tablet-6 col-desktop-4">
                <mat-card class="bg-light">
                  <mat-card-content>
                    Column 3
                  </mat-card-content>
                </mat-card>
              </div>
            </div>
          </div>
          
          <!-- Icons with Badges -->
          <div class="mb-3">
            <h3>Icons with Badges</h3>
            <button mat-icon-button class="mr-2">
              <mat-icon matBadge="5" matBadgeColor="warn">notifications</mat-icon>
            </button>
            <button mat-icon-button class="mr-2">
              <mat-icon matBadge="3" matBadgeColor="primary">mail</mat-icon>
            </button>
            <button mat-icon-button>
              <mat-icon>settings</mat-icon>
            </button>
          </div>
          
          <!-- Progress Indicators -->
          <div class="mb-3">
            <h3>Progress Indicators</h3>
            <mat-progress-bar mode="determinate" value="60" class="mb-2"></mat-progress-bar>
            <mat-progress-bar mode="indeterminate" class="mb-2"></mat-progress-bar>
            <mat-spinner diameter="40"></mat-spinner>
          </div>
          
          <!-- Chips -->
          <div class="mb-3">
            <h3>Chips</h3>
            <mat-chip-set>
              <mat-chip>Server</mat-chip>
              <mat-chip>Workstation</mat-chip>
              <mat-chip>Network Device</mat-chip>
              <mat-chip>Laptop</mat-chip>
            </mat-chip-set>
          </div>
          
        </mat-card-content>
        <mat-card-actions>
          <button mat-button color="primary">SAVE</button>
          <button mat-button>CANCEL</button>
        </mat-card-actions>
      </mat-card>
      
      <!-- Responsive Visibility -->
      <mat-card class="mt-3">
        <mat-card-header>
          <mat-card-title>Responsive Visibility</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="show-mobile">
            <mat-chip color="primary">Visible on Mobile Only</mat-chip>
          </div>
          <div class="show-tablet">
            <mat-chip color="accent">Visible on Tablet Only</mat-chip>
          </div>
          <div class="show-desktop">
            <mat-chip color="warn">Visible on Desktop Only</mat-chip>
          </div>
          <div class="hide-mobile">
            <mat-chip>Hidden on Mobile</mat-chip>
          </div>
        </mat-card-content>
      </mat-card>
      
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
  `]
})
export class ExampleMaterialUsageComponent { }
