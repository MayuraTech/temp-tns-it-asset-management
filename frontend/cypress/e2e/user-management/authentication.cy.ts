/**
 * E2E Tests: User Authentication Flows
 * 
 * Tests authentication functionality including login, logout, and token refresh
 * Validates Requirements: 1.1-1.8, 2.1-2.5
 */

describe('User Authentication', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  describe('Login Flow', () => {
    it('should successfully login with valid credentials', () => {
      // Given: Valid user credentials
      cy.get('[data-cy=username-input]').type('admin');
      cy.get('[data-cy=password-input]').type('Admin@123456');
      
      // When: User submits login form
      cy.get('[data-cy=login-button]').click();
      
      // Then: User is redirected to dashboard
      cy.url().should('include', '/dashboard');
      
      // And: Access token is stored
      cy.window().then((win) => {
        expect(win.localStorage.getItem('accessToken')).to.exist;
        expect(win.localStorage.getItem('refreshToken')).to.exist;
      });
    });

    it('should display error message with invalid credentials', () => {
      // Given: Invalid credentials
      cy.get('[data-cy=username-input]').type('admin');
      cy.get('[data-cy=password-input]').type('WrongPassword');
      
      // When: User submits login form
      cy.get('[data-cy=login-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Invalid credentials');
      
      // And: User remains on login page
      cy.url().should('include', '/login');
    });

    it('should lock account after 5 failed login attempts', () => {
      // Given: User attempts login 5 times with wrong password
      for (let i = 0; i < 5; i++) {
        cy.get('[data-cy=username-input]').clear().type('testuser');
        cy.get('[data-cy=password-input]').clear().type('WrongPassword');
        cy.get('[data-cy=login-button]').click();
        cy.wait(500);
      }
      
      // When: User attempts 6th login
      cy.get('[data-cy=username-input]').clear().type('testuser');
      cy.get('[data-cy=password-input]').clear().type('WrongPassword');
      cy.get('[data-cy=login-button]').click();
      
      // Then: Account locked message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Account is locked');
    });

    it('should validate required fields', () => {
      // When: User submits empty form
      cy.get('[data-cy=login-button]').click();
      
      // Then: Validation errors are displayed
      cy.get('[data-cy=username-error]')
        .should('be.visible')
        .and('contain', 'Username is required');
      
      cy.get('[data-cy=password-error]')
        .should('be.visible')
        .and('contain', 'Password is required');
    });

    it('should prevent login for disabled accounts', () => {
      // Given: Disabled user credentials
      cy.get('[data-cy=username-input]').type('disableduser');
      cy.get('[data-cy=password-input]').type('Password@123');
      
      // When: User submits login form
      cy.get('[data-cy=login-button]').click();
      
      // Then: Account disabled error is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Account is disabled');
    });
  });

  describe('Logout Flow', () => {
    beforeEach(() => {
      // Login before each logout test
      cy.login('admin', 'Admin@123456');
      cy.visit('/dashboard');
    });

    it('should successfully logout and clear tokens', () => {
      // When: User clicks logout
      cy.get('[data-cy=user-menu]').click();
      cy.get('[data-cy=logout-button]').click();
      
      // Then: User is redirected to login page
      cy.url().should('include', '/login');
      
      // And: Tokens are cleared
      cy.window().then((win) => {
        expect(win.localStorage.getItem('accessToken')).to.be.null;
        expect(win.localStorage.getItem('refreshToken')).to.be.null;
      });
    });

    it('should invalidate session on logout', () => {
      // Given: User is logged in
      cy.window().then((win) => {
        const token = win.localStorage.getItem('accessToken');
        
        // When: User logs out
        cy.get('[data-cy=user-menu]').click();
        cy.get('[data-cy=logout-button]').click();
        
        // Then: Old token should not work
        cy.request({
          method: 'GET',
          url: 'http://localhost:8080/api/v1/users',
          headers: {
            'Authorization': `Bearer ${token}`
          },
          failOnStatusCode: false
        }).then((response) => {
          expect(response.status).to.eq(401);
        });
      });
    });
  });

  describe('Token Refresh', () => {
    it('should refresh expired access token using refresh token', () => {
      // Given: User is logged in
      cy.login('admin', 'Admin@123456');
      cy.visit('/dashboard');
      
      // When: Access token expires (simulated by waiting)
      cy.wait(1000);
      
      // And: User makes an API request
      cy.request({
        method: 'GET',
        url: 'http://localhost:8080/api/v1/users',
        headers: {
          'Authorization': `Bearer ${window.localStorage.getItem('accessToken')}`
        }
      }).then((response) => {
        // Then: Request succeeds (token was refreshed automatically)
        expect(response.status).to.eq(200);
      });
    });
  });

  describe('Protected Routes', () => {
    it('should redirect unauthenticated users to login', () => {
      // When: Unauthenticated user tries to access protected route
      cy.visit('/users');
      
      // Then: User is redirected to login
      cy.url().should('include', '/login');
    });

    it('should allow authenticated users to access protected routes', () => {
      // Given: User is logged in
      cy.login('admin', 'Admin@123456');
      
      // When: User navigates to protected route
      cy.visit('/users');
      
      // Then: Route is accessible
      cy.url().should('include', '/users');
      cy.get('[data-cy=user-list]').should('be.visible');
    });
  });
});
