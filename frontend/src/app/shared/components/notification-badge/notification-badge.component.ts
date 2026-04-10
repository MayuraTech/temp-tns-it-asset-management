import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-notification-badge',
  standalone: true,
  imports: [
    CommonModule,
    MatBadgeModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './notification-badge.component.html',
  styleUrls: ['./notification-badge.component.scss']
})
export class NotificationBadgeComponent {
  @Input() count = 0;
  @Input() max = 99;
  @Input() icon = 'notifications';
  @Input() color: 'primary' | 'accent' | 'warn' = 'warn';

  get displayCount(): string {
    if (this.count > this.max) {
      return `${this.max}+`;
    }
    return this.count.toString();
  }

  get isHidden(): boolean {
    return this.count === 0;
  }
}
