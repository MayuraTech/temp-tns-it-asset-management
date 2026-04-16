import { Role } from '../../../core/models/auth.model';

/**
 * User Management DTOs and Interfaces
 * 
 * Defines data transfer objects for user management operations
 * aligned with backend API contracts
 */

/**
 * User DTO - Complete user information
 */
export interface UserDTO {
  id: string;
  username: string;
  email: string;
  isActive: boolean;
  accountLocked: boolean;
  lockUntil?: Date;
  lastLoginAt?: Date;
  roles: Role[];
  createdAt: Date;
  updatedAt: Date;
  createdBy?: string;
  updatedBy?: string;
}

/**
 * User creation request
 */
export interface UserRequest {
  username: string;
  email: string;
  password: string;
  roles: Role[];
}

/**
 * User update request
 */
export interface UserUpdateRequest {
  email?: string;
  username?: string;
}

/**
 * Profile update request
 */
export interface ProfileUpdateRequest {
  email?: string;
}

/**
 * Password change request
 */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

/**
 * Role assignment request
 */
export interface RoleAssignmentRequest {
  role: Role;
}

/**
 * User list filter options
 */
export interface UserFilterOptions {
  role?: Role;
  isActive?: boolean;
  searchText?: string;
}

/**
 * User statistics
 */
export interface UserStatistics {
  totalUsers: number;
  activeUsers: number;
  lockedUsers: number;
  usersByRole: Map<Role, number>;
}
