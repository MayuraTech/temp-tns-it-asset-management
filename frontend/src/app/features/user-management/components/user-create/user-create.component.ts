import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserFormComponent } from '../user-form/user-form.component';

/**
 * User Create Component
 * 
 * Wrapper component for creating new user accounts.
 * Delegates to UserFormComponent for form implementation.
 */
@Component({
  selector: 'app-user-create',
  standalone: true,
  imports: [CommonModule, UserFormComponent],
  template: `<app-user-form></app-user-form>`,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserCreateComponent {}
