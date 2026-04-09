import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-licenses',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page-container"><h1 class="page-title">Licenses</h1><p>License management will be displayed here.</p></div>`,
  styles: [`.page-container { padding: var(--space-xxl); } .page-title { font-family: var(--font-heading); font-size: var(--headline-lg); font-weight: 800; color: var(--secondary); }`],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LicensesComponent {}
