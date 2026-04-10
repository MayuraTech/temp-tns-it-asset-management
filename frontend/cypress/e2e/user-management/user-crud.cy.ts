/**
 * E2E Tests: User CRUD Operations
 * 
 * Tests user creation, retrieval, update, and deletion
 * Validates Requirements: 4.1-4.8, 5.1-5.6, 6.1-6.6, 7.1-7.5
 */

describe('User CRUD Operations', () => {
  beforeEach(() => {
    // Login as administrator
    cy.login('admin', 'Admin@123456');
    cy.visit('/users');
  });

  afterEach(() => {
    cy.logout();
  });

  describe('User Creation', () => {
    it('should create a new user with valid data', () => {
      // Given: Administrator is on users page
      cy.get('[data-cy=create-user-button]').click();
      
      // When: Administrator fills in user form
      cy.get('[data-cy=username-input]').type('newuser');
      cy.get('[data-cy=email-input]').type('newuser@example.com');
      cy.get('[data-cy=password-input]').type('NewUser@123');
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-VIEWER]').click();
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Success message is displayed
      cy.get('[data-cy=success-message]')
        .should('be.visible')
        .and('contain', 'User created successfully');
      
      // And: New user appears in list
      cy.get('[data-cy=user-list]')
        .should('contain', 'newuser')
        .and('contain', 'newuser@example.com');
    });

    it('should validate username uniqueness', () => {
      // Given: User with username 'admin' already exists
      cy.get('[data-cy=create-user-button]').click();
      
      // When: Administrator tries to create user with duplicate username
      cy.get('[data-cy=username-input]').type('admin');
      cy.get('[data-cy=email-input]').type('another@example.com');
      cy.get('[data-cy=password-input]').type('Password@123');
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-VIEWER]').click();
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Username already exists');
    });

    it('should validate email uniqueness', () => {
      // Given: User with email 'admin@example.com' already exists
      cy.get('[data-cy=create-user-button]').click();
      
      // When: Administrator tries to create user with duplicate email
      cy.get('[data-cy=username-input]').type('newadmin');
      cy.get('[data-cy=email-input]').type('admin@example.com');
      cy.get('[data-cy=password-input]').type('Password@123');
      cy.get('[data-cy=role-select]').click();
      cy.get('[data-cy=role-option-VIEWER]').click();
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Email already exists');
    });

    it('should validate password complexity', () => {
      // Given: Administrator is creating a new user
      cy.get('[data-cy=create-user-button]').click();
      
      // When: Administrator enters weak password
      cy.get('[data-cy=username-input]').type('testuser');
      cy.get('[data-cy=email-input]').type('test@example.com');
      cy.get('[data-cy=password-input]').type('weak');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Validation error is displayed
      cy.get('[data-cy=password-error]')
        .should('be.visible')
        .and('contain', 'Password must contain at least 8 characters');
    });

    it('should validate required fields', () => {
      // Given: Administrator opens create user form
      cy.get('[data-cy=create-user-button]').click();
      
      // When: Administrator submits empty form
      cy.get('[data-cy=submit-button]').click();
      
      // Then: All required field errors are displayed
      cy.get('[data-cy=username-error]').should('be.visible');
      cy.get('[data-cy=email-error]').should('be.visible');
      cy.get('[data-cy=password-error]').should('be.visible');
      cy.get('[data-cy=role-error]').should('be.visible');
    });
  });

  describe('User Retrieval', () => {
    it('should display paginated list of users', () => {
      // Then: User list is displayed
      cy.get('[data-cy=user-list]').should('be.visible');
      
      // And: Pagination controls are visible
      cy.get('[data-cy=pagination]').should('be.visible');
      
      // And: Users are displayed
      cy.get('[data-cy=user-row]').should('have.length.at.least', 1);
    });

    it('should filter users by role', () => {
      // When: Administrator filters by ADMINISTRATOR role
      cy.get('[data-cy=role-filter]').click();
      cy.get('[data-cy=role-filter-ADMINISTRATOR]').click();
      
      // Then: Only administrators are displayed
      cy.get('[data-cy=user-row]').each(($row) => {
        cy.wrap($row).should('contain', 'ADMINISTRATOR');
      });
    });

    it('should search users by name, email, or username', () => {
      // When: Administrator searches for 'admin'
      cy.get('[data-cy=search-input]').type('admin');
      
      // Then: Only matching users are displayed
      cy.get('[data-cy=user-row]').should('have.length.at.least', 1);
      cy.get('[data-cy=user-row]').first().should('contain', 'admin');
    });

    it('should view user details', () => {
      // When: Administrator clicks on a user
      cy.get('[data-cy=user-row]').first().click();
      
      // Then: User details are displayed
      cy.get('[data-cy=user-detail]').should('be.visible');
      cy.get('[data-cy=user-username]').should('be.visible');
      cy.get('[data-cy=user-email]').should('be.visible');
      cy.get('[data-cy=user-roles]').should('be.visible');
      cy.get('[data-cy=user-status]').should('be.visible');
    });
  });

  describe('User Update', () => {
    it('should update user email', () => {
      // Given: Administrator views user details
      cy.get('[data-cy=user-row]').contains('testuser').click();
      cy.get('[data-cy=edit-user-button]').click();
      
      // When: Administrator updates email
      cy.get('[data-cy=email-input]').clear().type('newemail@example.com');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Success message is displayed
      cy.get('[data-cy=success-message]')
        .should('be.visible')
        .and('contain', 'User updated successfully');
      
      // And: Updated email is displayed
      cy.get('[data-cy=user-email]').should('contain', 'newemail@example.com');
    });

    it('should validate email uniqueness on update', () => {
      // Given: Administrator is editing a user
      cy.get('[data-cy=user-row]').contains('testuser').click();
      cy.get('[data-cy=edit-user-button]').click();
      
      // When: Administrator tries to use existing email
      cy.get('[data-cy=email-input]').clear().type('admin@example.com');
      cy.get('[data-cy=submit-button]').click();
      
      // Then: Error message is displayed
      cy.get('[data-cy=error-message]')
        .should('be.visible')
        .and('contain', 'Email already exists');
    });
  });

  describe('User Deletion', () => {
    it('should delete a user', () => {
      // Given: Administrator views user to delete
      cy.get('[data-cy=user-row]').contains('testuser').click();
      
      // When: Administrator deletes user
      cy.get('[data-cy=delete-user-button]').click();
      cy.get('[data-cy=confirm-delete-button]').click();
      
      // Then: Success message is displayed
      cy.get('[data-cy=success-message]')
        .should('be.visible')
        .and('contain', 'User deleted successfully');
      
      // And: User is removed from list
      cy.visit('/users');
      cy.get('[data-cy=user-list]').should('not.contain', 'testuser');
    });

    it('should prevent self-deletion', () => {
      // Given: Administrator views their own profile
      cy.get('[data-cy=user-row]').contains('admin').click();
      
      // Then: Delete button should be disabled
      cy.get('[data-cy=delete-user-button]').should('be.disabled');
    });

    it('should show confirmation dialog before deletion', () => {
      // Given: Administrator clicks delete
      cy.get('[data-cy=user-row]').contains('testuser').click();
      cy.get('[data-cy=delete-user-button]').click();
      
      // Then: Confirmation dialog is displayed
      cy.get('[data-cy=confirm-dialog]').should('be.visible');
      cy.get('[data-cy=confirm-dialog]')
        .should('contain', 'Are you sure you want to delete this user?');
    });
  });

  describe('Account Status Management', () => {
    it('should disable a user account', () => {
      // Given: Administrator views active user
      cy.get('[data-cy=user-row]').contains('testuser').click();
      
      // When: Administrator disables account
      cy.get('[data-cy=disable-user-button]').click();
      cy.get('[data-cy=confirm-button]').click();
      
      // Then: User status is updated to inactive
      cy.get('[data-cy=user-status]').should('contain', 'Inactive');
    });

    it('should enable a disabled user account', () => {
      // Given: Administrator views disabled user
      cy.get('[data-cy=user-row]').contains('disableduser').click();
      
      // When: Administrator enables account
      cy.get('[data-cy=enable-user-button]').click();
      cy.get('[data-cy=confirm-button]').click();
      
      // Then: User status is updated to active
      cy.get('[data-cy=user-status]').should('contain', 'Active');
    });

    it('should prevent self-disable', () => {
      // Given: Administrator views their own profile
      cy.get('[data-cy=user-row]').contains('admin').click();
      
      // Then: Disable button should be disabled
      cy.get('[data-cy=disable-user-button]').should('be.disabled');
    });
  });
});
