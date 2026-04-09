import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page-container"><h1 class="page-title">Inventory</h1><p>Inventory overview will be displayed here.</p></div>`,
  styles: [`.page-container { padding: var(--space-xxl); } .page-title { font-family: var(--font-heading); font-size: var(--headline-lg); font-weight: 800; color: var(--secondary); }`],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InventoryComponent {}
