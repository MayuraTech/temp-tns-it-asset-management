/**
 * UserControlsComponent - User Controls with Geometric Hover Effects
 * 
 * Implements notification, settings, and user avatar components with
 * geometric hover effects and editorial transitions following
 * Editorial Geometry principles.
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 21.7
 */

import { Component, Output, EventEmitter, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

export type UserControlAction = 'notification' | 'settings' | 'profile';

export interface UserInfo {
  name: string;
  email: string;
  avatarUrl?: string;
  initials?: string;
}

@Component({
  selector: 'app-user-controls',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-controls.component.html',
  styleUrls: ['./user-controls.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserControlsComponent {
  
  /**
   * User information for avatar display
   */
  @Input() userInfo: UserInfo = {
    name: 'User',
    email: 'user@example.com',
    initials: 'U'
  };
  
  /**
   * Number of unread notifications
   */
  @Input() notificationCount: number = 0;
  
  /**
   * Event emitted when a user control is clicked
   * Requirements 7.6, 7.7, 7.8
   */
  @Output() controlClick = new EventEmitter<UserControlAction>();
  
  /**
   * Handle notification button click
   * Requirement 7.6
   */
  onNotificationClick(): void {
    this.controlClick.emit('notification');
  }
  
  /**
   * Handle settings button click
   * Requirement 7.7
   */
  onSettingsClick(): void {
    this.controlClick.emit('settings');
  }
  
  /**
   * Handle user avatar click
   * Requirement 7.8
   */
  onProfileClick(): void {
    this.controlClick.emit('profile');
  }
  
  /**
   * Get user initials for avatar display
   */
  getUserInitials(): string {
    if (this.userInfo.initials) {
      return this.userInfo.initials;
    }
    
    const nameParts = this.userInfo.name.split(' ');
    if (nameParts.length >= 2) {
      return `${nameParts[0][0]}${nameParts[1][0]}`.toUpperCase();
    }
    
    return this.userInfo.name.substring(0, 2).toUpperCase();
  }
}
