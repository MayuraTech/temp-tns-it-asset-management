import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { AssetType } from '../../../models';

/**
 * Asset Icon Component
 * 
 * Displays appropriate Material Design icons for all 15 asset types.
 * Provides consistent iconography across the application.
 * 
 * Features:
 * - Material Design icons for all asset types
 * - Configurable size (small, medium, large)
 * - Accessible with proper ARIA labels
 * - Consistent styling with Editorial Geometry design system
 */
@Component({
  selector: 'app-asset-icon',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <mat-icon 
      class="asset-icon"
      [class]="getSizeClass(size)"
      [attr.aria-label]="getAriaLabel(assetType)"
      role="img">
      {{ getIconName(assetType) }}
    </mat-icon>
  `,
  styles: [`
    .asset-icon {
      color: var(--primary, #143b7d);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      transition: color 0.2s ease;
    }

    /* Size variants */
    .size-small {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .size-medium {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .size-large {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .size-xlarge {
      font-size: 32px;
      width: 32px;
      height: 32px;
    }

    /* Interactive states */
    .asset-icon:hover {
      color: var(--primary-container, #315396);
    }

    /* High contrast mode support */
    @media (prefers-contrast: high) {
      .asset-icon {
        filter: contrast(1.2);
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssetIconComponent {
  @Input() assetType!: AssetType;
  @Input() size: 'small' | 'medium' | 'large' | 'xlarge' = 'medium';

  /**
   * Get Material Design icon name for asset type
   */
  getIconName(type: AssetType): string {
    const iconMap: Record<AssetType, string> = {
      [AssetType.SERVER]: 'dns',
      [AssetType.WORKSTATION]: 'computer',
      [AssetType.NETWORK_DEVICE]: 'router',
      [AssetType.STORAGE_DEVICE]: 'storage',
      [AssetType.SOFTWARE_LICENSE]: 'key',
      [AssetType.PERIPHERAL]: 'devices_other',
      [AssetType.KEYBOARD]: 'keyboard',
      [AssetType.MOUSE]: 'mouse',
      [AssetType.LAPTOP]: 'laptop',
      [AssetType.MONITOR]: 'monitor',
      [AssetType.HEADSET]: 'headset',
      [AssetType.LAPTOP_CHARGER]: 'power',
      [AssetType.HDMI_CABLE]: 'cable',
      [AssetType.NETWORK_CABLE]: 'settings_ethernet',
      [AssetType.ACCESS_CARD]: 'badge'
    };
    return iconMap[type] || 'inventory_2';
  }

  /**
   * Get CSS class for icon size
   */
  getSizeClass(size: string): string {
    return `size-${size}`;
  }

  /**
   * Get accessible aria-label for the icon
   */
  getAriaLabel(type: AssetType): string {
    const labelMap: Record<AssetType, string> = {
      [AssetType.SERVER]: 'Server icon',
      [AssetType.WORKSTATION]: 'Workstation icon',
      [AssetType.NETWORK_DEVICE]: 'Network device icon',
      [AssetType.STORAGE_DEVICE]: 'Storage device icon',
      [AssetType.SOFTWARE_LICENSE]: 'Software license icon',
      [AssetType.PERIPHERAL]: 'Peripheral device icon',
      [AssetType.KEYBOARD]: 'Keyboard icon',
      [AssetType.MOUSE]: 'Mouse icon',
      [AssetType.LAPTOP]: 'Laptop icon',
      [AssetType.MONITOR]: 'Monitor icon',
      [AssetType.HEADSET]: 'Headset icon',
      [AssetType.LAPTOP_CHARGER]: 'Laptop charger icon',
      [AssetType.HDMI_CABLE]: 'HDMI cable icon',
      [AssetType.NETWORK_CABLE]: 'Network cable icon',
      [AssetType.ACCESS_CARD]: 'Access card icon'
    };
    return labelMap[type] || 'Asset icon';
  }
}