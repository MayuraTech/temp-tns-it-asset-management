# User Form Component

## Overview

The `UserFormComponent` is a comprehensive form component for creating and editing user accounts in the IT Infrastructure Asset Management System. It implements the Editorial Geometry design system and provides a premium user experience with real-time validation, password strength indicators, and intuitive role selection.

## Features

### Core Functionality

- **Dual Mode Operation**: Automatically detects create vs. edit mode based on route parameters
- **Comprehensive Validation**: Real-time form validation with detailed error messages
- **Password Strength Indicator**: Visual feedback on password complexity
- **Role Selection**: Interactive role cards with multi-select capability
- **Error Handling**: Graceful error handling with user-friendly notifications
- **Responsive Design**: Adapts to mobile, tablet, and desktop viewports

### Form Fields

#### User Information Section
- **Username**: 3-100 characters, alphanumeric and underscores only
- **Email**: Valid email format, 5-255 characters

#### Security Credentials Section (Create Mode Only)
- **Password**: Minimum 8 characters with complexity requirements
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one digit
  - At least one special character
- **Confirm Password**: Must match password field
- **Password Strength Indicator**: Real-time visual feedback (Weak/Medium/Strong)

#### Role Assignment Section
- **Administrator**: Full system access and user management
- **Asset Manager**: Manage assets, tickets, and assignments
- **Viewer**: Read-only access to system data

## Usage

### Creating a New User

```typescript
// Navigate to create user page
this.router.navigate(['/user-management/create']);
```

The form will display in create mode with:
- All fields required
- Password fields visible
- Empty form ready for input

### Editing an Existing User

```typescript
// Navigate to edit user page
this.router.navigate(['/user-management', userId, 'edit']);
```

The form will display in edit mode with:
- User data pre-populated
- Password fields hidden (password changes handled separately)
- Username and email editable
- Roles editable

## Validation Rules

### Username
- **Required**: Yes
- **Min Length**: 3 characters
- **Max Length**: 100 characters
- **Pattern**: `^[a-zA-Z0-9_]+$` (alphanumeric and underscores only)
- **Error Messages**:
  - "Username is required"
  - "Username must be at least 3 characters"
  - "Username must not exceed 100 characters"
  - "Username can only contain letters, numbers, and underscores"

### Email
- **Required**: Yes
- **Min Length**: 5 characters
- **Max Length**: 255 characters
- **Format**: Valid email format
- **Error Messages**:
  - "Email is required"
  - "Please enter a valid email address"
  - "Email must be at least 5 characters"
  - "Email must not exceed 255 characters"

### Password (Create Mode)
- **Required**: Yes
- **Min Length**: 8 characters
- **Complexity**: Must contain uppercase, lowercase, digit, and special character
- **Error Messages**:
  - "Password is required"
  - "Password must be at least 8 characters"
  - "Password must contain at least one uppercase letter"
  - "Password must contain at least one lowercase letter"
  - "Password must contain at least one number"
  - "Password must contain at least one special character"

### Confirm Password
- **Required**: Yes
- **Match**: Must match password field
- **Error Messages**:
  - "Please confirm your password"
  - "Passwords do not match"

### Roles
- **Required**: Yes
- **Min Selection**: At least one role
- **Error Messages**:
  - "At least one role must be selected"

## Password Strength Calculation

The password strength indicator evaluates passwords based on:

1. **Length**:
   - 8+ characters: +1 point
   - 12+ characters: +1 point
   - 16+ characters: +1 point

2. **Character Types**:
   - Lowercase letters: +1 point
   - Uppercase letters: +1 point
   - Digits: +1 point
   - Special characters: +1 point

3. **Variety**:
   - 70%+ unique characters: +1 point

**Strength Levels**:
- **Weak** (0-3 points): Red indicator, "Weak - Add more complexity"
- **Medium** (4-6 points): Orange indicator, "Medium - Consider adding more characters"
- **Strong** (7+ points): Green indicator, "Strong - Good password!"

## API Integration

### Create User

```typescript
POST /api/v1/users
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "roles": ["ASSET_MANAGER"]
}
```

**Success Response** (201 Created):
```typescript
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "isActive": true,
  "accountLocked": false,
  "roles": ["ASSET_MANAGER"],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses**:
- `400 Bad Request`: Validation errors
- `409 Conflict`: Duplicate username or email
- `403 Forbidden`: Insufficient permissions

### Update User

```typescript
PUT /api/v1/users/{id}
{
  "username": "johndoe",
  "email": "john.doe@example.com"
}
```

**Success Response** (200 OK):
```typescript
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "isActive": true,
  "accountLocked": false,
  "roles": ["ASSET_MANAGER"],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T14:30:00Z"
}
```

### Role Management

Roles are managed separately through dedicated endpoints:

```typescript
// Assign role
POST /api/v1/users/{id}/roles
{ "role": "ADMINISTRATOR" }

// Revoke role
DELETE /api/v1/users/{id}/roles/{role}
```

## Editorial Geometry Design System

### Color Palette

- **Primary**: `#143b7d` (Blue 800) - Primary buttons, active states
- **Secondary**: `#a9371d` (Red-Orange) - Page titles, editorial accents
- **Surface**: `#faf9ff` (Light purple) - Base background
- **Surface Container**: `#eeedf4` - Form container background
- **Surface Container Lowest**: `#ffffff` - Card backgrounds

### Typography

- **Headings**: Manrope (geometric precision)
- **Body Text**: Inter (readability)
- **Page Title**: 30px, -0.75px letter-spacing
- **Section Title**: 20px, bold
- **Body Text**: 16px (large), 14px (medium)

### Spacing

- **Section Margins**: 40px (xxxl)
- **Field Gaps**: 24px (xl)
- **Card Padding**: 24px (xl)
- **Button Padding**: 12px 24px

### Shadows

- **Form Container**: `0 20px 40px rgba(20, 59, 125, 0.06)`
- **Buttons**: `0px 10px 15px -3px rgba(20, 59, 125, 0.2)`
- **Role Cards (Hover)**: `0 10px 20px rgba(20, 59, 125, 0.1)`

## Accessibility

### Keyboard Navigation

- All form fields are keyboard accessible
- Tab order follows logical flow
- Enter key submits form
- Escape key cancels (when implemented)

### Screen Reader Support

- Proper ARIA labels on all inputs
- Error messages associated with fields
- Role descriptions provided
- Button states announced

### Visual Accessibility

- **Color Contrast**: WCAG AA compliant (4.5:1 minimum)
- **Focus Indicators**: 2px solid outline with 2px offset
- **High Contrast Mode**: Enhanced borders (3px)
- **Reduced Motion**: Animations disabled when preferred

### Form Accessibility

- Explicit labels for all inputs
- Error messages clearly associated with fields
- Required fields indicated
- Password visibility toggles with ARIA labels

## Testing

### Unit Tests

Test coverage should include:

1. **Form Initialization**
   - Form creates with correct validators
   - Edit mode loads user data
   - Create mode shows password fields

2. **Validation**
   - Username validation rules
   - Email validation rules
   - Password complexity validation
   - Password match validation
   - Role selection validation

3. **Password Strength**
   - Weak password detection
   - Medium password detection
   - Strong password detection

4. **Form Submission**
   - Create user success
   - Update user success
   - Error handling
   - Role updates

5. **User Interactions**
   - Role selection toggle
   - Password visibility toggle
   - Cancel confirmation

### E2E Tests

```typescript
describe('User Form', () => {
  it('should create a new user', () => {
    cy.visit('/user-management/create');
    cy.get('[data-cy=username-input]').type('testuser');
    cy.get('[data-cy=email-input]').type('test@example.com');
    cy.get('[data-cy=password-input]').type('SecurePass123!');
    cy.get('[data-cy=confirm-password-input]').type('SecurePass123!');
    cy.get('[data-cy=role-ASSET_MANAGER]').click();
    cy.get('[data-cy=submit-btn]').click();
    cy.url().should('include', '/user-management/');
  });
});
```

## Requirements Mapping

This component satisfies the following requirements from the Module 1 - User Management spec:

### Requirement 4: User Account Creation
- **4.1**: Username uniqueness validation
- **4.2**: Email uniqueness validation
- **4.3**: Username format validation (alphanumeric + underscore)
- **4.4**: Email format validation
- **4.5**: Unique identifier generation (handled by backend)
- **4.6**: Account status set to active by default
- **4.7**: Failed login attempts set to zero
- **4.8**: Creation timestamp and creator tracking

### Requirement 6: User Account Update
- **6.1**: Email uniqueness validation on update
- **6.2**: Username uniqueness validation on update
- **6.3**: User account field updates
- **6.4**: Update timestamp and updater tracking
- **6.5**: Not found error handling
- **6.6**: Password field update prevention (separate endpoint)

## Future Enhancements

1. **Password Change in Edit Mode**: Add optional password change section
2. **Profile Picture Upload**: Add avatar upload functionality
3. **Additional Fields**: Department, employee ID, phone number
4. **Bulk Role Assignment**: Select multiple users for role changes
5. **Form Autosave**: Save draft changes automatically
6. **Validation Hints**: Show validation requirements before errors
7. **Password Generator**: Suggest strong passwords
8. **Username Availability Check**: Real-time username availability
9. **Email Verification**: Send verification email on creation
10. **Audit Trail**: Show user modification history

## Troubleshooting

### Common Issues

**Issue**: Form doesn't submit
- **Solution**: Check browser console for validation errors. Ensure all required fields are filled and valid.

**Issue**: Password strength not updating
- **Solution**: Verify password field has value. Check browser console for JavaScript errors.

**Issue**: Roles not saving
- **Solution**: Ensure at least one role is selected. Check network tab for API errors.

**Issue**: Edit mode not loading user data
- **Solution**: Verify user ID in route parameters. Check API response in network tab.

## Related Components

- **UserListComponent**: Displays list of users with link to create/edit
- **UserDetailComponent**: Shows user details with edit button
- **UserProfileComponent**: User self-service profile management
- **UserService**: API service for user operations

## References

- [Requirements Document](../../../../../.kiro/specs/module1-user-management/requirements.md)
- [Design Document](../../../../../.kiro/specs/module1-user-management/design.md)
- [Tasks Document](../../../../../.kiro/specs/module1-user-management/tasks.md)
- [Editorial Geometry UI Standards](../../../../../.kiro/steering/editorial-geometry-ui-standards.md)
