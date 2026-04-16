# Allocation Module Route Guards

This directory contains route guards for the Allocation Management module, implementing role-based access control for allocation operations.

## Guards

### AllocationGuard

**Purpose**: Protects write operations (assign, deallocate, statistics)

**Required Roles**: 
- ADMINISTRATOR
- ASSET_MANAGER

**Protected Routes**:
- `/allocation/assign/:id` - Assign asset to user or location
- `/allocation/deallocate/:id` - Deallocate asset
- `/allocation/statistics` - View assignment statistics

**Behavior**:
- If user is not authenticated → Redirect to `/login` with return URL
- If user lacks required role → Redirect to `/unauthorized`
- If user has required role → Allow access

**Usage**:
```typescript
{
  path: 'assign/:id',
  component: AllocationFormComponent,
  canActivate: [allocationGuard]
}
```

### ViewerGuard

**Purpose**: Protects read operations (history viewing)

**Required Roles**: 
- ADMINISTRATOR
- ASSET_MANAGER
- VIEWER

**Protected Routes**:
- `/allocation/history/:id` - View assignment history for asset

**Behavior**:
- If user is not authenticated → Redirect to `/login` with return URL
- If user lacks required role → Redirect to `/unauthorized`
- If user has required role → Allow access

**Usage**:
```typescript
{
  path: 'history/:id',
  component: AssignmentHistoryComponent,
  canActivate: [viewerGuard]
}
```

## Authorization Matrix

| Route | ADMINISTRATOR | ASSET_MANAGER | VIEWER |
|-------|---------------|---------------|--------|
| `/allocation/assign/:id` | ✅ | ✅ | ❌ |
| `/allocation/deallocate/:id` | ✅ | ✅ | ❌ |
| `/allocation/history/:id` | ✅ | ✅ | ✅ |
| `/allocation/statistics` | ✅ | ✅ | ❌ |

## Testing

Both guards have comprehensive unit tests covering:
- ✅ Access granted for authorized roles
- ✅ Access denied for unauthorized roles
- ✅ Redirect to login for unauthenticated users
- ✅ Redirect to unauthorized page for insufficient permissions
- ✅ Multiple roles handling

Run tests:
```bash
npm test -- --include='**/allocation/guards/*.spec.ts'
```

## Implementation Details

### Authentication Check
Guards first verify the user is authenticated by checking `authService.currentUserValue`. If null, the user is redirected to the login page with the attempted URL stored in query parameters for post-login redirection.

### Authorization Check
Guards check if the user's roles include any of the required roles using `Array.some()`. This allows users with multiple roles to access routes if they have at least one required role.

### Error Handling
- **Unauthenticated**: Redirect to `/login?returnUrl={attemptedUrl}`
- **Unauthorized**: Redirect to `/unauthorized`

### Integration with AuthService
Guards depend on the `AuthService` from `core/services/auth.service.ts` which provides:
- `currentUserValue`: Current authenticated user or null
- `isAuthenticated`: Boolean indicating authentication status
- User role information

## Security Considerations

1. **Client-Side Only**: These guards provide UI-level protection only. Backend API endpoints must also enforce authorization.

2. **Token Validation**: Guards rely on `AuthService` to validate JWT tokens and maintain user session.

3. **Role Synchronization**: User roles must be kept in sync between frontend and backend to prevent authorization bypass.

4. **Graceful Degradation**: Guards handle missing or invalid user data gracefully by redirecting to login.

## Future Enhancements

- [ ] Add permission-based guards for fine-grained access control
- [ ] Implement guard logging for security auditing
- [ ] Add support for temporary role elevation
- [ ] Create guard composition utilities for complex authorization rules
