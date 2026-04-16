/**
 * Assets Component - Re-export of Asset Inventory Component
 * 
 * This component serves as a routing entry point that delegates to the
 * full-featured Asset Inventory component from module2-assets.
 */

import { Component } from '@angular/core';
import { AssetInventoryComponent } from '../module2-assets/components/asset-inventory/asset-inventory.component';

@Component({
  selector: 'app-assets',
  standalone: true,
  imports: [AssetInventoryComponent],
  template: `<app-asset-inventory></app-asset-inventory>`,
  styles: []
})
export class AssetsComponent {
}
