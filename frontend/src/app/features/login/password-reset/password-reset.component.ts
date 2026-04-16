import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

/**
 * Password Reset Component
 * 
 * Placeholder component for password reset functionality.
 * This component will be fully implemented in a future task.
 */
@Component({
  selector: 'app-password-reset',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './password-reset.component.html',
  styleUrls: ['./password-reset.component.scss']
})
export class PasswordResetComponent {
  constructor(private router: Router) {}

  /**
   * Navigate back to login page
   */
  goBackToLogin(): void {
    this.router.navigate(['/login']);
  }
}
