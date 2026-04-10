import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LayoutModule } from './core/layout/layout.module';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, LayoutModule],
  template: `
    <app-shell></app-shell>
  `,
  styles: []
})
export class AppComponent {
  title = 'IT Infrastructure Asset Management';
}
