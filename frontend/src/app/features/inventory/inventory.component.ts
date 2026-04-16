/**
 * Inventory route entry — same experience as /assets (full asset inventory).
 */
import { Component } from '@angular/core';
import { AssetInventoryComponent } from '../module2-assets/components/asset-inventory/asset-inventory.component';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [AssetInventoryComponent],
  template: `<app-asset-inventory></app-asset-inventory>`
})
export class InventoryComponent {}
