# E2E Tests - IT Asset Management

This directory contains End-to-End (E2E) tests for the IT Infrastructure Asset Management application using Cypress.

## Overview

The E2E tests verify complete user workflows from the browser perspective, ensuring that all components work together correctly.

## Test Structure

```
cypress/
├── e2e/                    # E2E test specs
│   └── allocation/         # Allocation management tests
│       └── assignment-history.cy.ts
├── support/                # Support files and custom commands
│   ├── commands.ts         # Custom Cypress commands
│   └── e2e.ts             # E2E support file
└── README.md              # This file
```

## Running E2E Tests

### Prerequisites

1. **Backend must be running**: Start the Spring Boot backend on `http://localhost:8080`
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

2. **Frontend must be running**: Start the Angular dev server on `http://localhost:4200`
   ```bash
   cd frontend
   npm start
   ```

3. **Database must be seeded**: Ensure test data exists in the database

### Run All E2E Tests

```bash
# Run all tests in headless mode
npm run e2e

# Run all tests in interactive mode (Cypress UI)
npm run e2e:open

# Run all tests in Chrome browser
npm run e2e:chrome
```

### Run Specific Test Suites

```bash
# Run only allocation tests
npm run e2e:allocation

# Run specific test file
npx cypress run --spec 'cypress/e2e/allocation/assignment-history.cy.ts'
```

## Test Coverage

### Assignment History E2E Tests

**File**: `cypress/e2e/allocation/assignment-history.cy.ts`

**Test Scenarios**:

1. **Navigate to Asset Detail**
   - Navigate from assets list to asset detail page
   - Navigate directly via URL
   - Display asset information
   - Show assignment history section

2. **View Assignment History**
   - Display assignment history table
   - Display assignment history records
   - Display assignment type correctly
   - Display assigned to information
   - Display assigned by username
   - Display assignment dates
   - Display assignment status (Active/Historical)
   - Distinguish between active and historical assignments

3. **Verify History Display**
   - Display history in chronological order (most recent first)
   - Display loading state while fetching history
   - Display empty state when no history exists
   - Display error message when history loading fails
   - Support pagination for large history
   - Display assignment type badges with correct colors
   - Display status badges with correct colors
   - Format dates in readable format

4. **Complete Workflow**
   - Navigate from assets list to viewing history (full workflow)

5. **Authorization**
   - Allow ADMINISTRATOR to view assignment history
   - Allow ASSET_MANAGER to view assignment history
   - Allow VIEWER to view assignment history

## Custom Commands

### `cy.login(username, password)`

Authenticates a user and stores the JWT token.

**Example**:
# User Management E2E Tests

This directory contains end-to-end (E2E) tests for the User Management module using Cypress.

## Directory Structure

```
cypress/
├── e2e/
│   └── user-management/
│       ├── authentication.cy.ts       # Authentication flow tests
│       ├── user-crud.cy.ts           # User CRUD operation tests
│       ├── role-management.cy.ts     # Role management tests
│       ├── profile-management.cy.ts  # Profile management tests
│       └── error-handling.cy.ts      # Error handling and edge cases
├── support/
│   ├── commands.ts                   # Custom Cypress commands
│   └── e2e.ts                        # Support file configuration
└── README.md                         # This file
```

## Test Suites

### 1. Authentication Tests (`authentication.cy.ts`)

Tests user authentication flows including:
- Login with valid/invalid credentials
- Account locking after failed attempts
- Logout functionality
- Token refresh mechanism
- Protected route access control

**Requirements Covered**: 1.1-1.8, 2.1-2.5, 13.1-13.5

### 2. User CRUD Tests (`user-crud.cy.ts`)

Tests user management operations including:
- User creation with validation
- User listing with pagination and filtering
- User detail viewing
- User updates
- User deletion with safeguards
- Account status management

**Requirements Covered**: 4.1-4.8, 5.1-5.6, 6.1-6.6, 7.1-7.5, 8.1-8.5

### 3. Role Management Tests (`role-management.cy.ts`)

Tests role assignment and revocation including:
- Role assignment to users
- Role revocation with business rules
- Role-based access control
- Session invalidation on role changes

**Requirements Covered**: 9.1-9.5, 10.1-10.5, 12.1-12.7

### 4. Profile Management Tests (`profile-management.cy.ts`)

Tests user profile self-service including:
- Profile viewing and updating
- Password change functionality
- Email validation and uniqueness
- Responsive design
- Accessibility features

**Requirements Covered**: 11.1-11.6, 3.1-3.6

### 5. Error Handling Tests (`error-handling.cy.ts`)

Tests error scenarios and edge cases including:
- Network error handling
- Validation error display
- Authorization errors
- Loading states
- Session management

**Requirements Covered**: 14.1-14.4

## Prerequisites

Before running E2E tests, ensure:

1. **Backend Server** is running on `http://localhost:8080`
2. **Frontend Server** is running on `http://localhost:4200`
3. **Database** is set up with test data
4. **Node.js** and **npm** are installed

## Running Tests

### Quick Start

```bash
# From frontend directory
npm run e2e
```

### Using Helper Scripts

**Linux/Mac**:
```bash
chmod +x run-e2e-tests.sh
./run-e2e-tests.sh
```

**Windows**:
```cmd
run-e2e-tests.bat
```

### Interactive Mode

Run tests with Cypress UI for debugging:

```bash
# Linux/Mac
./run-e2e-tests.sh --interactive

# Windows
run-e2e-tests.bat --interactive

# Or directly
npx cypress open
```

### Run Specific Test Suite

```bash
# Authentication tests only
npx cypress run --spec "cypress/e2e/user-management/authentication.cy.ts"

# User CRUD tests only
npx cypress run --spec "cypress/e2e/user-management/user-crud.cy.ts"

# Role management tests only
npx cypress run --spec "cypress/e2e/user-management/role-management.cy.ts"

# Profile management tests only
npx cypress run --spec "cypress/e2e/user-management/profile-management.cy.ts"

# Error handling tests only
npx cypress run --spec "cypress/e2e/user-management/error-handling.cy.ts"
```

### Run with Different Browsers

```bash
# Chrome (default)
npx cypress run --browser chrome

# Firefox
npx cypress run --browser firefox

# Edge
npx cypress run --browser edge
```

### Headless Mode

```bash
npx cypress run --headless
```

## Custom Commands

The test suite includes custom Cypress commands for common operations:

### `cy.login(username, password)`

Authenticates a user and stores tokens in localStorage.

```typescript
cy.login('admin', 'Admin@123456');
```

### `cy.logout()`

Clears authentication tokens from localStorage.

```typescript
cy.logout();
```

## Data Attributes for Testing

The E2E tests use `data-cy` attributes to select elements. Ensure the following attributes are present in the components:

### Asset List Page
- `data-cy="asset-list"` - Asset list container
- `data-cy="asset-row"` - Individual asset row

### Asset Detail Page
- `data-cy="asset-detail"` - Asset detail container
- `data-cy="asset-name"` - Asset name field
- `data-cy="asset-serial-number"` - Serial number field
- `data-cy="asset-type"` - Asset type field
- `data-cy="asset-status"` - Asset status field

### Assignment History Section
- `data-cy="assignment-history-section"` - History section container
- `data-cy="assignment-history-table"` - History table
- `data-cy="history-row"` - Individual history row
- `data-cy="assignment-type"` - Assignment type badge
- `data-cy="assigned-to"` - Assigned to field
- `data-cy="assigned-by"` - Assigned by field
- `data-cy="assigned-at"` - Assigned at date
- `data-cy="unassigned-at"` - Unassigned at date
- `data-cy="assignment-status"` - Status badge
- `data-cy="history-loading"` - Loading indicator
- `data-cy="history-empty-state"` - Empty state message
- `data-cy="history-error"` - Error message
- `data-cy="history-paginator"` - Pagination controls

## Best Practices

1. **Use data-cy attributes**: Always use `data-cy` attributes for element selection instead of classes or IDs
2. **Wait for elements**: Use Cypress's built-in retry logic with `.should()` assertions
3. **Intercept API calls**: Use `cy.intercept()` to mock API responses for edge cases
4. **Clean up after tests**: Use `afterEach()` to logout and clean up state
5. **Test user workflows**: Focus on complete user journeys, not individual components
6. **Handle async operations**: Use `cy.wait()` for intercepted requests
7. **Verify visual feedback**: Check for loading states, error messages, and success notifications

## Debugging

### Run Tests in Interactive Mode

```bash
npm run e2e:open
```

This opens the Cypress Test Runner where you can:
- See tests running in real-time
- Inspect DOM at each step
- View network requests
- Debug test failures

### View Screenshots and Videos

Failed tests automatically capture screenshots:
- Screenshots: `cypress/screenshots/`
- Videos: `cypress/videos/` (if enabled)

### Enable Video Recording

Update `cypress.config.ts`:
```typescript
export default defineConfig({
  e2e: {
    video: true,  // Enable video recording
    // ...
  },
});
```

## Continuous Integration
## Test Data

Tests require the following test users in the database:

| Username | Password | Roles | Status |
|----------|----------|-------|--------|
| admin | Admin@123456 | ADMINISTRATOR | Active |
| assetmanager | AssetManager@123 | ASSET_MANAGER | Active |
| viewer | Viewer@123 | VIEWER | Active |
| testuser | TestUser@123 | VIEWER | Active |
| disableduser | Password@123 | VIEWER | Inactive |
| multiuser | MultiUser@123 | VIEWER, ASSET_MANAGER | Active |
| singleuser | SingleUser@123 | VIEWER | Active |

## Test Selectors

Tests use `data-cy` attributes for element selection:

```html
<!-- Example -->
<button data-cy="login-button">Login</button>
<input data-cy="username-input" />
<div data-cy="error-message">Error text</div>
```

This approach provides:
- Stable selectors independent of styling
- Clear test intent
- Easy maintenance

## Configuration

### Cypress Configuration (`cypress.config.ts`)

```typescript
{
  e2e: {
    baseUrl: 'http://localhost:4200',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    video: false,
    screenshotOnRunFailure: true,
    viewportWidth: 1280,
    viewportHeight: 720,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 10000
  }
}
```

### Environment Variables

Create `cypress.env.json` for environment-specific configuration:

```json
{
  "apiUrl": "http://localhost:8080",
  "frontendUrl": "http://localhost:4200"
}
```

## Debugging Tests

### Interactive Mode

Use Cypress UI for step-by-step debugging:

```bash
npx cypress open
```

Features:
- Time travel through test steps
- DOM snapshots at each step
- Network request inspection
- Console log viewing

### Screenshots

Failed tests automatically capture screenshots:

```
cypress/screenshots/
└── user-management/
    └── authentication.cy.ts/
        └── should successfully login -- failed.png
```

### Videos

Enable video recording in `cypress.config.ts`:

```typescript
{
  e2e: {
    video: true
  }
}
```

Videos are saved to `cypress/videos/`.

## Best Practices

1. **Test Independence**: Each test should be independent and not rely on other tests
2. **Data Cleanup**: Use `afterEach` hooks to clean up test data
3. **Explicit Waits**: Use `cy.wait()` for API calls, not arbitrary timeouts
4. **Descriptive Names**: Use clear, descriptive test names
5. **Custom Commands**: Extract common operations into custom commands
6. **Selectors**: Use `data-cy` attributes for stable selectors
7. **Assertions**: Use specific assertions with meaningful messages

## Troubleshooting

### Backend Not Running

```
Error: Backend is not running on port 8080
```

**Solution**: Start the backend server:
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Not Running

```
Error: Frontend is not running on port 4200
```

**Solution**: Start the frontend server:
```bash
cd frontend
npm start
```

### Test Failures

1. Check backend and frontend are running
2. Verify test data exists in database
3. Check console logs for errors
4. Review screenshots in `cypress/screenshots/`
5. Run tests in interactive mode for debugging

### Timeout Errors

If tests timeout:
1. Increase timeout in `cypress.config.ts`
2. Check network connectivity
3. Verify API endpoints are responding
4. Check for slow database queries

## CI/CD Integration

### GitHub Actions Example

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  e2e:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Start Backend
        run: |
          cd backend
          ./mvnw spring-boot:run &
          sleep 30
      
      - name: Start Frontend
        run: |
          cd frontend
          npm ci
          npm start &
          sleep 30
      
      - name: Run E2E Tests
        run: |
          cd frontend
          npx cypress run --headless
      
      - name: Upload Screenshots
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: cypress-screenshots
          path: frontend/cypress/screenshots
```

## Coverage Report

After running tests, view coverage in:
- Console output
- `cypress/results/` (if configured)
- CI/CD pipeline reports

## Contributing

When adding new E2E tests:

1. Follow existing test structure
2. Use descriptive test names
3. Add `data-cy` attributes to new UI elements
4. Update this README with new test suites
5. Ensure tests are independent
6. Add proper cleanup in `afterEach` hooks

## Resources

- [Cypress Documentation](https://docs.cypress.io/)
- [Best Practices](https://docs.cypress.io/guides/references/best-practices)
- [Custom Commands](https://docs.cypress.io/api/cypress-api/custom-commands)
- [Assertions](https://docs.cypress.io/guides/references/assertions)

## Support

For issues or questions:
1. Check this README
2. Review test output and screenshots
3. Check Cypress documentation
4. Contact the development team
