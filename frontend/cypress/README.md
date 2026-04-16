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
```typescript
cy.login('admin', 'Admin@123456');
```

### `cy.logout()`

Clears authentication tokens and navigates to login page.

**Example**:
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
          
      - name: Start Frontend
        run: |
          cd frontend
          npm install
          npm start &
          
      - name: Wait for services
        run: |
          npx wait-on http://localhost:8080/actuator/health
          npx wait-on http://localhost:4200
          
      - name: Run E2E Tests
        run: |
          cd frontend
          npm run e2e:headless
          
      - name: Upload Screenshots
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: cypress-screenshots
          path: frontend/cypress/screenshots
```

## Troubleshooting

### Tests Fail with "Cannot GET /api/v1/..."

**Solution**: Ensure the backend is running on `http://localhost:8080`

### Tests Fail with "Timed out waiting for element"

**Solution**: 
1. Check if the frontend is running on `http://localhost:4200`
2. Increase timeout in test: `cy.get('[data-cy=element]', { timeout: 10000 })`
3. Verify the `data-cy` attribute exists in the component

### Authentication Fails

**Solution**:
1. Verify test user credentials exist in the database
2. Check JWT token configuration
3. Ensure CORS is configured correctly

### Cypress Binary Not Found

**Solution**:
```bash
npx cypress install
```

## Resources

- [Cypress Documentation](https://docs.cypress.io/)
- [Cypress Best Practices](https://docs.cypress.io/guides/references/best-practices)
- [Cypress API Reference](https://docs.cypress.io/api/table-of-contents)
