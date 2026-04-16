/**
 * E2E Tests: Profile Management
 * 
 * Tests user profile viewing, updating, and password change functionality
 * Validates Requirements: 11.1-11.6, 3.1-3.6
 */

describe('Profile Management', () => {
  beforeEach(() => {
    // Login as regular user
    cy.login('testuser', 'TestUser@123');
    cy.visit('/profile');
  });

  afterEach(() => {
    cy.logout();
  });

  describe('Profile Viewing', () => {
    it('should display current user profile information', () => {
      // Then: Profile information is displayed
      cy.get('[data-cy=profile-username]').should('be.visible');
      cy.get('[data-cy=profile-email]').should('be.visible');
      cy.get('[data-cy=profile-roles]').should('be.visible');
      cy.get('[data-cy=profile-created-at]').should('be.visible');
    });

    it('should not display password hash', () => {
      // Then: Password hash should not be visible
      cy.get('[data-cy=profile-container]').should('not.contain', 'passwordHash');
      cy.get('[data-cy=profile-container]').should('not.contain', '$2a$');
    });

    it('should display user roles', () => {
      // Then: User roles are displayed
      cy.get('[data-cy=profile-roles]').should('contain', 'VIEWER');
    });

    it('should display account status', () => {
      // Then: Account status is displayed
      cy.get('[data-cy=profile-status]').should('contain', 'Active');
    });
  });

  describe('Profile Update', () => {
    it('should update email address', () => {
      // Given: User is viewing their profile
      cy.get('[data-cy=edit-profile-button]').click();
      
      // When: User updates email
      cy.get('[data-cy=email-input]').clear().type('newemail@example.com');
      cy.get('[data-cy=save-button]').click();
      
      // Then: Success message is displayed
      cy.get('[data-cy=success-message]')
        .should('be.visible')
        .and('contain', 'Profile updated successfully');
      
      // And: Updated email is displayed
      cy.get('[data-cy=profile-email]').should('contain', 'newemail@example.com');
    });

    it('should validate email format', () => {
      // Given: User is editing profile
      cy.get('[data-cy=edit-profile-button]').click();
      
      // When: User enters invalid email
      cy.get('[data-cy=email-input]').clear().type('invalid-email');
      cy.get('[data-cy=save-button]').click();
      
      // Then: Validation error is displayed
      cy.get('[data-cy=email-error]')
        .should('be.visible')
        .and('contain', 'Invalid email format');
    });

    it('should validate email uniqueness', () => {
      // Given: User is editing profile
      cy.get('[data-cy=edit-profile-button]').click();
      
      // When: User tries to use existing email
      cy.get('[data-cy=email-input]').clear().type('admin@example.com');
      cy.get('[data-cy=save-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Email already exists');
    });

    it('should prevent role modification through profile', () => {
      // Given: User is viewing profile
      cy.get('[data-cy=edit-profile-button]').click();
      
      // Then: Role field should not be editable
      cy.get('[data-cy=role-input]').should('not.exist');
      cy.get('[data-cy=profile-roles]').should('be.visible');
    });

    it('should cancel profile update', () => {
      // Given: User is editing profile
      cy.get('[data-cy=edit-profile-button]').click();
      cy.get('[data-cy=email-input]').clear().type('cancelled@example.com');
      
      // When: User cancels
      cy.get('[data-cy=cancel-button]').click();
      
      // Then: Changes are not saved
      cy.get('[data-cy=profile-email]').should('not.contain', 'cancelled@example.com');
    });
  });

  describe('Password Change', () => {
    it('should change password with valid inputs', () => {
      // Given: User is on profile page
      cy.get('[data-cy=change-password-button]').click();
      
      // When: User enters valid password change data
      cy.get('[data-cy=current-password-input]').type('TestUser@123');
      cy.get('[data-cy=new-password-input]').type('NewPassword@123');
      cy.get('[data-cy=confirm-password-input]').type('NewPassword@123');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Success message is displayed
      cy.get('[data-cy=success-message]')
        .should('be.visible')
        .and('contain', 'Password changed successfully');
      
      // And: User is logged out (session invalidated)
      cy.url().should('include', '/login');
    });

    it('should validate current password', () => {
      // Given: User is changing password
      cy.get('[data-cy=change-password-button]').click();
      
      // When: User enters wrong current password
      cy.get('[data-cy=current-password-input]').type('WrongPassword@123');
      cy.get('[data-cy=new-password-input]').type('NewPassword@123');
      cy.get('[data-cy=confirm-password-input]').type('NewPassword@123');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Current password is incorrect');
    });

    it('should validate password complexity', () => {
      // Given: User is changing password
      cy.get('[data-cy=change-password-button]').click();
      
      // When: User enters weak password
      cy.get('[data-cy=current-password-input]').type('TestUser@123');
      cy.get('[data-cy=new-password-input]').type('weak');
      cy.get('[data-cy=confirm-password-input]').type('weak');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Validation error is displayed
      cy.get('[data-cy=new-password-error]')
        .should('be.visible')
        .and('contain', 'Password must contain at least 8 characters');
    });

    it('should validate password confirmation match', () => {
      // Given: User is changing password
      cy.get('[data-cy=change-password-button]').click();
      
      // When: User enters mismatched passwords
      cy.get('[data-cy=current-password-input]').type('TestUser@123');
      cy.get('[data-cy=new-password-input]').type('NewPassword@123');
      cy.get('[data-cy=confirm-password-input]').type('DifferentPassword@123');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Validation error is displayed
      cy.get('[data-cy=confirm-password-error]')
        .should('be.visible')
        .and('contain', 'Passwords do not match');
    });

    it('should prevent using same password', () => {
      // Given: User is changing password
      cy.get('[data-cy=change-password-button]').click();
      
      // When: User enters same password as current
      cy.get('[data-cy=current-password-input]').type('TestUser@123');
      cy.get('[data-cy=new-password-input]').type('TestUser@123');
      cy.get('[data-cy=confirm-password-input]').type('TestUser@123');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'New password must be different from current password');
    });

    it('should show password strength indicator', () => {
      // Given: User is changing password
      cy.get('[data-cy=change-password-button]').click();
      
      // When: User types new password
      cy.get('[data-cy=new-password-input]').type('Weak1!');
      
      // Then: Password strength indicator is displayed
      cy.get('[data-cy=password-strength]').should('be.visible');
      
      // When: User types stronger password
      cy.get('[data-cy=new-password-input]').clear().type('VeryStrong@Password123');
      
      // Then: Strength indicator updates
      cy.get('[data-cy=password-strength]').should('contain', 'Strong');
    });

    it('should invalidate all sessions after password change', () => {
      // Given: User has multiple active sessions
      const oldToken = window.localStorage.getItem('accessToken');
      
      // When: User changes password
      cy.get('[data-cy=change-password-button]').click();
      cy.get('[data-cy=current-password-input]').type('TestUser@123');
      cy.get('[data-cy=new-password-input]').type('NewPassword@123');
      cy.get('[data-cy=confirm-password-input]').type('NewPassword@123');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Old token should not work
      cy.request({
        method: 'GET',
        url: 'http://localhost:8080/api/v1/profile',
        headers: {
          'Authorization': `Bearer ${oldToken}`
        },
        failOnStatusCode: false
      }).then((response) => {
        expect(response.status).to.eq(401);
      });
    });
  });

  describe('Responsive Design', () => {
    it('should display correctly on mobile devices', () => {
      // Given: Mobile viewport
      cy.viewport('iphone-x');
      
      // Then: Profile should be responsive
      cy.get('[data-cy=profile-container]').should('be.visible');
      cy.get('[data-cy=edit-profile-button]').should('be.visible');
      cy.get('[data-cy=change-password-button]').should('be.visible');
    });

    it('should display correctly on tablet devices', () => {
      // Given: Tablet viewport
      cy.viewport('ipad-2');
      
      // Then: Profile should be responsive
      cy.get('[data-cy=profile-container]').should('be.visible');
      cy.get('[data-cy=edit-profile-button]').should('be.visible');
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels', () => {
      // Then: Form elements have ARIA labels
      cy.get('[data-cy=edit-profile-button]').should('have.attr', 'aria-label');
      cy.get('[data-cy=change-password-button]').should('have.attr', 'aria-label');
    });

    it('should support keyboard navigation', () => {
      // When: User navigates with keyboard
      cy.get('[data-cy=edit-profile-button]').focus();
      cy.focused().should('have.attr', 'data-cy', 'edit-profile-button');
      
      // And: User presses Tab
      cy.focused().tab();
      
      // Then: Focus moves to next element
      cy.focused().should('have.attr', 'data-cy', 'change-password-button');
    });

    it('should announce success messages to screen readers', () => {
      // Given: User updates profile
      cy.get('[data-cy=edit-profile-button]').click();
      cy.get('[data-cy=email-input]').clear().type('updated@example.com');
      cy.get('[data-cy=save-button]').click();
      
      // Then: Success message has ARIA live region
      cy.get('[data-cy=success-message]')
        .should('have.attr', 'role', 'alert')
        .and('have.attr', 'aria-live', 'polite');
    });
  });
});
