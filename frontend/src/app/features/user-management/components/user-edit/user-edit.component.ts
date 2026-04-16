import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserFormComponent } from '../user-form/user-form.component';

/**
 * User Edit Component
 * 
 * Wrapper component for editing existing user accounts.
 * Delegates to UserFormComponent for form implementation.
 */
@Component({
  selector: 'app-user-edit',
  standalone: true,
  imports: [CommonModule, UserFormComponent],
  template: `<app-user-form></app-user-form>`,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserEditComponent {}
