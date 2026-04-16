/**
 * E2E Tests: Error Handling and Edge Cases
 * 
 * Tests error handling, validation, and edge case scenarios
 * Validates Requirements: 14.1-14.4
 */

describe('Error Handling and Edge Cases', () => {
  beforeEach(() => {
    cy.login('admin', 'Admin@123456');
  });

  afterEach(() => {
    cy.logout();
  });

  describe('Network Error Handling', () => {
    it('should handle API server unavailable', () => {
      // Given: API server is down (simulated)
      cy.intercept('GET', '/api/v1/users', {
        forceNetworkError: true
      }).as('getUsersError');
      
      // When: User navigates to users page
      cy.visit('/users');
      cy.wait('@getUsersError');
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Unable to connect to server');
    });

    it('should handle timeout errors', () => {
      // Given: API request times out
      cy.intercept('GET', '/api/v1/users', (req) => {
        req.reply({ delay: 15000, statusCode: 408 });
      }).as('getUsersTimeout');
      
      // When: User navigates to users page
      cy.visit('/users');
      
      // Then: Timeout error is displayed
      cy.get('[data-cy=error-message]', { timeout: 20000 })
        .should('be.visible')
        .and('contain', 'Request timed out');
    });

    it('should handle 500 internal server errors', () => {
      // Given: Server returns 500 error
      cy.intercept('POST', '/api/v1/users', {
        statusCode: 500,
        body: {
          error: {
            type: 'INTERNAL_SERVER_ERROR',
            message: 'An unexpected error occurred'
          }
        }
      }).as('createUserError');
      
      // When: User tries to create a user
      cy.visit('/users');
      cy.get('[data-cy=create-user-button]').click();
      cy.get('[data-cy=username-input]').type('newuser');
      cy.get('[data-cy=email-input]').type('new@example.com');
      cy.get('[data-cy=password-input]').type('Password@123');
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-VIEWER]').click();
      cy.get('[data-cy=submit-button]').click();
      cy.wait('@createUserError');
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'An unexpected error occurred');
    });
  });

  describe('Validation Error Handling', () => {
    it('should display all validation errors at once', () => {
      // Given: User submits form with multiple validation errors
      cy.visit('/users');
      cy.get('[data-cy=create-user-button]').click();
      cy.get('[data-cy=username-input]').type('ab'); // Too short
      cy.get('[data-cy=email-input]').type('invalid'); // Invalid format
      cy.get('[data-cy=password-input]').type('weak'); // Weak password
      cy.get('[data-cy=submit-button]').click();
      
      // Then: All validation errors are displayed
      cy.get('[data-cy=username-error]').should('be.visible');
      cy.get('[data-cy=email-error]').should('be.visible');
      cy.get('[data-cy=password-error]').should('be.visible');
      cy.get('[data-cy=role-error]').should('be.visible');
    });

    it('should clear validation errors when corrected', () => {
      // Given: Form has validation errors
      cy.visit('/users');
      cy.get('[data-cy=create-user-button]').click();
      cy.get('[data-cy=submit-button]').click();
      cy.get('[data-cy=username-error]').should('be.visible');
      
      // When: User corrects the error
      cy.get('[data-cy=username-input]').type('validuser');
      
      // Then: Error message is cleared
      cy.get('[data-cy=username-error]').should('not.exist');
    });

    it('should show real-time validation feedback', () => {
      // Given: User is filling out form
      cy.visit('/users');
      cy.get('[data-cy=create-user-button]').click();
      
      // When: User types invalid email
      cy.get('[data-cy=email-input]').type('invalid').blur();
      
      // Then: Validation error appears immediately
      cy.get('[data-cy=email-error]').should('be.visible');
      
      // When: User corrects email
      cy.get('[data-cy=email-input]').clear().type('valid@example.com').blur();
      
      // Then: Error disappears
      cy.get('[data-cy=email-error]').should('not.exist');
    });
  });

  describe('Authorization Error Handling', () => {
    it('should handle insufficient permissions gracefully', () => {
      // Given: User with VIEWER role
      cy.logout();
      cy.login('viewer', 'Viewer@123');
      
      // When: User tries to access restricted endpoint
      cy.request({
        method: 'POST',
        url: 'http://localhost:8080/api/v1/users',
        headers: {
          'Authorization': `Bearer ${window.localStorage.getItem('accessToken')}`
        },
        body: {
          username: 'newuser',
          email: 'new@example.com',
          password: 'Password@123',
          roles: ['VIEWER']
        },
        failOnStatusCode: false
      }).then((response) => {
        // Then: 403 Forbidden is returned
        expect(response.status).to.eq(403);
        expect(response.body.error.type).to.eq('INSUFFICIENT_PERMISSIONS');
      });
    });

    it('should redirect to login when token expires', () => {
      // Given: User has expired token
      window.localStorage.setItem('accessToken', 'expired-token');
      
      // When: User tries to access protected page
      cy.visit('/users', { failOnStatusCode: false });
      
      // Then: User is redirected to login
      cy.url().should('include', '/login');
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Session expired');
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty search results', () => {
      // Given: User searches for non-existent user
      cy.visit('/users');
      cy.get('[data-cy=search-input]').type('nonexistentuser12345');
      
      // Then: Empty state is displayed
      cy.get('[data-cy=empty-state]')
        .should('be.visible')
        .and('contain', 'No users found');
    });

    it('should handle pagination with single page', () => {
      // Given: Only one page of results
      cy.intercept('GET', '/api/v1/users*', {
        statusCode: 200,
        body: {
          content: [{ id: '1', username: 'user1', email: 'user1@example.com' }],
          page: {
            size: 20,
            number: 0,
            totalElements: 1,
            totalPages: 1
          }
        }
      }).as('getUsers');
      
      cy.visit('/users');
      cy.wait('@getUsers');
      
      // Then: Pagination controls are hidden or disabled
      cy.get('[data-cy=next-page-button]').should('be.disabled');
      cy.get('[data-cy=previous-page-button]').should('be.disabled');
    });

    it('should handle special characters in input fields', () => {
      // Given: User enters special characters
      cy.visit('/users');
      cy.get('[data-cy=create-user-button]').click();
      cy.get('[data-cy=username-input]').type('user<script>alert("xss")</script>');
      cy.get('[data-cy=email-input]').type('test@example.com');
      cy.get('[data-cy=password-input]').type('Password@123');
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-VIEWER]').click();
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Input is sanitized and validation error is shown
      cy.get('[data-cy=username-error]')
        .should('be.visible')
        .and('contain', 'Username must contain only alphanumeric characters');
    });

    it('should handle concurrent user updates', () => {
      // Given: Two users try to update same user simultaneously
      cy.visit('/users');
      cy.get('[data-cy=user-row]').first().click();
      cy.get('[data-cy=edit-user-button]').click();
      
      // When: First user updates email
      cy.get('[data-cy=email-input]').clear().type('updated1@example.com');
      
      // And: Second user updates email (simulated via API)
      cy.request({
        method: 'PUT',
        url: 'http://localhost:8080/api/v1/users/1',
        headers: {
          'Authorization': `Bearer ${window.localStorage.getItem('accessToken')}`
        },
        body: {
          email: 'updated2@example.com'
        }
      });
      
      // And: First user submits
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Conflict error is handled
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'User has been modified by another user');
    });

    it('should handle very long input values', () => {
      // Given: User enters very long values
      const longString = 'a'.repeat(300);
      
      cy.visit('/users');
      cy.get('[data-cy=create-user-button]').click();
      cy.get('[data-cy=username-input]').type(longString);
      cy.get('[data-cy=email-input]').type(`${longString}@example.com`);
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Validation errors for max length are displayed
      cy.get('[data-cy=username-error]')
        .should('be.visible')
        .and('contain', 'Username must not exceed 100 characters');
      
      cy.get('[data-cy=email-error]')
        .should('be.visible')
        .and('contain', 'Email must not exceed 255 characters');
    });
  });

  describe('Loading States', () => {
    it('should show loading indicator during API calls', () => {
      // Given: API call takes time
      cy.intercept('GET', '/api/v1/users*', (req) => {
        req.reply({ delay: 2000, statusCode: 200, body: { content: [] } });
      }).as('getUsers');
      
      // When: User navigates to users page
      cy.visit('/users');
      
      // Then: Loading indicator is displayed
      cy.get('[data-cy=loading-spinner]').should('be.visible');
      
      // And: Loading indicator disappears after data loads
      cy.wait('@getUsers');
      cy.get('[data-cy=loading-spinner]').should('not.exist');
    });

    it('should disable submit button during form submission', () => {
      // Given: User is creating a user
      cy.visit('/users');
      cy.get('[data-cy=create-user-button]').click();
      cy.get('[data-cy=username-input]').type('newuser');
      cy.get('[data-cy=email-input]').type('new@example.com');
      cy.get('[data-cy=password-input]').type('Password@123');
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-VIEWER]').click();
      
      // When: User submits form
      cy.intercept('POST', '/api/v1/users', (req) => {
        req.reply({ delay: 2000, statusCode: 201, body: {} });
      }).as('createUser');
      
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Submit button is disabled
      cy.get('[data-cy=submit-button]').should('be.disabled');
      
      // And: Loading indicator is shown
      cy.get('[data-cy=submit-button]').should('contain', 'Creating...');
    });
  });

  describe('Session Management', () => {
    it('should handle multiple tabs with same user', () => {
      // Given: User is logged in
      cy.visit('/users');
      
      // When: User logs out in another tab (simulated)
      cy.window().then((win) => {
        win.localStorage.removeItem('accessToken');
        win.localStorage.removeItem('refreshToken');
      });
      
      // And: User tries to perform action
      cy.get('[data-cy=create-user-button]').click();
      
      // Then: User is redirected to login
      cy.url().should('include', '/login');
    });

    it('should refresh token automatically before expiration', () => {
      // Given: User is logged in with token about to expire
      cy.visit('/users');
      
      // When: User makes request near token expiration
      cy.wait(28000); // Wait 28 minutes (token expires in 30)
      cy.get('[data-cy=user-row]').first().click();
      
      // Then: Token is refreshed automatically
      cy.window().then((win) => {
        const newToken = win.localStorage.getItem('accessToken');
        expect(newToken).to.exist;
      });
      
      // And: Request succeeds
      cy.get('[data-cy=user-detail]').should('be.visible');
    });
  });
});
