/**
 * Authentication and authorization models
 */

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface User {
  id: string;
  username: string;
  email: string;
  roles: Role[];
  createdAt: Date;
  updatedAt: Date;
  lastLoginAt?: Date;
  accountLocked: boolean;
}

export enum Role {
  ADMINISTRATOR = 'Administrator',
  ASSET_MANAGER = 'Asset_Manager',
  VIEWER = 'Viewer'
}

export enum Action {
  CREATE_ASSET = 'CREATE_ASSET',
  UPDATE_ASSET = 'UPDATE_ASSET',
  DELETE_ASSET = 'DELETE_ASSET',
  VIEW_ASSET = 'VIEW_ASSET',
  MANAGE_USERS = 'MANAGE_USERS',
  VIEW_AUDIT_LOG = 'VIEW_AUDIT_LOG',
  EXPORT_DATA = 'EXPORT_DATA',
  IMPORT_DATA = 'IMPORT_DATA',
  CONFIGURE_SYSTEM = 'CONFIGURE_SYSTEM'
}
