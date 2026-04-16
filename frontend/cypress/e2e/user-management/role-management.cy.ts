/**
 * E2E Tests: Role Management
 * 
 * Tests role assignment and revocation functionality
 * Validates Requirements: 9.1-9.5, 10.1-10.5
 */

describe('Role Management', () => {
  beforeEach(() => {
    // Login as administrator
    cy.login('admin', 'Admin@123456');
    cy.visit('/users');
  });

  afterEach(() => {
    cy.logout();
  });

  describe('Role Assignment', () => {
    it('should assign a role to a user', () => {
      // Given: Administrator views user details
      cy.get('[data-cy=user-row]').contains('testuser').click();
      
      // When: Administrator assigns ASSET_MANAGER role
      cy.get('[data-cy=assign-role-button]').click();
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-ASSET_MANAGER]').click();
      cy.get('[data-cy=confirm-button]').click();
      
      // Then: Success message is displayed
      cy.get('[data-cy=success-message]')
        .should('be.visible')
        .and('contain', 'Role assigned successfully');
      
      // And: Role appears in user's role list
      cy.get('[data-cy=user-roles]').should('contain', 'ASSET_MANAGER');
    });

    it('should prevent duplicate role assignment', () => {
      // Given: User already has VIEWER role
      cy.get('[data-cy=user-row]').contains('testuser').click();
      
      // When: Administrator tries to assign VIEWER role again
      cy.get('[data-cy=assign-role-button]').click();
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-VIEWER]').click();
      cy.get('[data-cy=confirm-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'User already has this role');
    });

    it('should validate role is from allowed values', () => {
      // Given: Administrator is assigning a role
      cy.get('[data-cy=user-row]').contains('testuser').click();
      cy.get('[data-cy=assign-role-button]').click();
      
      // Then: Only valid roles are available
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option]').should('have.length', 3);
      cy.get('[data-cy=role-option-ADMINISTRATOR]').should('exist');
      cy.get('[data-cy=role-option-ASSET_MANAGER]').should('exist');
      cy.get('[data-cy=role-option-VIEWER]').should('exist');
    });

    it('should invalidate user sessions after role assignment', () => {
      // Given: Test user is logged in another session
      const testUserToken = 'test-user-token';
      
      // When: Administrator assigns new role
      cy.get('[data-cy=user-row]').contains('testuser').click();
      cy.get('[data-cy=assign-role-button]').click();
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-ASSET_MANAGER]').click();
      cy.get('[data-cy=confirm-button]').click();
      
      // Then: Old session should be invalidated
      cy.request({
        method: 'GET',
        url: 'http://localhost:8080/api/v1/users',
        headers: {
          'Authorization': `Bearer ${testUserToken}`
        },
        failOnStatusCode: false
      }).then((response) => {
        expect(response.status).to.eq(401);
      });
    });
  });

  describe('Role Revocation', () => {
    it('should revoke a role from a user', () => {
      // Given: User has multiple roles
      cy.get('[data-cy=user-row]').contains('multiuser').click();
      cy.get('[data-cy=user-roles]').should('contain', 'VIEWER');
      cy.get('[data-cy=user-roles]').should('contain', 'ASSET_MANAGER');
      
      // When: Administrator revokes VIEWER role
      cy.get('[data-cy=role-chip-VIEWER]').within(() => {
        cy.get('[data-cy=remove-role-button]').click();
      });
      cy.get('[data-cy=confirm-button]').click();
      
      // Then: Success message is displayed
      cy.get('[data-cy=success-message]')
        .should('be.visible')
        .and('contain', 'Role revoked successfully');
      
      // And: Role is removed from user's role list
      cy.get('[data-cy=user-roles]').should('not.contain', 'VIEWER');
      cy.get('[data-cy=user-roles]').should('contain', 'ASSET_MANAGER');
    });

    it('should prevent revoking last role', () => {
      // Given: User has only one role
      cy.get('[data-cy=user-row]').contains('singleuser').click();
      cy.get('[data-cy=user-roles]').should('contain', 'VIEWER');
      
      // When: Administrator tries to revoke last role
      cy.get('[data-cy=role-chip-VIEWER]').within(() => {
        cy.get('[data-cy=remove-role-button]').click();
      });
      cy.get('[data-cy=confirm-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Cannot revoke last role');
    });

    it('should prevent self-revocation of ADMINISTRATOR role', () => {
      // Given: Administrator views their own profile
      cy.get('[data-cy=user-row]').contains('admin').click();
      
      // Then: Remove button for ADMINISTRATOR role should be disabled
      cy.get('[data-cy=role-chip-ADMINISTRATOR]').within(() => {
        cy.get('[data-cy=remove-role-button]').should('be.disabled');
      });
    });

    it('should invalidate user sessions after role revocation', () => {
      // Given: Test user is logged in another session
      const testUserToken = 'test-user-token';
      
      // When: Administrator revokes a role
      cy.get('[data-cy=user-row]').contains('multiuser').click();
      cy.get('[data-cy=role-chip-VIEWER]').within(() => {
        cy.get('[data-cy=remove-role-button]').click();
      });
      cy.get('[data-cy=confirm-button]').click();
      
      // Then: Old session should be invalidated
      cy.request({
        method: 'GET',
        url: 'http://localhost:8080/api/v1/users',
        headers: {
          'Authorization': `Bearer ${testUserToken}`
        },
        failOnStatusCode: false
      }).then((response) => {
        expect(response.status).to.eq(401);
      });
    });
  });

  describe('Role-Based Access Control', () => {
    it('should allow ADMINISTRATOR to manage users', () => {
      // Given: User is logged in as ADMINISTRATOR
      // (already logged in as admin in beforeEach)
      
      // Then: All user management actions should be available
      cy.get('[data-cy=create-user-button]').should('be.visible');
      cy.get('[data-cy=user-row]').first().click();
      cy.get('[data-cy=edit-user-button]').should('be.visible');
      cy.get('[data-cy=delete-user-button]').should('be.visible');
      cy.get('[data-cy=assign-role-button]').should('be.visible');
    });

    it('should restrict ASSET_MANAGER to view-only access', () => {
      // Given: User is logged in as ASSET_MANAGER
      cy.logout();
      cy.login('assetmanager', 'AssetManager@123');
      cy.visit('/users');
      
      // Then: Create and delete actions should not be available
      cy.get('[data-cy=create-user-button]').should('not.exist');
      cy.get('[data-cy=user-row]').first().click();
      cy.get('[data-cy=edit-user-button]').should('not.exist');
      cy.get('[data-cy=delete-user-button]').should('not.exist');
      cy.get('[data-cy=assign-role-button]').should('not.exist');
    });

    it('should restrict VIEWER to profile-only access', () => {
      // Given: User is logged in as VIEWER
      cy.logout();
      cy.login('viewer', 'Viewer@123');
      
      // When: User tries to access users page
      cy.visit('/users', { failOnStatusCode: false });
      
      // Then: Access should be denied
      cy.url().should('not.include', '/users');
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Insufficient permissions');
    });
  });
});
