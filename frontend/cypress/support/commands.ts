/// <reference types="cypress" />

/**
 * Custom Cypress commands for IT Asset Management E2E tests
 */

/**
 * Login command - authenticates a user and stores the JWT token
 * @param username - The username to login with
 * @param password - The password to login with
 */
Cypress.Commands.add('login', (username: string, password: string) => {
  cy.request({
    method: 'POST',
    url: '/api/v1/auth/login',
    body: {
      username,
      password
    }
  }).then((response) => {
    expect(response.status).to.eq(200);
    expect(response.body).to.have.property('accessToken');
    
    // Store the token in localStorage
    window.localStorage.setItem('accessToken', response.body.accessToken);
    
    // Set authorization header for subsequent requests
    cy.intercept('**', (req) => {
      req.headers['Authorization'] = `Bearer ${response.body.accessToken}`;
    });
  });
});

/**
 * Logout command - clears authentication tokens
 */
Cypress.Commands.add('logout', () => {
  window.localStorage.removeItem('accessToken');
  cy.visit('/login');
});
