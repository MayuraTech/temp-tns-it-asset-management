# User Management Feature Module

This module provides comprehensive user account lifecycle management for the IT Infrastructure Asset Management System.

## Overview

The User Management module implements:
- User CRUD operations (Create, Read, Update, Delete)
- Role-based access control management
- User profile self-service
- Account status management (enable/disable)
- Password management
- User filtering and pagination

## Module Structure

```
user-management/
├── components/              # UI components
│   ├── user-list/          # User list with filtering and pagination
│   ├── user-detail/        # User detail view
│   ├── user-create/        # User creation form
│   ├── user-edit/          # User editing form
│   ├── user-profile/       # Current user profile management
│   └── index.ts            # Component barrel export
├── services/               # Business logic services
│   ├── user.service.ts     # User management API service
│   ├── profile.service.ts  # Profile management API service
│   └── index.ts            # Service barrel export
├── models/                 # TypeScript interfaces and DTOs
│   ├── user.model.ts       # User-related interfaces
│   └── index.ts            # Model barrel export
├── user-management.routes.ts  # Feature routing configuration
├── index.ts                # Module barrel export
└── README.md               # This file
```

## Routing Configuration

The module uses lazy loading for optimal performance:

```typescript
// In app.routes.ts
{
  path: 'users',
  loadChildren: () => import('./features/user-management/user-management.routes')
    .then(m => m.userManagementRoutes)
}
```

### Available Routes

- `/users` - User list view (All authenticated users)
- `/users/create` - Create new user (Administrator only)
- `/users/profile` - Current user profile (All authenticated users)
- `/users/:id` - User detail view (Administrator, Asset Manager)
- `/users/:id/edit` - Edit user (Administrator only)

## Services

### UserService

Handles all user management operations:

```typescript
// Get paginated users with filtering
getUsers(page, size, filters): Observable<PageResponse<UserDTO>>

// Get single user
getUser(id): Observable<UserDTO>

// Create user
createUser(request): Observable<UserDTO>

// Update user
updateUser(id, request): Observable<UserDTO>

// Delete user
deleteUser(id): Observable<void>

// Enable/disable user
enableUser(id): Observable<void>
disableUser(id): Observable<void>

// Role management
assignRole(id, role): Observable<void>
revokeRole(id, role): Observable<void>
```

### ProfileService

Handles current user profile operations:

```typescript
// Get current user profile
getProfile(): Observable<UserDTO>

// Update profile
updateProfile(request): Observable<UserDTO>

// Change password
changePassword(request): Observable<void>
```

## Models

### UserDTO

Complete user information returned from API:

```typescript
interface UserDTO {
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
```

### UserRequest

User creation request:

```typescript
interface UserRequest {
  username: string;
  email: string;
  password: string;
  roles: Role[];
}
```

### UserUpdateRequest

User update request:

```typescript
interface UserUpdateRequest {
  email?: string;
  username?: string;
}
```

### ChangePasswordRequest

Password change request:

```typescript
interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
```

## Role-Based Access Control

The module enforces role-based permissions:

- **Administrator**: Full access to all user management operations
- **Asset Manager**: Can view users but cannot modify
- **Viewer**: Can only view and edit their own profile

## Security Features

- JWT-based authentication via `authGuard`
- Role-based authorization via `roleGuard`
- Password complexity validation
- Account locking after failed login attempts
- Session invalidation on password change
- Audit logging of all user management operations

## Integration with Core Services

The module integrates with:
- `AuthService` - For authentication state
- `AuthGuard` - For route protection
- `RoleGuard` - For role-based access control
- `JwtInterceptor` - For automatic token attachment
- `ErrorInterceptor` - For centralized error handling
- `LoadingInterceptor` - For loading state management

## API Endpoints

All endpoints are prefixed with `/api/v1`:

### User Management
- `GET /users` - List users with pagination
- `GET /users/:id` - Get user by ID
- `POST /users` - Create user
- `PUT /users/:id` - Update user
- `DELETE /users/:id` - Delete user
- `PATCH /users/:id/enable` - Enable user
- `PATCH /users/:id/disable` - Disable user
- `POST /users/:id/roles` - Assign role
- `DELETE /users/:id/roles/:role` - Revoke role

### Profile Management
- `GET /profile` - Get current user profile
- `PUT /profile` - Update profile
- `POST /profile/change-password` - Change password

## Design System Compliance

All components follow the Editorial Geometry design system:

- **Typography**: Manrope for headings, Inter for body text
- **Colors**: Primary (#143b7d), Secondary (#a9371d)
- **Spacing**: Editorial spacing scale (--space-*)
- **Surfaces**: Layered surface hierarchy
- **Components**: Material Design components with custom styling

## Testing

Unit tests should be created for:
- All service methods
- Component logic
- Form validation
- Route guards
- Error handling

## Future Enhancements

Planned features for future iterations:
- Bulk user operations
- User import/export
- Advanced filtering and search
- User activity timeline
- Role permission customization
- Two-factor authentication
- Password reset via email
