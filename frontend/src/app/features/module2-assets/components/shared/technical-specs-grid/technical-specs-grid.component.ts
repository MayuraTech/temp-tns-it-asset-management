import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AssetType } from '../../../models';

/**
 * Interface for technical specification entries
 */
export interface TechnicalSpec {
  label: string;
  value: string;
  icon?: string;
  unit?: string;
  category?: string;
}

/**
 * Technical Specs Grid Component
 * 
 * Displays technical specifications in a responsive grid layout.
 * Used in asset detail view and form side panels to show key specifications.
 * 
 * Features:
 * - Responsive grid layout with key-value pairs
 * - Category grouping for organized display
 * - Icon support for visual identification
 * - Unit display for measurements
 * - Asset type-specific default specifications
 * - Editorial Geometry styling with proper spacing
 */
@Component({
  selector: 'app-technical-specs-grid',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatTooltipModule
  ],
  template: `
    <div class="specs-container" *ngIf="displaySpecs && displaySpecs.length > 0; else emptyTemplate">
      <h3 class="specs-title">Technical Specifications</h3>
      
      <!-- Grouped Specifications -->
      <div class="specs-content" *ngIf="groupedSpecs; else flatSpecs">
        <div 
          class="spec-category"
          *ngFor="let category of getCategories()"
          [attr.aria-labelledby]="'category-' + getCategoryId(category)">
          
          <h4 
            class="category-title"
            [id]="'category-' + getCategoryId(category)">
            {{ category }}
          </h4>
          
          <div class="specs-grid">
            <div 
              class="spec-item"
              *ngFor="let spec of getSpecsByCategory(category)"
              role="definition">
              
              <div class="spec-label" [attr.aria-label]="spec.label">
                <mat-icon 
                  *ngIf="spec.icon"
                  class="spec-icon"
                  [matTooltip]="spec.label">
                  {{ spec.icon }}
                </mat-icon>
                <span class="label-text">{{ spec.label }}</span>
              </div>
              
              <div class="spec-value">
                <span class="value-text">{{ spec.value }}</span>
                <span class="value-unit" *ngIf="spec.unit">{{ spec.unit }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Flat Specifications -->
      <ng-template #flatSpecs>
        <div class="specs-grid">
          <div 
            class="spec-item"
            *ngFor="let spec of displaySpecs"
            role="definition">
            
            <div class="spec-label" [attr.aria-label]="spec.label">
              <mat-icon 
                *ngIf="spec.icon"
                class="spec-icon"
                [matTooltip]="spec.label">
                {{ spec.icon }}
              </mat-icon>
              <span class="label-text">{{ spec.label }}</span>
            </div>
            
            <div class="spec-value">
              <span class="value-text">{{ spec.value }}</span>
              <span class="value-unit" *ngIf="spec.unit">{{ spec.unit }}</span>
            </div>
          </div>
        </div>
      </ng-template>
    </div>

    <!-- Empty State Template -->
    <ng-template #emptyTemplate>
      <div class="empty-specs">
        <div class="empty-icon">
          <mat-icon>info</mat-icon>
        </div>
        <h3 class="empty-title">No Specifications Available</h3>
        <p class="empty-description">
          Technical specifications will be displayed here when available.
        </p>
      </div>
    </ng-template>
  `,
  styles: [`
    .specs-container {
      background: var(--surface-container-lowest, #ffffff);
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(20, 59, 125, 0.06);
    }

    .specs-title {
      font-family: 'Manrope', sans-serif;
      font-size: 16px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0 0 16px 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .specs-title::before {
      content: '';
      width: 3px;
      height: 16px;
      background: var(--tertiary, #80002b);
      border-radius: 2px;
    }

    .specs-content {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .spec-category {
      border-bottom: 1px solid var(--outline-variant, #c4c6d2);
      padding-bottom: 16px;
    }

    .spec-category:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    .category-title {
      font-family: 'Inter', sans-serif;
      font-size: 13px;
      font-weight: 600;
      color: var(--on-surface-variant, #434750);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin: 0 0 12px 0;
    }

    .specs-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .spec-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      min-height: 32px;
    }

    .spec-label {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
      min-width: 0;
    }

    .spec-icon {
      color: var(--primary, #143b7d);
      font-size: 16px;
      width: 16px;
      height: 16px;
      flex-shrink: 0;
    }

    .label-text {
      font-family: 'Inter', sans-serif;
      font-size: 13px;
      color: var(--on-surface-variant, #434750);
      font-weight: 500;
      word-break: break-word;
    }

    .spec-value {
      display: flex;
      align-items: baseline;
      gap: 4px;
      flex-shrink: 0;
      text-align: right;
    }

    .value-text {
      font-family: 'Manrope', sans-serif;
      font-size: 13px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
    }

    .value-unit {
      font-family: 'Inter', sans-serif;
      font-size: 11px;
      color: var(--on-surface-variant, #434750);
      font-weight: 400;
    }

    /* Empty State Styles */
    .empty-specs {
      text-align: center;
      padding: 32px 16px;
      background: var(--surface-container-low, #f4f3f9);
      border-radius: 8px;
    }

    .empty-icon {
      margin-bottom: 12px;
    }

    .empty-icon mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: var(--on-surface-variant, #434750);
      opacity: 0.6;
    }

    .empty-title {
      font-family: 'Manrope', sans-serif;
      font-size: 14px;
      font-weight: 600;
      color: var(--on-surface, #1a1b20);
      margin: 0 0 6px 0;
    }

    .empty-description {
      font-family: 'Inter', sans-serif;
      font-size: 12px;
      color: var(--on-surface-variant, #434750);
      margin: 0;
      line-height: 1.4;
    }

    /* Responsive Design */
    @media (min-width: 480px) {
      .specs-grid {
        grid-template-columns: 1fr 1fr;
        gap: 16px 20px;
      }

      .spec-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
        padding: 12px;
        background: var(--surface-container-low, #f4f3f9);
        border-radius: 6px;
      }

      .spec-value {
        align-self: flex-end;
      }
    }

    @media (min-width: 768px) {
      .specs-container {
        padding: 24px;
      }

      .specs-title {
        font-size: 18px;
        margin-bottom: 20px;
      }

      .specs-grid {
        grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      }
    }

    @media (max-width: 479px) {
      .specs-container {
        padding: 16px;
      }

      .specs-title {
        font-size: 14px;
        margin-bottom: 12px;
      }

      .spec-item {
        padding: 6px 0;
        min-height: 28px;
      }

      .label-text {
        font-size: 12px;
      }

      .value-text {
        font-size: 12px;
      }

      .value-unit {
        font-size: 10px;
      }
    }

    /* High Contrast Mode */
    @media (prefers-contrast: high) {
      .specs-container {
        border: 2px solid var(--outline, #747782);
      }

      .spec-item {
        border-bottom: 1px solid var(--outline-variant, #c4c6d2);
      }

      .spec-item:last-child {
        border-bottom: none;
      }
    }

    /* Print Styles */
    @media print {
      .specs-container {
        box-shadow: none;
        border: 1px solid #ccc;
      }

      .spec-icon {
        display: none;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TechnicalSpecsGridComponent {
  @Input() specifications: TechnicalSpec[] = [];
  @Input() assetType?: AssetType;
  @Input() groupedSpecs: boolean = false;

  /**
   * Get specifications to display (custom or default)
   */
  get displaySpecs(): TechnicalSpec[] {
    if (this.specifications && this.specifications.length > 0) {
      return this.specifications;
    }
    
    if (this.assetType) {
      return this.getDefaultSpecsForAssetType(this.assetType);
    }
    
    return [];
  }

  /**
   * Get unique categories from specifications
   */
  getCategories(): string[] {
    if (!this.groupedSpecs) return [];
    
    const categories = this.displaySpecs
      .map(spec => spec.category)
      .filter((category, index, array) => category && array.indexOf(category) === index);
    
    return categories as string[];
  }

  /**
   * Get specifications by category
   */
  getSpecsByCategory(category: string): TechnicalSpec[] {
    return this.displaySpecs.filter(spec => spec.category === category);
  }

  /**
   * Get category ID for accessibility
   */
  getCategoryId(category: string): string {
    return category.toLowerCase().replace(/\s+/g, '-');
  }

  /**
   * Get default specifications for asset type
   */
  private getDefaultSpecsForAssetType(assetType: AssetType): TechnicalSpec[] {
    const defaultSpecs: Record<AssetType, TechnicalSpec[]> = {
      [AssetType.SERVER]: [
        { label: 'CPU', value: 'Not specified', icon: 'memory', category: 'Hardware' },
        { label: 'RAM', value: 'Not specified', icon: 'storage', unit: 'GB', category: 'Hardware' },
        { label: 'Storage', value: 'Not specified', icon: 'hard_drive', unit: 'TB', category: 'Hardware' },
        { label: 'OS', value: 'Not specified', icon: 'computer', category: 'Software' },
        { label: 'Network', value: 'Not specified', icon: 'network_check', category: 'Network' }
      ],
      [AssetType.WORKSTATION]: [
        { label: 'CPU', value: 'Not specified', icon: 'memory', category: 'Hardware' },
        { label: 'RAM', value: 'Not specified', icon: 'storage', unit: 'GB', category: 'Hardware' },
        { label: 'Graphics', value: 'Not specified', icon: 'videocam', category: 'Hardware' },
        { label: 'OS', value: 'Not specified', icon: 'computer', category: 'Software' }
      ],
      [AssetType.LAPTOP]: [
        { label: 'CPU', value: 'Not specified', icon: 'memory', category: 'Hardware' },
        { label: 'RAM', value: 'Not specified', icon: 'storage', unit: 'GB', category: 'Hardware' },
        { label: 'Screen Size', value: 'Not specified', icon: 'monitor', unit: 'inch', category: 'Display' },
        { label: 'Battery', value: 'Not specified', icon: 'battery_full', unit: 'Wh', category: 'Power' }
      ],
      [AssetType.MONITOR]: [
        { label: 'Size', value: 'Not specified', icon: 'monitor', unit: 'inch', category: 'Display' },
        { label: 'Resolution', value: 'Not specified', icon: 'high_quality', category: 'Display' },
        { label: 'Panel Type', value: 'Not specified', icon: 'display_settings', category: 'Display' },
        { label: 'Refresh Rate', value: 'Not specified', icon: 'speed', unit: 'Hz', category: 'Display' }
      ],
      [AssetType.NETWORK_DEVICE]: [
        { label: 'Ports', value: 'Not specified', icon: 'settings_ethernet', category: 'Network' },
        { label: 'Speed', value: 'Not specified', icon: 'speed', unit: 'Gbps', category: 'Network' },
        { label: 'Protocol', value: 'Not specified', icon: 'router', category: 'Network' },
        { label: 'Management', value: 'Not specified', icon: 'settings', category: 'Management' }
      ],
      [AssetType.STORAGE_DEVICE]: [
        { label: 'Capacity', value: 'Not specified', icon: 'storage', unit: 'TB', category: 'Storage' },
        { label: 'Type', value: 'Not specified', icon: 'hard_drive', category: 'Storage' },
        { label: 'Interface', value: 'Not specified', icon: 'cable', category: 'Connectivity' },
        { label: 'Speed', value: 'Not specified', icon: 'speed', unit: 'MB/s', category: 'Performance' }
      ],
      [AssetType.SOFTWARE_LICENSE]: [
        { label: 'Version', value: 'Not specified', icon: 'info', category: 'Software' },
        { label: 'License Type', value: 'Not specified', icon: 'key', category: 'Licensing' },
        { label: 'Users', value: 'Not specified', icon: 'people', category: 'Licensing' },
        { label: 'Expiry', value: 'Not specified', icon: 'schedule', category: 'Licensing' }
      ],
      [AssetType.PERIPHERAL]: [
        { label: 'Interface', value: 'Not specified', icon: 'cable', category: 'Connectivity' },
        { label: 'Compatibility', value: 'Not specified', icon: 'check_circle', category: 'Compatibility' }
      ],
      [AssetType.KEYBOARD]: [
        { label: 'Layout', value: 'Not specified', icon: 'keyboard', category: 'Input' },
        { label: 'Connection', value: 'Not specified', icon: 'cable', category: 'Connectivity' },
        { label: 'Backlight', value: 'Not specified', icon: 'lightbulb', category: 'Features' }
      ],
      [AssetType.MOUSE]: [
        { label: 'Type', value: 'Not specified', icon: 'mouse', category: 'Input' },
        { label: 'DPI', value: 'Not specified', icon: 'tune', category: 'Performance' },
        { label: 'Connection', value: 'Not specified', icon: 'cable', category: 'Connectivity' }
      ],
      [AssetType.HEADSET]: [
        { label: 'Type', value: 'Not specified', icon: 'headset', category: 'Audio' },
        { label: 'Microphone', value: 'Not specified', icon: 'mic', category: 'Audio' },
        { label: 'Connection', value: 'Not specified', icon: 'cable', category: 'Connectivity' }
      ],
      [AssetType.LAPTOP_CHARGER]: [
        { label: 'Wattage', value: 'Not specified', icon: 'power', unit: 'W', category: 'Power' },
        { label: 'Connector', value: 'Not specified', icon: 'cable', category: 'Connectivity' },
        { label: 'Compatibility', value: 'Not specified', icon: 'check_circle', category: 'Compatibility' }
      ],
      [AssetType.HDMI_CABLE]: [
        { label: 'Version', value: 'Not specified', icon: 'cable', category: 'Specifications' },
        { label: 'Length', value: 'Not specified', icon: 'straighten', unit: 'm', category: 'Physical' },
        { label: 'Resolution', value: 'Not specified', icon: 'high_quality', category: 'Performance' }
      ],
      [AssetType.NETWORK_CABLE]: [
        { label: 'Category', value: 'Not specified', icon: 'settings_ethernet', category: 'Specifications' },
        { label: 'Length', value: 'Not specified', icon: 'straighten', unit: 'm', category: 'Physical' },
        { label: 'Speed', value: 'Not specified', icon: 'speed', unit: 'Gbps', category: 'Performance' }
      ],
      [AssetType.ACCESS_CARD]: [
        { label: 'Type', value: 'Not specified', icon: 'badge', category: 'Security' },
        { label: 'Access Level', value: 'Not specified', icon: 'security', category: 'Security' },
        { label: 'Expiry', value: 'Not specified', icon: 'schedule', category: 'Validity' }
      ]
    };

    return defaultSpecs[assetType] || [];
  }
}