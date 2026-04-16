# Data Seeding Implementation

## Overview

Implemented automatic data seeding for the development environment to provide default users for testing and development.

## Problem

The application was starting successfully but had no users in the database, causing login failures:
```
Login failed: User not found - rmurugan
```

## Solution

Created `DataInitializer.java` that automatically seeds the database with default users when running in development mode.

## Implementation Details

### File Created

**`backend/src/main/java/com/company/assetmanagement/config/DataInitializer.java`**

### Features

1. **Profile-Specific**: Only runs when `dev` profile is active
2. **Idempotent**: Checks if data exists before seeding (won't duplicate)
3. **Three Default Users**: Admin, Manager, and Viewer with different roles
4. **Secure Passwords**: Uses BCrypt password encoding
5. **Audit Trail**: Properly sets creation timestamps and relationships
6. **Logging**: Provides clear console output showing created users

### Default Users

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| `admin` | `Admin@123456` | ADMINISTRATOR | Full system access, user management, all operations |
| `manager` | `Manager@123456` | ASSET_MANAGER | Asset management, create/edit/delete assets |
| `viewer` | `Viewer@123456` | VIEWER | Read-only access, view assets and reports |

### How It Works

1. **Startup Trigger**: Runs automatically when Spring Boot starts with `dev` profile
2. **Check Existing Data**: Queries `userRepository.count()` to see if users exist
3. **Skip if Data Exists**: If users already exist, skips initialization
4. **Create Users**: If database is empty, creates three default users
5. **Assign Roles**: Creates UserRole entries linking users to their roles
6. **Log Results**: Outputs clear console messages showing what was created

### Console Output

When the application starts with an empty database, you'll see:

```
================================================================================
Initializing development data...
================================================================================
✓ Created admin user: username='admin', password='Admin@123456'
✓ Created manager user: username='manager', password='Manager@123456'
✓ Created viewer user: username='viewer', password='Viewer@123456'
================================================================================
Development data initialization complete!
================================================================================

Default Login Credentials:
  Administrator: admin / Admin@123456
  Asset Manager: manager / Manager@123456
  Viewer:        viewer / Viewer@123456

================================================================================
```

## Usage

### Starting with Seeded Data

```bash
# Start backend with dev profile
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Wait for initialization message in console
# Login with any of the default credentials
```

### Resetting Data

Since H2 is in-memory, simply restart the application:

```bash
# Stop the application (Ctrl+C)
# Start again
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Database is cleared and re-seeded automatically
```

### Testing Different Roles

1. **Test Administrator Access**:
   - Login: `admin` / `Admin@123456`
   - Can access all features
   - Can manage users
   - Can perform all operations

2. **Test Asset Manager Access**:
   - Login: `manager` / `Manager@123456`
   - Can manage assets
   - Cannot manage users
   - Limited to asset operations

3. **Test Viewer Access**:
   - Login: `viewer` / `Viewer@123456`
   - Read-only access
   - Cannot create/edit/delete
   - Can view assets and reports

## Security Considerations

### Development Only

- **Profile Restriction**: `@Profile("dev")` ensures this only runs in development
- **Never in Production**: Production should use proper user provisioning
- **Hardcoded Passwords**: Acceptable for dev, never for production

### Password Security

- **BCrypt Encoding**: All passwords are properly hashed using BCrypt
- **Strength 10**: Default BCrypt strength provides good security
- **No Plain Text**: Passwords are never stored in plain text

### Best Practices

1. **Change Passwords**: Change default passwords in production
2. **Disable Seeding**: Never enable dev profile in production
3. **Use Secrets Management**: Production should use environment variables
4. **Audit Trail**: All user creation is logged and tracked

## Production Deployment

For production, you should:

1. **Disable Data Seeding**: Use `prod` profile (no seeding)
2. **Manual User Creation**: Create admin user manually via SQL or admin tool
3. **Secure Passwords**: Use strong, unique passwords
4. **Environment Variables**: Store credentials in secure vault
5. **Flyway Migrations**: Use database migrations for schema management

### Production User Creation Example

```sql
-- Create admin user in production
INSERT INTO Users (Id, Username, PasswordHash, Email, IsActive, AccountLocked, FailedLoginAttempts, CreatedAt, UpdatedAt)
VALUES (
    NEWID(),
    'prodadmin',
    '$2a$10$[BCrypt_Hash_Here]',  -- Generate using BCrypt
    'admin@company.com',
    1,
    0,
    0,
    GETUTCDATE(),
    GETUTCDATE()
);

-- Assign administrator role
INSERT INTO UserRoles (Id, UserId, Role, AssignedBy, AssignedAt)
VALUES (
    NEWID(),
    (SELECT Id FROM Users WHERE Username = 'prodadmin'),
    'ADMINISTRATOR',
    (SELECT Id FROM Users WHERE Username = 'prodadmin'),
    GETUTCDATE()
);
```

## Troubleshooting

### Issue: Users not created

**Check**:
1. Verify dev profile is active: Look for "Initializing development data..." in logs
2. Check if users already exist: Data seeding skips if users exist
3. Verify database is empty: Restart application to clear H2 database

### Issue: Login still fails

**Check**:
1. Verify correct credentials: `admin` / `Admin@123456` (case-sensitive)
2. Check console logs: Look for user creation messages
3. Verify H2 console: Access `/h2-console` and query Users table
4. Check application logs: Look for authentication errors

### Issue: Want to add more users

**Option 1**: Modify `DataInitializer.java` and add more users
**Option 2**: Use the user management API after logging in as admin
**Option 3**: Insert directly via H2 console

## Files Modified

1. **Created**: `backend/src/main/java/com/company/assetmanagement/config/DataInitializer.java`
2. **Updated**: `QUICK_START_GUIDE.md` - Added default credentials table

## Next Steps

1. ✅ Restart backend application
2. ✅ Verify users are created (check console output)
3. ✅ Login with default credentials
4. ✅ Test different role permissions
5. 🎯 Start developing!

## Related Documentation

- `BACKEND_DATABASE_CONFIG_FIX.md` - Database configuration details
- `QUICK_START_GUIDE.md` - Complete setup instructions
- `.kiro/steering/it-asset-management-coding-standards.md` - Coding standards
